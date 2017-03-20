package uk.gov.justice.log.search.main.output;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilePrinter extends OutputPrinter {

    protected final Path path;
    private final Logger LOGGER = LoggerFactory.getLogger(FilePrinter.class);

    public FilePrinter(final Path filePath) throws IOException {
        if (filePath != null) {
            this.path = filePath;
        } else {
            path = Paths.get("results.html");
        }
        Files.deleteIfExists(path);
    }

    @Override
    public void writeMessages(final Result result) throws IOException {
        try {
            writeToFile(path, jsonOf(result));
        } catch (IOException e) {
            writeException(e);
        }
    }

    protected void writeToFile(final Path path, final String message) throws IOException {
        final OpenOption[] options = new OpenOption[]{CREATE, APPEND};
        Files.write(path, message.getBytes(), options);
    }

    @Override
    public void writeException(final IOException exception) throws IOException {
        try {
            writeToFile(path, exception.getMessage());
        } catch (IOException e) {
            System.err.println(exception);
            throw exception;
        }
    }
}

