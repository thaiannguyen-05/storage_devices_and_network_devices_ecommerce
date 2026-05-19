package module.bussiness.wishlist.repository.interfaces;

import java.util.List;
import module.bussiness.wishlist.WishlistItemView;

public interface IWishlistRepository {
    List<WishlistItemView> findByUserId(String userId);
    void insert(String id, String userId, String productId);
    void delete(String userId, String productId);
    void deleteAll(String userId);
}
