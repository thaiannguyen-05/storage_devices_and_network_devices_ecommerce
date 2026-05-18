package module.bussiness.order.dto;

import jakarta.json.bind.annotation.JsonbProperty;

public class SepayWebhookPayload {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String code;
    private String content;
    private String transferType;
    private Double transferAmount;
    private Double accumulatedAsset;
    private String referenceCode;
    private String description;
    private String reference;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    
    // transactionDate / transaction_date
    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
    @JsonbProperty("transaction_date")
    public void setTransactionDateSnake(String transactionDate) { this.transactionDate = transactionDate; }
    
    // accountNumber / account_number
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    @JsonbProperty("account_number")
    public void setAccountNumberSnake(String accountNumber) { this.accountNumber = accountNumber; }
    
    // subAccount / sub_account
    public String getSubAccount() { return subAccount; }
    public void setSubAccount(String subAccount) { this.subAccount = subAccount; }
    @JsonbProperty("sub_account")
    public void setSubAccountSnake(String subAccount) { this.subAccount = subAccount; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    // transferType / transfer_type
    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }
    @JsonbProperty("transfer_type")
    public void setTransferTypeSnake(String transferType) { this.transferType = transferType; }
    
    // transferAmount / transfer_amount
    public Double getTransferAmount() { return transferAmount; }
    public void setTransferAmount(Double transferAmount) { this.transferAmount = transferAmount; }
    @JsonbProperty("transfer_amount")
    public void setTransferAmountSnake(Double transferAmount) { this.transferAmount = transferAmount; }
    
    // accumulatedAsset / accumulated_asset / accumulated
    public Double getAccumulatedAsset() { return accumulatedAsset; }
    public void setAccumulatedAsset(Double accumulatedAsset) { this.accumulatedAsset = accumulatedAsset; }
    @JsonbProperty("accumulated_asset")
    public void setAccumulatedAssetSnake(Double accumulatedAsset) { this.accumulatedAsset = accumulatedAsset; }
    @JsonbProperty("accumulated")
    public void setAccumulated(Double accumulated) { this.accumulatedAsset = accumulated; }
    
    // referenceCode / reference_code
    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    @JsonbProperty("reference_code")
    public void setReferenceCodeSnake(String referenceCode) { this.referenceCode = referenceCode; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
