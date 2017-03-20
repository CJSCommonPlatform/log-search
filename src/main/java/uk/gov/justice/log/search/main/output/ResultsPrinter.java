package uk.gov.justice.log.search.main.output;

import java.io.IOException;
import java.util.List;

public class ResultsPrinter {

    protected List<OutputPrinter> outputPrinters;

    public ResultsPrinter(final List<OutputPrinter> outputPrinters) {
        this.outputPrinters = outputPrinters;
    }

    public void printResults(final List<Result> results) throws IOException {
        print(results);

    }

    private void print(final List<Result> results) throws IOException {
        for (OutputPrinter printer : outputPrinters) {
            for (Result result : results) {
                printer.writeMessages(result);
            }
        }
    }

    public void printException(final IOException exception) throws IOException {
        for (OutputPrinter printer : outputPrinters) {
            printer.writeException(exception);
        }
    }
}
