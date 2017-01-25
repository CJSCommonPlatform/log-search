package uk.gov.justice.log.utils;


public class CommonConstant {

    public static final String ELASTIC_SEARCH_CLUSTER_URL = "/elasticsearch/";
    public static final String ELASTIC_MULTI_SEARCH_URL = ELASTIC_SEARCH_CLUSTER_URL + "_msearch";

    public static final int MINS_TO_MILLIS_MULTIPLIER = 60000;
    public static final int MAX_RETRY_TIMEOUT_MILLIS = 60000;
    public static final int DEFAULT_RESPONSE_OUTPUT_SIZE = 100;

    public static final String NEW_LINE = "\n";
    public static final String HTML_BREAK = "<BR>";
    public static final String BOLD_BEGIN = "<B>";
    public static final String BOLD_END = "</B>";
    public static final String POST = "POST";
    public static final String PUT = "PUT";

    private CommonConstant() {
        throw new IllegalAccessError("Utility class");
    }
}
