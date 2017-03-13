package uk.gov.justice.log.search.main.output;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import net.minidev.json.JSONArray;


public abstract class OutputPrinter {

    public abstract void write(final JsonObject message) throws IOException;

    public abstract void writeMessages(final JsonObjectBuilder objectBuilder, final Result result) throws IOException;

    public void writeStackTrace(final Exception exception) {
        exception.printStackTrace();
    }

    public void writeException(final Throwable throwable) {
        System.err.println(throwable.getMessage());
    }

    public JsonObject jsonOf(final JsonObjectBuilder jsonObjectBuilder,
                             final Result result) {
        jsonObjectBuilder.add("hits", result.getHits())
                .add("query", result.getQuery())
                .add("fromTime", result.getFromTime())
                .add("toTime", result.getToTime());
        final JsonArrayBuilder messages = Json.createArrayBuilder();
        jsonObjectBuilder.add("messages", jsonArrayOf(messages, result.getMessage()));
        return jsonObjectBuilder.build();
    }

    protected JsonArray jsonArrayOf(final JsonArrayBuilder messages,
                                    final JSONArray messageData) {
        for (Object message : messageData) {
            messages.add((String) message);
        }
        return messages.build();
    }

    protected void writeToFile(final Path path, final String htmlMessage) throws IOException {
        final OpenOption[] options = new OpenOption[]{CREATE, TRUNCATE_EXISTING};
        Files.write(path, htmlMessage.getBytes(), options);
    }
}
