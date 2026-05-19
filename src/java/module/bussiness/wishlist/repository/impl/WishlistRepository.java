package module.bussiness.wishlist.repository.impl;

import java.util.List;
import module.bussiness.wishlist.WishlistItemView;
import module.bussiness.wishlist.repository.interfaces.IWishlistRepository;
import module.core.sql.JdbcHelper;

public class WishlistRepository implements IWishlistRepository {
    @Override
    public List<WishlistItemView> findByUserId(String userId) {
        return JdbcHelper.executeQuery(
                "SELECT sp.id, sp.productId, pv.id AS variantId, p.name AS productName, pv.sku, pv.imageUrl, pv.price "
                + "FROM SavedProduct sp "
                + "JOIN Product p ON p.id = sp.productId "
                + "LEFT JOIN ProductVariant pv ON pv.id = ("
                + "    SELECT pv2.id FROM ProductVariant pv2 WHERE pv2.productId = p.id ORDER BY pv2.createdAt ASC LIMIT 1"
                + ") "
                + "WHERE sp.userId = ? ORDER BY sp.createdAt DESC",
                rs -> {
                    WishlistItemView item = new WishlistItemView();
                    item.setId(rs.getString("id"));
                    item.setProductId(rs.getString("productId"));
                    item.setVariantId(rs.getString("variantId"));
                    item.setProductName(rs.getString("productName"));
                    item.setSku(rs.getString("sku"));
                    item.setImageUrl(rs.getString("imageUrl"));
                    item.setPrice(rs.getBigDecimal("price"));
                    return item;
                }, userId);
    }

    @Override
    public void insert(String id, String userId, String productId) {
        JdbcHelper.executeUpdate(
                "INSERT INTO SavedProduct (id, userId, productId, quantity) VALUES (?, ?, ?, 1)",
                id, userId, productId);
    }

    @Override
    public void delete(String userId, String productId) {
        JdbcHelper.executeUpdate("DELETE FROM SavedProduct WHERE userId = ? AND productId = ?", userId, productId);
    }

    @Override
    public void deleteAll(String userId) {
        JdbcHelper.executeUpdate("DELETE FROM SavedProduct WHERE userId = ?", userId);
    }
}
