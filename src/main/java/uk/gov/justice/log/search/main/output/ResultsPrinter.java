package uk.gov.justice.log.search.main.output;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class ResultsPrinter {

    protected List<OutputPrinter> outputPrinters;

    public ResultsPrinter(final List<OutputPrinter> outputPrinters) {
        this.outputPrinters = outputPrinters;
    }

    public void printResults(final JsonObjectBuilder jsonObjectBuilder,final Result result) {
        try {
            print(jsonObjectBuilder,result);
        } catch (IOException exception) {
            printException(exception);
        }
    }

    private void print(final JsonObjectBuilder jsonObjectBuilder,final Result result) throws IOException {
        for (OutputPrinter printer : outputPrinters) {
            printer.writeMessages(jsonObjectBuilder,result);
        }
    }

    public void printException(final Exception exception) {
        for (OutputPrinter printer : outputPrinters) {
            printer.writeException(exception);
        }
    }
}
