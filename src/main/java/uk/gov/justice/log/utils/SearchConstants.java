package uk.gov.justice.log.utils;


import java.time.Instant;

public final class SearchConstants {
    public static final String ELASTIC_SEARCH_ALL = "/_all";
    public static final String ELASTIC_SEARCH_CLUSTER_URL = "/elasticsearch/";
    public static final String ELASTIC_MULTI_SEARCH_URL = ELASTIC_SEARCH_CLUSTER_URL + "_msearch";

    public static final int MINS_TO_MILLIS_MULTIPLIER = 60000;
    public static final int MAX_RETRY_TIMEOUT_MILLIS = 60000;
    public static final int DEFAULT_RESPONSE_OUTPUT_SIZE = 100;
    public static final int MAX_CONNECTIONS = 50;

    public static final String NEW_LINE = "\n";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    public static final String RESPONSE_HITS = "$.responses[0].hits.total";
    public static final String RESPONSE_MESSAGES = "$.responses[0].hits..message";

    public static final String HTML_BREAK = "<BR>";
    public static final String BOLD_BEGIN = "<B>";
    public static final String BOLD_END = "</B>";
    public static final String RESULTS_HTML = "results.html";

    public static final String YES = "yes";
    public static final String MESSAGE_RESULT = "Result:";

    private SearchConstants() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Used for mocking now values
     */
    public static class InstantGenerator {

        /**
         * Now instant.
         *
         * @return the instant
         */
        public Instant now() {
            return Instant.now();
        }
    }
}
