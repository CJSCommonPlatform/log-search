package uk.gov.justice.log.search.main.output;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static uk.gov.justice.log.utils.CommonConstant.BOLD_BEGIN;
import static uk.gov.justice.log.utils.CommonConstant.BOLD_END;
import static uk.gov.justice.log.utils.CommonConstant.HTML_BREAK;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTMLPrinter extends OutputPrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(HTMLPrinter.class);

    private final Path path;

    public HTMLPrinter(final String filePath) {
        if (filePath != null) {
            this.path = Paths.get(filePath);
        } else {
            path = Paths.get("results.html");
        }
    }

    @Override
    public void write(final String message) {
        try {
            final OpenOption[] options = new OpenOption[]{CREATE, TRUNCATE_EXISTING};
            Files.write(path, message.getBytes(), options);
        } catch (IOException exception) {
            writeStackTrace(exception);
        }
    }

    @Override
    public void writeMessages(final String query, final String fromTime, final String toTime,
                              final String hits, final JSONArray messageData) {
        write(HTML_BREAK + BOLD_BEGIN + "Search From : " + fromTime + BOLD_END + HTML_BREAK + BOLD_BEGIN +
                "Search To : " + toTime + BOLD_END + HTML_BREAK + BOLD_BEGIN +
                "Query:" + query + BOLD_END + HTML_BREAK + BOLD_BEGIN +
                "Hits: " + hits + HTML_BREAK + BOLD_END +
                jsonStringOf(messageData));
    }

    protected String jsonStringOf(final JSONArray messageData) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Object message : messageData) {
            stringBuilder.append(HTML_BREAK).append(message).append(HTML_BREAK);
        }
        return stringBuilder.toString();
    }
}
