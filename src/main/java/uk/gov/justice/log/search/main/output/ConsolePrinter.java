package uk.gov.justice.log.search.main.output;

import static uk.gov.justice.log.utils.SearchConstants.MESSAGE_RESULT;
import static uk.gov.justice.log.utils.SearchConstants.YES;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class ConsolePrinter extends OutputPrinter {
    private String displayConsoleMessages = YES;

    public ConsolePrinter(final String displayConsoleMessages) {
        if (displayConsoleMessages != null) {
            this.displayConsoleMessages = displayConsoleMessages.trim().toLowerCase();
        }
    }

    @Override
    public void write(final JsonObject message) {
        System.out.println(message);
    }

    @Override
    public void writeMessages(final JsonObjectBuilder objectBuilder,
                              final Result result) {
        if (displayConsoleMessages.equalsIgnoreCase(YES)) {
            System.out.println(MESSAGE_RESULT + jsonOf(objectBuilder, result));
        } else {
            System.out.println("Hits: " + result.getHits());
        }
    }
}
