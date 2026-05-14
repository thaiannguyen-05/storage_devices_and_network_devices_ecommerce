package module.bussiness.payment.repository.interfaces;

import entity.VoucherEntity;
import java.time.LocalDate;
import java.util.List;

public interface IVoucherRepository {

    VoucherEntity findById(String id);

    List<VoucherEntity> findByUserId(String userId);

    boolean create(String userId, Double percent, LocalDate expTime, Integer quantity);

    boolean decreaseQuantity(String id);

    long calculateDiscount(String voucherId, long subtotal);
}
