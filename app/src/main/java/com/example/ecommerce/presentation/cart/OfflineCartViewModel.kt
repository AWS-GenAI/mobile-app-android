package com.example.ecommerce.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecommerce.domain.model.CartItem
import com.example.ecommerce.domain.repository.CartRepository
import com.example.ecommerce.domain.usecase.SyncCartUseCase
import com.example.ecommerce.util.NetworkMonitor
import com.example.ecommerce.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the shopping cart with offline support.
 * Implements the requirements from MOB-345: Implement offline shopping cart functionality.
 */
@HiltViewModel
class OfflineCartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val syncCartUseCase: SyncCartUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        loadCart()
        observeNetworkChanges()
    }

    /**
     * Loads the cart items from local storage.
     */
    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            try {
                cartRepository.getCartItems().collectLatest { items ->
                    _uiState.value = if (items.isEmpty()) {
                        CartUiState.Empty
                    } else {
                        CartUiState.Success(items)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading cart")
                _uiState.value = CartUiState.Error("Failed to load cart: ${e.message}")
            }
        }
    }

    /**
     * Adds an item to the cart.
     * Works in both online and offline mode.
     *
     * @param productId The ID of the product to add
     * @param quantity The quantity to add
     */
    fun addToCart(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.addToCart(productId, quantity)
                attemptSync()
            } catch (e: Exception) {
                Timber.e(e, "Error adding item to cart")
                _uiState.value = CartUiState.Error("Failed to add item: ${e.message}")
            }
        }
    }
    /**
     * Updates the quantity of an item in the cart.
     *
     * @param cartItem The cart item to update
     * @param newQuantity The new quantity
     */
    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartRepository.removeFromCart(cartItem.productId)
                } else {
                    cartRepository.updateCartItemQuantity(cartItem.productId, newQuantity)
                }
                attemptSync()
            } catch (e: Exception) {
                Timber.e(e, "Error updating cart item")
                _uiState.value = CartUiState.Error("Failed to update item: ${e.message}")
            }
        }
    }

    /**
     * Removes an item from the cart.
     *
     * @param productId The ID of the product to remove
     */
    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(productId)
                attemptSync()
            } catch (e: Exception) {
                Timber.e(e, "Error removing item from cart")
                _uiState.value = CartUiState.Error("Failed to remove item: ${e.message}")
            }
        }
    }

    /**
     * Clears all items from the cart.
     */
    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
                attemptSync()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing cart")
                _uiState.value = CartUiState.Error("Failed to clear cart: ${e.message}")
            }
        }
    }

    /**
     * Attempts to synchronize the local cart with the server.
     * Only runs if the device is online.
     */
    fun attemptSync() {
        viewModelScope.launch {
            if (networkMonitor.isOnline()) {
                _syncState.value = SyncState.Syncing
                when (val result = syncCartUseCase()) {
                    is Result.Success -> {
                        _syncState.value = SyncState.Success
                        Timber.d("Cart synchronized successfully")
                    }
                    is Result.Error -> {
                        _syncState.value = SyncState.Error(result.exception.message ?: "Unknown error")
                        Timber.e(result.exception, "Failed to synchronize cart")
                    }
                }
            } else {
                _syncState.value = SyncState.Offline
                Timber.d("Device is offline, skipping cart synchronization")
            }
        }
    }

    /**
     * Observes network changes and attempts to sync when the device comes online.
     */
    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.isOnlineFlow.collectLatest { isOnline ->
                if (isOnline) {
                    Timber.d("Network connection restored, attempting to sync cart")
                    attemptSync()
                } else {
                    _syncState.value = SyncState.Offline
                    Timber.d("Network connection lost, cart will be synchronized when online")
                }
            }
        }
    }
}


/**
 * Represents the UI state of the cart screen.
 */
sealed class CartUiState {
    object Loading : CartUiState()
    object Empty : CartUiState()
    data class Success(val items: List<CartItem>) : CartUiState()
    data class Error(val message: String) : CartUiState()
}

/**
 * Represents the synchronization state of the cart.
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    object Offline : SyncState()
    data class Error(val message: String) : SyncState()
}
