package nl.rabobank.customerstatementservice.ui.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rabobank.customerstatementservice.config.CustomRestExceptionHandler;
import nl.rabobank.customerstatementservice.dto.CustomerStatementRecordDto;
import nl.rabobank.customerstatementservice.dto.ResponseBody;
import nl.rabobank.customerstatementservice.dto.ResultType;
import nl.rabobank.customerstatementservice.service.CustomerStatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerStatementControllerTest {

    @InjectMocks
    private CustomerStatementController customerStatementController;

    @Mock
    private CustomerStatementService customerStatementService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        // We would need this line if we would not use MockitoJUnitRunner
        // MockitoAnnotations.initMocks(this);
        // Initializes the JacksonTester
        JacksonTester.initFields(this, new ObjectMapper());
        // MockMvc standalone approach
        mockMvc = MockMvcBuilders.standaloneSetup(customerStatementController)
                .setControllerAdvice(new CustomRestExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void addCustomerStatementRecordTestCorrectInputResultsInOk() throws Exception {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);

        when(customerStatementService.addCustomerStatementRecordWithExceptionHandling(any()))
                .thenReturn(new ResponseBody(ResultType.SUCCESSFUL));

        // When
        mockMvc.perform(post("/v1/customerstatements")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsBytes(givenRecord)))
                // Then
                .andExpect(status().isOk())
                .andExpect(content().string("{\"result\":\"SUCCESSFUL\",\"errorRecords\":[]}"));

        verify(customerStatementService).addCustomerStatementRecordWithExceptionHandling(any());
    }

    @Test
    void addCustomerStatementRecordTestExceptionResultsInInternalServerError() throws Exception {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(false);

        when(customerStatementService.addCustomerStatementRecordWithExceptionHandling(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        mockMvc.perform(post("/v1/customerstatements")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsBytes(givenRecord)))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"result\":\"INTERNAL_SERVER_ERROR\",\"errorRecords\":[]}"));

        verify(customerStatementService).addCustomerStatementRecordWithExceptionHandling(any());
    }

    @Test
    void addCustomerStatementRecordTestInvalidInputResultsInBadRequest() throws Exception {
        // Given
        CustomerStatementRecordDto givenRecord = buildRecord(true);

        // When
        mockMvc.perform(post("/v1/customerstatements")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsBytes(givenRecord)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"result\":\"BAD_REQUEST\",\"errorRecords\":[]}"));

        verify(customerStatementService, times(0)).addCustomerStatementRecordWithExceptionHandling(any());
    }

    private CustomerStatementRecordDto buildRecord(boolean badRequest) {
        CustomerStatementRecordDto customerStatementRecordDto = new CustomerStatementRecordDto();
        if (badRequest) {
            return customerStatementRecordDto;
        }

        customerStatementRecordDto.setTransactionReference(1234);
        customerStatementRecordDto.setAccountNumber("NL12RABO0123456789");
        customerStatementRecordDto.setDescription("beautiful record");
        customerStatementRecordDto.setStartBalance(100);
        customerStatementRecordDto.setMutation(-10);
        customerStatementRecordDto.setEndBalance(90);

        return customerStatementRecordDto;
    }

}