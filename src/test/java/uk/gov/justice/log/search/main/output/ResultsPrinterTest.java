package uk.gov.justice.log.search.main.output;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import net.minidev.json.JSONArray;
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

        final JSONArray messageData = new JSONArray();
        final Result result = new Result("query", "fromTime", "toTime", 10, messageData);
        resultsPrinter.outputPrinters = printers;
        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        resultsPrinter.printResults(jsonObjectBuilder, result);

        verify(filePrinter).writeMessages(jsonObjectBuilder, result);
        verify(consolePrinter).writeMessages(jsonObjectBuilder, result);
    }

    @Test
    public void shouldPrintExceptions() throws IOException {
        final List<OutputPrinter> printers = new ArrayList<>();

        printers.add(consolePrinter);
        printers.add(filePrinter);

        final ResultsPrinter resultsPrinter = new ResultsPrinter(printers);

        final Exception exception = new Exception();
        resultsPrinter.printException(exception);

        verify(filePrinter).writeException(exception);
        verify(consolePrinter).writeException(exception);
    }
}