package nl.rabobank.customerstatementservice;

import nl.rabobank.customerstatementservice.dto.CustomerStatementRecordDto;
import nl.rabobank.customerstatementservice.dto.ErrorRecord;
import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecord;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecordRepository;
import nl.rabobank.customerstatementservice.ui.rest.CustomerStatementController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static nl.rabobank.customerstatementservice.dto.ResultType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerStatementIT {

    private static final String DUPLICATED_RECORD_ACCOUNT_NUMBER = "NL12RABO0987654321";

    private static final String GIVEN_RECORD_ACCOUNT_NUMBER = "NL12RABO0123456789";

    private static final Integer GIVEN_TRANSACTION_REFERENCE = 1234;

    @Autowired
    CustomerStatementController customerStatementController;

    @Autowired
    CustomerStatementRecordRepository customerStatementRecordRepository;

    @BeforeEach
    public void setUp() {
        customerStatementRecordRepository.deleteAll();
    }

    @Test
    void addCustomerStatementRecordTestDuplicateTransactionReference() {
        // Given
        customerStatementRecordRepository.save(buildDuplicateDatabaseRecord());

        CustomerStatementRecordDto givenRecord = buildRecord(false);

        // When
        ResponseBody returnedResponseBody = customerStatementController.addCustomerStatementRecord(givenRecord);

        // Then
        assertEquals(DUPLICATE_REFERENCE, returnedResponseBody.getResult());
        assertEquals(1, returnedResponseBody.getErrorRecords().size());

        ErrorRecord returnedErrorRecord = returnedResponseBody.getErrorRecords().get(0);
        assertEquals(GIVEN_TRANSACTION_REFERENCE, returnedErrorRecord.getReference());
        assertEquals(DUPLICATED_RECORD_ACCOUNT_NUMBER, returnedErrorRecord.getAccountNumber());

        List<CustomerStatementRecord> databaseRecords = customerStatementRecordRepository.findAll();
        assertEquals(1, databaseRecords.size());
    }

    @Test
    void addCustomerStatementRecordTestDuplicateTransactionReferenceAndIncorrectEndBalance() {
        // Given
        customerStatementRecordRepository.save(buildDuplicateDatabaseRecord());

        CustomerStatementRecordDto givenRecord = buildRecord(true);

        // When
        ResponseBody returnedResponseBody = customerStatementController.addCustomerStatementRecord(givenRecord);

        // Then
        assertEquals(DUPLICATE_REFERENCE_INCORRECT_END_BALANCE, returnedResponseBody.getResult());
        assertEquals(2, returnedResponseBody.getErrorRecords().size());

        AtomicInteger givenAccountNumber = new AtomicInteger();
        AtomicInteger duplicateAccountNumber = new AtomicInteger();

        returnedResponseBody.getErrorRecords().forEach(errorRecord -> {
            assertEquals(GIVEN_TRANSACTION_REFERENCE, errorRecord.getReference());

            if (GIVEN_RECORD_ACCOUNT_NUMBER.equals(errorRecord.getAccountNumber())) {
                givenAccountNumber.incrementAndGet();
            }
            if (DUPLICATED_RECORD_ACCOUNT_NUMBER.equals(errorRecord.getAccountNumber())) {
                duplicateAccountNumber.incrementAndGet();
            }
        });

        assertEquals(1, givenAccountNumber.get());
        assertEquals(1, duplicateAccountNumber.get());

        List<CustomerStatementRecord> databaseRecords = customerStatementRecordRepository.findAll();
        assertEquals(1, databaseRecords.size());
    }

    @Test
    void addCustomerStatementRecordTestIncorrectEndBalance() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(true);

        // When
        ResponseBody returnedResponseBody = customerStatementController.addCustomerStatementRecord(givenRecord);

        // Then
        assertEquals(INCORRECT_END_BALANCE, returnedResponseBody.getResult());
        assertEquals(1, returnedResponseBody.getErrorRecords().size());

        ErrorRecord returnedErrorRecord = returnedResponseBody.getErrorRecords().get(0);
        assertEquals(GIVEN_TRANSACTION_REFERENCE, returnedErrorRecord.getReference());
        assertEquals(GIVEN_RECORD_ACCOUNT_NUMBER, returnedErrorRecord.getAccountNumber());

        List<CustomerStatementRecord> databaseRecords = customerStatementRecordRepository.findAll();
        assertEquals(0, databaseRecords.size());
    }

    @Test
    void addCustomerStatementRecordTestSuccessful() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);

        // When
        ResponseBody returnedResponseBody = customerStatementController.addCustomerStatementRecord(givenRecord);

        // Then
        assertEquals(SUCCESSFUL, returnedResponseBody.getResult());
        assertEquals(0, returnedResponseBody.getErrorRecords().size());

        List<CustomerStatementRecord> databaseRecords = customerStatementRecordRepository.findAll();
        assertEquals(1, databaseRecords.size());

        CustomerStatementRecord insertedRecord = databaseRecords.get(0);

        assertEquals(givenRecord.getTransactionReference(), insertedRecord.getTransactionReference());
        assertEquals(givenRecord.getAccountNumber(), insertedRecord.getAccountNumber());
        assertEquals(givenRecord.getStartBalance(), insertedRecord.getStartBalance());
        assertEquals(givenRecord.getMutation(), insertedRecord.getMutation());
        assertEquals(givenRecord.getEndBalance(), insertedRecord.getEndBalance());
        assertEquals(givenRecord.getDescription(), insertedRecord.getDescription());
    }

    private CustomerStatementRecord buildDuplicateDatabaseRecord() {
        CustomerStatementRecord databaseRecord = new CustomerStatementRecord();

        databaseRecord.setTransactionReference(GIVEN_TRANSACTION_REFERENCE);
        databaseRecord.setAccountNumber(DUPLICATED_RECORD_ACCOUNT_NUMBER);
        databaseRecord.setDescription("beautiful record");
        databaseRecord.setStartBalance(1000);
        databaseRecord.setMutation(-102);
        databaseRecord.setEndBalance(898);

        return databaseRecord;
    }

    private CustomerStatementRecordDto buildRecord(boolean incorrectEndBalance) {
        CustomerStatementRecordDto customerStatementRecordDto = new CustomerStatementRecordDto();

        customerStatementRecordDto.setTransactionReference(GIVEN_TRANSACTION_REFERENCE);
        customerStatementRecordDto.setAccountNumber(GIVEN_RECORD_ACCOUNT_NUMBER);
        customerStatementRecordDto.setDescription("beautiful record");
        customerStatementRecordDto.setStartBalance(100);
        customerStatementRecordDto.setMutation(-10);

        if (incorrectEndBalance) {
            customerStatementRecordDto.setEndBalance(110);
        } else {
            customerStatementRecordDto.setEndBalance(90);
        }

        return customerStatementRecordDto;
    }
}
