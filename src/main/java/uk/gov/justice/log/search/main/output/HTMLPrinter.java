package uk.gov.justice.log.search.main.output;

import static uk.gov.justice.log.utils.SearchConstants.BOLD_BEGIN;
import static uk.gov.justice.log.utils.SearchConstants.BOLD_END;
import static uk.gov.justice.log.utils.SearchConstants.RESULTS_HTML;

import uk.gov.justice.log.utils.SearchConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HTMLPrinter extends OutputPrinter {

    private final Logger LOGGER = LoggerFactory.getLogger(HTMLPrinter.class);

    private final Path path;

    public HTMLPrinter(final Path filePath) {
        if (filePath != null) {
            this.path = filePath;
        } else {
            path = Paths.get(RESULTS_HTML);
        }
    }

    @Override
    public void write(final JsonObject message) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            final String htmlMessage = ow.writeValueAsString(message.toString());
            writeToFile(path, htmlMessage);
        } catch (IOException exception) {
            writeStackTrace(exception);
        }
    }



    public String htmlOf(final Result result) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BOLD_BEGIN +"hits:"+BOLD_END).append(result.getHits())
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN + "query:" +BOLD_END).append(result.getQuery() + SearchConstants.HTML_BREAK)
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN  + "fromTime" + BOLD_END).append(result.getFromTime() + SearchConstants.HTML_BREAK)
                .append(SearchConstants.HTML_BREAK +
                        BOLD_BEGIN + "toTime" + BOLD_END).append(result.getToTime() + SearchConstants.HTML_BREAK
                );
        stringBuilder.append("messages").append(htmlArrayOf( result.getMessage()));
        return stringBuilder.toString();
    }

    protected String htmlArrayOf(final JSONArray messageData) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Object message : messageData) {
            stringBuilder.append(SearchConstants.HTML_BREAK + (String) message + SearchConstants.HTML_BREAK);
        }
        return stringBuilder.toString();
    }

    @Override
    public void writeMessages(final JsonObjectBuilder objectBuilder, final Result result) {
        write(Json.createObjectBuilder().add("Result" ,htmlOf(result)).build());
    }
}
