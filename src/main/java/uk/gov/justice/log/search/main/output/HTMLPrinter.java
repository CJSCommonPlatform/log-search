package uk.gov.justice.log.search.main.output;

import static uk.gov.justice.log.utils.SearchConstants.BOLD_BEGIN;
import static uk.gov.justice.log.utils.SearchConstants.BOLD_END;
import static uk.gov.justice.log.utils.SearchConstants.MESSAGE_RESULT;

import uk.gov.justice.log.utils.SearchConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTMLPrinter extends FilePrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(HTMLPrinter.class);

    public HTMLPrinter(final Path filePath) throws IOException {
        super(filePath);
    }

    public String htmlOf(final Result result) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BOLD_BEGIN + "hits:" + BOLD_END).append(result.getHits())
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN + "query:" + BOLD_END).append(result.getQuery() + SearchConstants.HTML_BREAK)
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN + "fromTime" + BOLD_END).append(result.getFromTime() + SearchConstants.HTML_BREAK)
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN + "toTime" + BOLD_END).append(result.getToTime() + SearchConstants.HTML_BREAK
        );
        stringBuilder.append("messages").append(htmlArrayOf(result.getMessage()));
        return stringBuilder.toString();
    }

    protected String htmlArrayOf(final List<String> messageData) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Object message : messageData) {
            stringBuilder.append(SearchConstants.HTML_BREAK + (String) message + SearchConstants.HTML_BREAK);
        }
        return stringBuilder.toString();
    }

    @Override
    public void writeMessages(final Result result) throws IOException {
        try {
            writeToFile(path, MESSAGE_RESULT + htmlOf(result));
        } catch (IOException e) {
            writeException(e);
        }
    }
}
