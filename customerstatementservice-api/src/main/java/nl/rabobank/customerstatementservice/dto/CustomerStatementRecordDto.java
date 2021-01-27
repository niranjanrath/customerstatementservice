package nl.rabobank.customerstatementservice.dto;

import javax.validation.constraints.NotNull;

public class CustomerStatementRecordDto {

    @NotNull
    private Integer transactionReference;

    @NotNull
    private String accountNumber;

    @NotNull
    private Integer startBalance;

    @NotNull
    private Integer mutation;

    private String description;

    @NotNull
    private Integer endBalance;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public Integer getEndBalance() {
        return endBalance;
    }

    public Integer getMutation() {
        return mutation;
    }

    public Integer getStartBalance() {
        return startBalance;
    }

    public Integer getTransactionReference() {
        return transactionReference;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndBalance(Integer endBalance) {
        this.endBalance = endBalance;
    }

    public void setMutation(Integer mutation) {
        this.mutation = mutation;
    }

    public void setStartBalance(Integer startBalance) {
        this.startBalance = startBalance;
    }

    public void setTransactionReference(Integer transactionReference) {
        this.transactionReference = transactionReference;
    }

    @Override
    public String toString() {
        return "CustomerStatementRecordDto{" +
                "transactionReference=" + transactionReference +
                ", accountNumber='" + accountNumber + '\'' +
                ", startBalance=" + startBalance +
                ", mutation=" + mutation +
                ", description='" + description + '\'' +
                ", endBalance=" + endBalance +
                '}';
    }
}
