package uk.gov.justice.log.search.main.output;

import static uk.gov.justice.log.utils.CommonConstant.NEW_LINE;

import net.minidev.json.JSONArray;

public abstract class OutputPrinter {

    public abstract void write(final String message);

    public abstract void writeMessages(final String query, final String fromTime, final String toTime,
                                       final String hits, final JSONArray messageData);

    public void writeStackTrace(final Exception exception) {
        exception.printStackTrace();
    }

    public void writeException(final Throwable throwable) {
        System.err.println(throwable.getMessage());
    }

    protected String jsonStringOf(final JSONArray messageData) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (Object message : messageData) {
            stringBuilder.append(message).append(NEW_LINE);
        }
        return stringBuilder.toString();
    }
}
