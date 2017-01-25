package uk.gov.justice.log.search.main.output;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static uk.gov.justice.log.utils.CommonConstant.NEW_LINE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePrinter extends OutputPrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(FilePrinter.class);

    private final Path path;

    public FilePrinter(final String filePath) {
        if (filePath != null) {
            this.path = Paths.get(filePath);
        } else {
            path = Paths.get("results.log");
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
    public void writeMessages(final String query, final String fromTime, final String toTime, final String hits, final JSONArray messageData) {
        write(NEW_LINE + "Search From : " + fromTime + NEW_LINE +
                "Search To : " + toTime + NEW_LINE +
                query + NEW_LINE +
                hits + NEW_LINE +
                jsonStringOf(messageData));
    }

}
