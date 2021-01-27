package nl.rabobank.customerstatementservice.service;

import nl.rabobank.customerstatementservice.dto.CustomerStatementRecordDto;
import nl.rabobank.customerstatementservice.dto.ErrorRecord;
import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.dto.ResultType;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecord;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecordRepository;
import nl.rabobank.customerstatementservice.mapper.CustomerStatementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class CustomerStatementServiceTest {

    private static final String DUPLICATED_RECORD_ACCOUNT_NUMBER = "NL12RABO0987654321";

    private static final String GIVEN_RECORD_ACCOUNT_NUMBER = "NL12RABO0123456789";

    private static final Integer GIVEN_TRANSACTION_REFERENCE = 1234;

    @Mock
    CustomerStatementRecordRepository customerStatementRecordRepository;
    private CustomerStatementService customerStatementService;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        this.customerStatementService = new CustomerStatementService(customerStatementRecordRepository, new CustomerStatementMapper());
    }

    @Test
    void addCustomerStatementRecordWithExceptionHandlingTestAnExceptionResultsInAnInternalServerError() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);
        when(customerStatementRecordRepository.existsById(any()))
                .thenThrow(new IllegalArgumentException("Any exception will do"));

        // When
        ResponseStatusException thrownException = assertThrows(ResponseStatusException.class, () -> customerStatementService.addCustomerStatementRecordWithExceptionHandling(givenRecord));

        // Then
        assertEquals(INTERNAL_SERVER_ERROR, thrownException.getStatus());
        verify(customerStatementRecordRepository).existsById(any());
    }

    @Test
    void addCustomerStatementRecordWithExceptionHandlingTestDuplicateAndIncorrectBalance() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(true);
        CustomerStatementRecord givenDuplicateDatabaseRecord = buildDuplicateDatabaseRecord();
        when(customerStatementRecordRepository.existsById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(true);
        when(customerStatementRecordRepository.findById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(Optional.of(givenDuplicateDatabaseRecord));

        // When
        ResponseBody returnedResponseBody = customerStatementService.addCustomerStatementRecordWithExceptionHandling(givenRecord);

        // Then
        assertEquals(ResultType.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE, returnedResponseBody.getResult());
        assertEquals(2, returnedResponseBody.getErrorRecords().size());

        returnedResponseBody.getErrorRecords().forEach(errorRecord -> {
            assertEquals(GIVEN_TRANSACTION_REFERENCE, errorRecord.getReference());
            // Can't verify accountNumber as we can't be sure about the order and there's no reference to the reason for the errorRecord.
        });

        verify(customerStatementRecordRepository).existsById(GIVEN_TRANSACTION_REFERENCE);
        verify(customerStatementRecordRepository).findById(givenRecord.getTransactionReference());
    }

    @Test
    void addCustomerStatementRecordWithExceptionHandlingTestDuplicateButNotIncorrectBalance() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);
        CustomerStatementRecord givenDuplicateDatabaseRecord = buildDuplicateDatabaseRecord();
        when(customerStatementRecordRepository.existsById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(true);
        when(customerStatementRecordRepository.findById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(Optional.of(givenDuplicateDatabaseRecord));

        // When
        ResponseBody returnedResponseBody = customerStatementService.addCustomerStatementRecordWithExceptionHandling(givenRecord);

        // Then
        assertEquals(ResultType.DUPLICATE_REFERENCE, returnedResponseBody.getResult());
        assertEquals(1, returnedResponseBody.getErrorRecords().size());
        ErrorRecord returnedErrorRecord = returnedResponseBody.getErrorRecords().get(0);

        assertEquals(givenDuplicateDatabaseRecord.getTransactionReference(), returnedErrorRecord.getReference());
        assertEquals(givenDuplicateDatabaseRecord.getAccountNumber(), returnedErrorRecord.getAccountNumber());

        verify(customerStatementRecordRepository).existsById(GIVEN_TRANSACTION_REFERENCE);
        verify(customerStatementRecordRepository).findById(givenRecord.getTransactionReference());
    }

    @Test
    void addCustomerStatementRecordWithExceptionHandlingTestNoDuplicateAndNotIncorrectBalance() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);
        when(customerStatementRecordRepository.existsById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(false);

        // When
        ResponseBody returnedResponseBody = customerStatementService.addCustomerStatementRecordWithExceptionHandling(givenRecord);

        // Then
        assertEquals(ResultType.SUCCESSFUL, returnedResponseBody.getResult());
        assertEquals(0, returnedResponseBody.getErrorRecords().size());

        verify(customerStatementRecordRepository).existsById(GIVEN_TRANSACTION_REFERENCE);
        verify(customerStatementRecordRepository, times(0)).findById(givenRecord.getTransactionReference());
    }

    @Test
    void addCustomerStatementRecordWithExceptionHandlingTestNoDuplicateButIncorrectBalance() {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(true);
        when(customerStatementRecordRepository.existsById(GIVEN_TRANSACTION_REFERENCE))
                .thenReturn(false);

        // When
        ResponseBody returnedResponseBody = customerStatementService.addCustomerStatementRecordWithExceptionHandling(givenRecord);

        // Then
        assertEquals(ResultType.INCORRECT_END_BALANCE, returnedResponseBody.getResult());
        assertEquals(1, returnedResponseBody.getErrorRecords().size());
        ErrorRecord returnedErrorRecord = returnedResponseBody.getErrorRecords().get(0);

        assertEquals(GIVEN_TRANSACTION_REFERENCE, returnedErrorRecord.getReference());
        assertEquals(givenRecord.getAccountNumber(), returnedErrorRecord.getAccountNumber());

        verify(customerStatementRecordRepository).existsById(GIVEN_TRANSACTION_REFERENCE);
        verify(customerStatementRecordRepository, times(0)).findById(givenRecord.getTransactionReference());
    }

    private CustomerStatementRecord buildDuplicateDatabaseRecord() {
        CustomerStatementRecord databaseRecord = new CustomerStatementRecord();

        databaseRecord.setTransactionReference(GIVEN_TRANSACTION_REFERENCE);
        databaseRecord.setAccountNumber(DUPLICATED_RECORD_ACCOUNT_NUMBER);
        databaseRecord.setDescription("beautiful record");
        databaseRecord.setStartBalance(100);
        databaseRecord.setMutation(-10);
        databaseRecord.setEndBalance(90);

        return databaseRecord;
    }

    private CustomerStatementRecordDto buildRecord(boolean incorrectBalance) {
        CustomerStatementRecordDto customerStatementRecordDto = new CustomerStatementRecordDto();

        customerStatementRecordDto.setTransactionReference(GIVEN_TRANSACTION_REFERENCE);
        customerStatementRecordDto.setAccountNumber(GIVEN_RECORD_ACCOUNT_NUMBER);
        customerStatementRecordDto.setDescription("beautiful record");
        customerStatementRecordDto.setStartBalance(100);
        customerStatementRecordDto.setMutation(-10);

        if (incorrectBalance) {
            customerStatementRecordDto.setEndBalance(20);
        } else {
            customerStatementRecordDto.setEndBalance(90);
        }

        return customerStatementRecordDto;
    }
}