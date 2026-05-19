package module.bussiness.wishlist;

import java.util.List;
import java.util.UUID;
import module.bussiness.wishlist.repository.impl.WishlistRepository;
import module.core.common.BaseResponse;

public class WishlistService {
    private final WishlistRepository wishlistRepository = new WishlistRepository();

    public List<WishlistItemView> getWishlistByUserId(String userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public BaseResponse addToWishlist(String userId, String productId) {
        BaseResponse response = new BaseResponse();
        if (isBlank(userId) || isBlank(productId)) {
            fail(response, "User and product are required");
            return response;
        }
        wishlistRepository.insert(UUID.randomUUID().toString(), userId, productId);
        response.setSuccess(true);
        response.setSuccessMessage("Saved to wishlist");
        return response;
    }

    public BaseResponse removeFromWishlist(String userId, String productId) {
        BaseResponse response = new BaseResponse();
        wishlistRepository.delete(userId, productId);
        response.setSuccess(true);
        response.setSuccessMessage("Removed from wishlist");
        return response;
    }

    public BaseResponse clearWishlist(String userId) {
        BaseResponse response = new BaseResponse();
        wishlistRepository.deleteAll(userId);
        response.setSuccess(true);
        response.setSuccessMessage("Wishlist cleared");
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
