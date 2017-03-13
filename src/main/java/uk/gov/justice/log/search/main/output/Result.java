package uk.gov.justice.log.search.main.output;


import net.minidev.json.JSONArray;

public class Result {

    private final String query;
    private final String fromTime;
    private final String toTime;
    private final JSONArray message;
    private final int hits;

    public Result(final String query,
                  final String fromTime,
                  final String toTime,
                  final int hits,
                  final JSONArray message) {
        this.query = query;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.hits = hits;
        this.message = message;
    }

    public int getHits() {
        return hits;
    }

    public String getQuery() {
        return query;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public JSONArray getMessage() {
        return message;
    }
}
