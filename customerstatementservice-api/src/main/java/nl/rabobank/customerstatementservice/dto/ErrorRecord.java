package nl.rabobank.customerstatementservice.dto;

public class ErrorRecord {

    private Integer reference;

    private String accountNumber;

    public ErrorRecord(Integer reference, String accountNumber) {
        this.reference = reference;
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "ErrorRecord{" +
                "reference='" + reference + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Integer getReference() {
        return reference;
    }

    public void setReference(Integer reference) {
        this.reference = reference;
    }
}
