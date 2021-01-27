package nl.rabobank.customerstatementservice.infrastructure;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "customer_statement_record")
public class CustomerStatementRecord {

    @Id
    @Column(name = "transaction_reference", nullable = false, unique = true)
    private Integer transactionReference;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "start_balance")
    private int startBalance;

    @Column(name = "mutation")
    private int mutation;

    @Column(name = "description")
    private String description;

    @Column(name = "end_balance")
    private int endBalance;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getDescription() {
        return description;
    }

    public int getEndBalance() {
        return endBalance;
    }

    public int getMutation() {
        return mutation;
    }

    public int getStartBalance() {
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

    public void setEndBalance(int endBalance) {
        this.endBalance = endBalance;
    }

    public void setMutation(int mutation) {
        this.mutation = mutation;
    }

    public void setStartBalance(int startBalance) {
        this.startBalance = startBalance;
    }

    public void setTransactionReference(Integer transactionReference) {
        this.transactionReference = transactionReference;
    }
}
