package module.bussiness.voucher.repository.interfaces;

import entity.VoucherEntity;
import java.util.List;

public interface IVoucherRepository {
    List<VoucherEntity> findAll();
    VoucherEntity findById(String id);
    List<VoucherEntity> findActiveByUserId(String userId);
    void insert(VoucherEntity voucher);
    void update(VoucherEntity voucher);
    void delete(String id);
}
