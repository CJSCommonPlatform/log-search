package uk.gov.justice.log.search.main.output;


import static uk.gov.justice.log.utils.SearchConstants.FROM_TIME;
import static uk.gov.justice.log.utils.SearchConstants.HITS;
import static uk.gov.justice.log.utils.SearchConstants.QUERY;
import static uk.gov.justice.log.utils.SearchConstants.TO_TIME;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;


public abstract class OutputPrinter implements Printer {

    public String jsonOf(final Result result) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add(HITS, result.getHits())
                .add(QUERY, result.getQuery())
                .add(FROM_TIME, result.getFromTime())
                .add(TO_TIME, result.getToTime());
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
