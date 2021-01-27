package nl.rabobank.customerstatementservice.dto;

/**
 * TODO
 */
public enum ResultType {

    SUCCESSFUL,
    DUPLICATE_REFERENCE,
    INCORRECT_END_BALANCE,
    DUPLICATE_REFERENCE_INCORRECT_END_BALANCE,
    BAD_REQUEST,
    INTERNAL_SERVER_ERROR
}
