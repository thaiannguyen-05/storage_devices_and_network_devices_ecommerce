package module.bussiness.cart;

import entity.ProductEntity;
import entity.ProductVariantEntity;
import java.math.BigDecimal;
import java.util.List;
import module.bussiness.cart.repository.impl.CartRepository;
import module.bussiness.cart.response_dto.AddToCartResponseDto;
import module.bussiness.cart.response_dto.GetCartResponseDto;
import module.bussiness.cart.response_dto.RemoveFromCartResponseDto;
import module.bussiness.cart.response_dto.UpdateCartItemResponseDto;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.VariantRepository;
import module.core.common.BaseResponse;
import module.core.config.AppConfig;

public class CartService {
    private final CartRepository cartRepository = new CartRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final VariantRepository variantRepository = new VariantRepository();

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
        if (isBlank(productId)) {
            fail(response, "Product is required");
            return response;
        }
        if (isBlank(variantId)) {
            fail(response, "Please choose a product variant");
            return response;
        }
        if (quantity <= 0 || quantity > AppConfig.MAX_CART_ITEMS) {
            fail(response, "Quantity is invalid");
            return response;
        }

        ProductEntity product = productRepository.findById(productId);
        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            fail(response, "Product is unavailable");
            return response;
        }

        ProductVariantEntity variant = variantRepository.findById(variantId);
        if (variant == null || !productId.equals(variant.getProductId())) {
            fail(response, "Variant is invalid");
            return response;
        }
        if (!"ACTIVE".equalsIgnoreCase(variant.getStatus()) || variant.getQuantity() == null || variant.getQuantity() <= 0) {
            fail(response, "Variant is out of stock");
            return response;
        }

        String cartId = cartRepository.getOrCreateCart(userId);
        CartItemView existingItem = cartRepository.findItem(cartId, productId, variantId);
        int existingQuantity = existingItem == null ? 0 : existingItem.getQuantity();
        if (existingQuantity + quantity > variant.getQuantity()) {
            fail(response, "Requested quantity exceeds stock");
            return response;
        }

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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
