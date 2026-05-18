package module.bussiness.cart.repository.impl;

import java.util.List;
import java.util.UUID;
import module.bussiness.cart.CartItemView;
import module.bussiness.cart.repository.interfaces.ICartRepository;
import module.core.sql.JdbcHelper;

public class CartRepository implements ICartRepository {
    @Override
    public String getOrCreateCart(String userId) {
        List<String> carts = JdbcHelper.executeQuery("SELECT id FROM OrderCart WHERE userId = ? LIMIT 1", rs -> rs.getString("id"), userId);
        if (!carts.isEmpty()) {
            return carts.get(0);
        }
        String id = UUID.randomUUID().toString();
        JdbcHelper.executeUpdate("INSERT INTO OrderCart (id, userId) VALUES (?, ?)", id, userId);
        return id;
    }

    @Override
    public List<CartItemView> getItemsByCartId(String cartId) {
        return JdbcHelper.executeQuery("SELECT i.*, p.name productName, v.sku, v.price, v.quantity stockQuantity FROM ItemCart i JOIN Product p ON i.productId = p.id LEFT JOIN ProductVariant v ON i.variantId = v.id WHERE i.cartId = ? ORDER BY i.createdAt DESC",
                rs -> mapItem(rs), cartId);
    }

    @Override
    public List<CartItemView> getItemsByCartId(String cartId, int offset, int limit) {
        return JdbcHelper.executeQuery("SELECT i.*, p.name productName, v.sku, v.price, v.quantity stockQuantity FROM ItemCart i JOIN Product p ON i.productId = p.id LEFT JOIN ProductVariant v ON i.variantId = v.id WHERE i.cartId = ? ORDER BY i.createdAt DESC LIMIT ? OFFSET ?",
                rs -> mapItem(rs), cartId, limit, offset);
    }

    @Override
    public int countItemsByCartId(String cartId) {
        List<Integer> count = JdbcHelper.executeQuery("SELECT COUNT(*) AS total FROM ItemCart WHERE cartId = ?",
                rs -> rs.getInt("total"), cartId);
        return count.isEmpty() ? 0 : count.get(0);
    }

    @Override
    public CartItemView findItem(String itemId) {
        List<CartItemView> rows = JdbcHelper.executeQuery("SELECT i.*, p.name productName, v.sku, v.price, v.quantity stockQuantity FROM ItemCart i JOIN Product p ON i.productId = p.id LEFT JOIN ProductVariant v ON i.variantId = v.id WHERE i.id = ?",
                rs -> mapItem(rs), itemId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public void addItem(String cartId, String productId, String variantId, int quantity) {
        JdbcHelper.executeUpdate("INSERT INTO ItemCart (id, cartId, productId, variantId, quantity) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)",
                UUID.randomUUID().toString(), cartId, productId, variantId, quantity);
    }

    @Override
    public void updateQuantity(String itemId, int quantity) {
        JdbcHelper.executeUpdate("UPDATE ItemCart SET quantity = ? WHERE id = ?", quantity, itemId);
    }

    @Override
    public void removeItem(String itemId) {
        JdbcHelper.executeUpdate("DELETE FROM ItemCart WHERE id = ?", itemId);
    }

    @Override
    public void clearCart(String cartId) {
        JdbcHelper.executeUpdate("DELETE FROM ItemCart WHERE cartId = ?", cartId);
    }

    private CartItemView mapItem(java.sql.ResultSet rs) throws java.sql.SQLException {
        CartItemView item = new CartItemView();
        item.setId(rs.getString("id"));
        item.setProductId(rs.getString("productId"));
        item.setVariantId(rs.getString("variantId"));
        item.setProductName(rs.getString("productName"));
        item.setSku(rs.getString("sku"));
        item.setQuantity(rs.getInt("quantity"));
        item.setStockQuantity(rs.getInt("stockQuantity"));
        item.setPrice(rs.getBigDecimal("price"));
        return item;
    }
}
