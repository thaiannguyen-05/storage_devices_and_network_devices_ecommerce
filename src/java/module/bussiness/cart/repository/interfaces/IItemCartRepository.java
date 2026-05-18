package module.bussiness.cart.repository.interfaces;

import entity.ItemCartEntity;
import java.util.List;

public interface IItemCartRepository {
    List<ItemCartEntity> findByCartId(String cartId);
    ItemCartEntity findByCartIdAndProductAndVariant(String cartId, String productId, String variantId);
    ItemCartEntity create(String cartId, String productId, String variantId, int quantity);
    ItemCartEntity upsert(String cartId, String productId, String variantId, int quantity);
    boolean updateQuantity(String id, int quantity);
    boolean deleteByCartIdAndProductAndVariant(String cartId, String productId, String variantId);
    boolean clearByCartId(String cartId);
}
