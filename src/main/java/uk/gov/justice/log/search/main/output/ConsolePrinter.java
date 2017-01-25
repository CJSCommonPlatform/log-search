package uk.gov.justice.log.search.main.output;

import net.minidev.json.JSONArray;

public class ConsolePrinter extends OutputPrinter {

    @Override
    public void write(final String message) {
        System.out.println(message);
    }

    @Override
    public void writeMessages(final String query, final String fromTime, final String toTime,
                              final String hits, final JSONArray messageData) {
        System.out.println("Search From : " + fromTime);
        System.out.println("Search To : " + toTime);
        System.out.println("Query: " + query);
        System.out.println("Hits: " + hits);
        System.out.println(jsonStringOf(messageData));
    }


}
