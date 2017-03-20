package uk.gov.justice.log.factory;

import uk.gov.justice.log.search.main.output.ConsolePrinter;
import uk.gov.justice.log.search.main.output.FilePrinter;
import uk.gov.justice.log.search.main.output.HTMLPrinter;
import uk.gov.justice.log.search.main.output.OutputPrinter;
import uk.gov.justice.log.search.main.output.ResultsPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResultsPrinterFactory {

    private final Path responsePath;

    public ResultsPrinterFactory(final Path responsePath) {
        this.responsePath = responsePath;
    }

    public ResultsPrinter createResultsPrinters(final String displayConsoleMessages) throws IOException {
        final List<OutputPrinter> printerList = new ArrayList();
        final ResultsPrinter resultsPrinter = new ResultsPrinter(printerList);
        printerList.add(new ConsolePrinter(displayConsoleMessages));
        printerList.add(outputPrinter());
        return resultsPrinter;
    }

    private OutputPrinter outputPrinter() throws IOException {
        if (responsePath != null && responsePath.endsWith(".txt")) {
            return new FilePrinter(responsePath);
        } else {
            return new HTMLPrinter(responsePath);
        }
    }
}
