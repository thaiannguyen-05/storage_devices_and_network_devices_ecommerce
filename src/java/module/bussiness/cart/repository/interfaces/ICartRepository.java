package module.bussiness.cart.repository.interfaces;

import java.util.List;
import module.bussiness.cart.CartItemView;

public interface ICartRepository {
    String getOrCreateCart(String userId);
    List<CartItemView> getItemsByCartId(String cartId);
    List<CartItemView> getItemsByCartId(String cartId, int offset, int limit);
    int countItemsByCartId(String cartId);
    CartItemView findItem(String itemId);
    CartItemView findItem(String cartId, String productId, String variantId);
    void addItem(String cartId, String productId, String variantId, int quantity);
    void updateQuantity(String itemId, int quantity);
    void removeItem(String itemId);
    void clearCart(String cartId);
}
