package nl.rabobank.customerstatementservice.dto;

import java.util.ArrayList;
import java.util.List;

public class ResponseBody {

    private final ResultType result;

    private final List<ErrorRecord> errorRecords;

    public ResponseBody(ResultType result) {
        this(result, new ArrayList<>());
    }

    public ResponseBody(ResultType result, List<ErrorRecord> errorRecords) {
        this.result = result;
        this.errorRecords = errorRecords;
    }

    @Override
    public String toString() {
        return "ResponseBody{" +
                "result=" + result +
                ", errorRecords=" + errorRecords +
                '}';
    }

    public ResultType getResult() {
        return result;
    }

    public List<ErrorRecord> getErrorRecords() {
        return errorRecords;
    }
}
