package module.bussiness.cart;

import java.math.BigDecimal;
import java.util.List;
import module.bussiness.cart.repository.impl.CartRepository;
import module.bussiness.cart.response_dto.AddToCartResponseDto;
import module.bussiness.cart.response_dto.GetCartResponseDto;
import module.bussiness.cart.response_dto.RemoveFromCartResponseDto;
import module.bussiness.cart.response_dto.UpdateCartItemResponseDto;
import module.core.common.BaseResponse;
import module.core.config.AppConfig;

public class CartService {
    private final CartRepository cartRepository = new CartRepository();

    public GetCartResponseDto getCart(String userId) {
        return getCart(userId, 1, Integer.MAX_VALUE);
    }

    public GetCartResponseDto getCart(String userId, int page, int pageSize) {
        String cartId = cartRepository.getOrCreateCart(userId);
        int offset = Math.max(0, page - 1) * pageSize;
        List<CartItemView> items = cartRepository.getItemsByCartId(cartId, offset, pageSize);
        int totalItems = cartRepository.countItemsByCartId(cartId);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        GetCartResponseDto response = new GetCartResponseDto();
        response.setCartId(cartId);
        response.setItems(items);
        response.setTotal(items.stream().map(CartItemView::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        response.setSuccess(true);
        return response;
    }

    public AddToCartResponseDto addToCart(String userId, String productId, String variantId, int quantity) {
        AddToCartResponseDto response = new AddToCartResponseDto();
        if (quantity <= 0 || quantity > AppConfig.MAX_CART_ITEMS) {
            fail(response, "Quantity is invalid");
            return response;
        }
        String cartId = cartRepository.getOrCreateCart(userId);
        cartRepository.addItem(cartId, productId, variantId, quantity);
        response.setSuccess(true);
        response.setSuccessMessage("Added to cart");
        return response;
    }

    public UpdateCartItemResponseDto updateQuantity(String userId, String itemId, int quantity) {
        UpdateCartItemResponseDto response = new UpdateCartItemResponseDto();
        CartItemView item = cartRepository.findItem(itemId);
        if (item == null || quantity <= 0 || quantity > item.getStockQuantity()) {
            fail(response, "Quantity is invalid");
            return response;
        }
        cartRepository.updateQuantity(itemId, quantity);
        response.setSuccess(true);
        response.setSuccessMessage("Cart updated");
        return response;
    }

    public RemoveFromCartResponseDto removeItem(String userId, String itemId) {
        RemoveFromCartResponseDto response = new RemoveFromCartResponseDto();
        cartRepository.removeItem(itemId);
        response.setSuccess(true);
        response.setSuccessMessage("Item removed");
        return response;
    }

    public RemoveFromCartResponseDto clearCart(String userId) {
        RemoveFromCartResponseDto response = new RemoveFromCartResponseDto();
        cartRepository.clearCart(cartRepository.getOrCreateCart(userId));
        response.setSuccess(true);
        response.setSuccessMessage("Cart cleared");
        return response;
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
