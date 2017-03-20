package uk.gov.justice.log.search.main.output;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultsPrinterTest {

    @Mock
    ConsolePrinter consolePrinter;

    @Mock
    FilePrinter filePrinter;

    @InjectMocks
    ResultsPrinter resultsPrinter;

    @Test
    public void shouldPrintResults() throws IOException {
        final List<OutputPrinter> printers = new ArrayList<>();
        printers.add(consolePrinter);
        printers.add(filePrinter);

        final List<String> messageData = new ArrayList<>();
        final Result result = new Result("query", "fromTime", "toTime", 10, messageData);
        final List<Result> results = new ArrayList<>();
        results.add(result);
        resultsPrinter.outputPrinters = printers;
        resultsPrinter.printResults(results);

        verify(filePrinter).writeMessages(result);
        verify(consolePrinter).writeMessages(result);
    }

    @Test
    public void shouldPrintExceptions() throws IOException {
        final List<OutputPrinter> printers = new ArrayList<>();

        printers.add(consolePrinter);
        printers.add(filePrinter);

        final ResultsPrinter resultsPrinter = new ResultsPrinter(printers);

        final IOException exception = new IOException();
        resultsPrinter.printException(exception);

        verify(filePrinter).writeException(exception);
        verify(consolePrinter).writeException(exception);
    }
}