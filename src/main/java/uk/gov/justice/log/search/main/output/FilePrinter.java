package uk.gov.justice.log.search.main.output;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePrinter extends OutputPrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(FilePrinter.class);

    private final Path path;

    public FilePrinter(final Path filePath) {
        if (filePath != null) {
            this.path = filePath;
        } else {
            path = Paths.get("results.html");
        }
    }

    @Override
    public void write(final JsonObject message) throws IOException {
        writeToFile(path, message.toString());
    }

    @Override
    public void writeMessages(final JsonObjectBuilder objectBuilder, final Result result) throws IOException {
        write(jsonOf(objectBuilder, result));
    }
}
