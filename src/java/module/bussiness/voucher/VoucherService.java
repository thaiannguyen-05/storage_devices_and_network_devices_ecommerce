package module.bussiness.voucher;

import entity.VoucherEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import module.bussiness.voucher.repository.impl.VoucherRepository;
import module.core.common.BaseResponse;

public class VoucherService {
    private final VoucherRepository voucherRepository = new VoucherRepository();

    public List<VoucherEntity> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public VoucherEntity getVoucherById(String id) {
        return voucherRepository.findById(id);
    }

    public List<VoucherEntity> getActiveVouchersForUser(String userId) {
        return voucherRepository.findActiveByUserId(userId);
    }

    public BaseResponse createVoucher(String id, String percent, String userId, String expTime, String quantity) {
        BaseResponse response = new BaseResponse();
        if (isBlank(percent) || isBlank(userId) || isBlank(expTime) || isBlank(quantity)) {
            fail(response, "Percent, user, expiry and quantity are required");
            return response;
        }
        String voucherId = isBlank(id) ? java.util.UUID.randomUUID().toString() : id.trim();
        VoucherEntity voucher = new VoucherEntity(voucherId, parsePercent(percent), userId.trim(),
                LocalDate.parse(expTime.trim()), LocalDateTime.now(), parseInt(quantity));
        voucherRepository.insert(voucher);
        response.setSuccess(true);
        response.setSuccessMessage("Voucher created");
        return response;
    }

    public BaseResponse updateVoucher(String id, String percent, String userId, String expTime, String quantity) {
        BaseResponse response = new BaseResponse();
        VoucherEntity voucher = voucherRepository.findById(id);
        if (voucher == null) {
            fail(response, "Voucher not found");
            return response;
        }
        voucher.setPercent(isBlank(percent) ? voucher.getPercent() : parsePercent(percent));
        voucher.setUserId(defaultValue(userId, voucher.getUserId()));
        voucher.setExpTime(isBlank(expTime) ? voucher.getExpTime() : LocalDate.parse(expTime.trim()));
        voucher.setQuantity(isBlank(quantity) ? voucher.getQuantity() : parseInt(quantity));
        voucherRepository.update(voucher);
        response.setSuccess(true);
        response.setSuccessMessage("Voucher updated");
        return response;
    }

    public BaseResponse deleteVoucher(String id) {
        BaseResponse response = new BaseResponse();
        voucherRepository.delete(id);
        response.setSuccess(true);
        response.setSuccessMessage("Voucher deleted");
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private double parsePercent(String value) {
        return Double.parseDouble(value.trim());
    }

    private int parseInt(String value) {
        return Integer.parseInt(value.trim());
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }
}
