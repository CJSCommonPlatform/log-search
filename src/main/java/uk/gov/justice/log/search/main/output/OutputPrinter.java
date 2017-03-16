package uk.gov.justice.log.search.main.output;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;


public abstract class OutputPrinter implements Printer {

    protected void writeToFile(final Path path, final String message) throws IOException {
        final OpenOption[] options = new OpenOption[]{CREATE, TRUNCATE_EXISTING};
        Files.write(path, message.getBytes(), options);
    }

    public String jsonOf(final Result result) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("hits", result.getHits())
                .add("query", result.getQuery())
                .add("fromTime", result.getFromTime())
                .add("toTime", result.getToTime());
        final JsonArrayBuilder messages = Json.createArrayBuilder();
        jsonObjectBuilder.add("messages", jsonArrayOf(messages, result.getMessage()));
        return jsonObjectBuilder.build().toString();
    }

    protected JsonArray jsonArrayOf(final JsonArrayBuilder messages,
                                    final List<String> messageData) {
        for (Object message : messageData) {
            messages.add((String) message);
        }
        return messages.build();
    }
}
