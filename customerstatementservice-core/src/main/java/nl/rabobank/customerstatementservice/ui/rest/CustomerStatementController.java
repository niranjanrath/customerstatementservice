package nl.rabobank.customerstatementservice.ui.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.rabobank.customerstatementservice.dto.CustomerStatementRecordDto;
import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.service.CustomerStatementService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/customerstatements")
@Api(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerStatementController {

    private final CustomerStatementService customerStatementService;

    public CustomerStatementController(CustomerStatementService customerStatementService) {
        this.customerStatementService = customerStatementService;
    }

    @ApiOperation("Adds a new customer statement record.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Either added the record, or came across inconsistent data."),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Failure")
    })
    @PostMapping
    public ResponseBody addCustomerStatementRecord(@Valid @RequestBody CustomerStatementRecordDto customerStatementRecordDto) {

        return customerStatementService.addCustomerStatementRecordWithExceptionHandling(customerStatementRecordDto);
    }
}
