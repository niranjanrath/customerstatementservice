package nl.rabobank.customerstatementservice.service;

import nl.rabobank.customerstatementservice.dto.CustomerStatementRecordDto;
import nl.rabobank.customerstatementservice.dto.ErrorRecord;
import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.dto.ResultType;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecord;
import nl.rabobank.customerstatementservice.infrastructure.CustomerStatementRecordRepository;
import nl.rabobank.customerstatementservice.mapper.CustomerStatementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static java.util.Arrays.asList;

@Service
public class CustomerStatementService {

    private final CustomerStatementRecordRepository customerStatementRecordRepository;

    private final CustomerStatementMapper customerStatementMapper;

    private final Logger log = LoggerFactory.getLogger(CustomerStatementService.class);

    public CustomerStatementService(CustomerStatementRecordRepository customerStatementRecordRepository,
                                    CustomerStatementMapper customerStatementMapper) {
        this.customerStatementRecordRepository = customerStatementRecordRepository;
        this.customerStatementMapper = customerStatementMapper;
    }

    /**
     * Adds a customer statement record to the database after verifying it's valid. If not, the record won't be saved.
     * <p>
     * As to not clutter the actual business logic, the overall catch is separated from the business logic.
     *
     * @param recordDto contains all information
     * @return Information regarding the handling of the record, or an exception if something went wrong.
     */
    public ResponseBody addCustomerStatementRecordWithExceptionHandling(CustomerStatementRecordDto recordDto) {
        try {
            return addCustomerStatementRecord(recordDto);
        } catch (Exception e) {
            log.info("Unknown exception thrown when adding customer statement record; {}", recordDto, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a given customer statement record to the database, after verifying a few things first.
     * If a verification fails, the record won't be inserted in the database.
     *
     * @param recordDto Given information
     * @return ResponseBody containing the way the record has been handled.
     */
    private ResponseBody addCustomerStatementRecord(CustomerStatementRecordDto recordDto) {

        boolean duplicateReference = customerStatementRecordRepository.existsById(recordDto.getTransactionReference());
        boolean incorrectEndBalance = checkEndBalanceIncorrect(recordDto.getStartBalance(), recordDto.getMutation(), recordDto.getEndBalance());

        ResponseBody responseBody;

        // Expecting most records to be correct, thus checking for that first;
        if (!duplicateReference && !incorrectEndBalance) {
            CustomerStatementRecord toBeInsertedRecord = customerStatementMapper.toCustomerStatementRecord(recordDto);

            log.debug("Inserting the following record in the database: {}", toBeInsertedRecord);
            customerStatementRecordRepository.save(toBeInsertedRecord);
            log.debug("Successfully inserted the following record in the database: {}", toBeInsertedRecord);

            responseBody = new ResponseBody(ResultType.SUCCESSFUL);
        } else if (!duplicateReference) {
            log.debug("To be inserted record had an incorrect end balance. Won't insert it into the database: {}", recordDto);
            ErrorRecord errorRecord = new ErrorRecord(recordDto.getTransactionReference(), recordDto.getAccountNumber());

            responseBody = new ResponseBody(ResultType.INCORRECT_END_BALANCE, Collections.singletonList(errorRecord));
        } else if (!incorrectEndBalance) {
            log.debug("Transaction reference of the to be inserted record already existed. Won't insert it into the database: {}", recordDto);
            String accountNumberFromExistingRecord = getAccountNumberFromExistingRecord(recordDto.getTransactionReference());
            ErrorRecord errorRecord = new ErrorRecord(recordDto.getTransactionReference(), accountNumberFromExistingRecord);

            responseBody = new ResponseBody(ResultType.DUPLICATE_REFERENCE, Collections.singletonList(errorRecord));
        } else {
            log.debug("To be inserted record had an incorrect end balance & reference already exists in database. Won't insert it into the database: {}", recordDto);
            Integer transactionReference = recordDto.getTransactionReference();
            String accountNumberFromExistingRecord = getAccountNumberFromExistingRecord(recordDto.getTransactionReference());

            ErrorRecord duplicateReferenceError = new ErrorRecord(transactionReference, recordDto.getAccountNumber());
            ErrorRecord incorrectEndBalanceError = new ErrorRecord(transactionReference, accountNumberFromExistingRecord);

            responseBody = new ResponseBody(ResultType.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE, asList(duplicateReferenceError, incorrectEndBalanceError));
        }

        return responseBody;
    }

    private boolean checkEndBalanceIncorrect(int startBalance, int mutation, int expectedEndBalance) {
        return (startBalance + mutation) != expectedEndBalance;
    }

    private String getAccountNumberFromExistingRecord(Integer transactionReference) {
        return customerStatementRecordRepository.findById(transactionReference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
                .getAccountNumber();
    }
}
