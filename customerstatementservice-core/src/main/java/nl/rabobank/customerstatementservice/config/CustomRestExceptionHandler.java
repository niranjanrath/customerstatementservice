package nl.rabobank.customerstatementservice.config;

import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.dto.ResultType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This class is needed to return the requested response within a 400 or 500 HttpStatus.
 * This could've been handled by Spring itself, using the default response body.
 */
@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return createBadRequestResponse();
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return createBadRequestResponse();
    }

    /**
     * This method is to comply to the request that any internal exception must be returned with a specific body.
     *
     * @param ex
     * @return HttpStatus 500 with specific body format.
     */
    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatus() != HttpStatus.INTERNAL_SERVER_ERROR) {
            // This is to make it a little more future proof. If ResponseStatusException is used without a 500, it'll be handled as normal.
            throw ex;
        }

        ResponseBody responseBody = new ResponseBody(ResultType.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseBody);
    }

    private ResponseEntity<Object> createBadRequestResponse() {
        ResponseBody responseBody = new ResponseBody(ResultType.BAD_REQUEST);

        return ResponseEntity.badRequest()
                .body(responseBody);
    }
}
