package uk.gov.justice.log.search.main.output;

import static uk.gov.justice.log.utils.SearchConstants.MESSAGE_RESULT;
import static uk.gov.justice.log.utils.SearchConstants.YES;

import java.io.IOException;


public class ConsolePrinter extends OutputPrinter {
    private String displayConsoleMessages = YES;

    public ConsolePrinter(final String displayConsoleMessages) {
        if (displayConsoleMessages != null) {
            this.displayConsoleMessages = displayConsoleMessages.trim().toLowerCase();
        }
    }

    @Override
    public void writeMessages(final Result result) {
        if (displayConsoleMessages.equalsIgnoreCase(YES)) {
            System.out.println(MESSAGE_RESULT + jsonOf(result));
        } else {
            System.out.println("Hits: " + result.getHits());
        }
    }

    @Override
    public void writeException(IOException ioException) throws IOException {
        System.err.println(ioException);
        throw ioException;
    }
}
