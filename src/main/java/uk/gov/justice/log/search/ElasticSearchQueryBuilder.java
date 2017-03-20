package uk.gov.justice.log.search;


import static java.time.Instant.ofEpochMilli;
import static java.time.Instant.parse;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.log.utils.SearchConstants.MINS_TO_MILLIS_MULTIPLIER;
import static uk.gov.justice.log.utils.SearchConstants.NEW_LINE;

import uk.gov.justice.log.utils.SearchConstants;

import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class ElasticSearchQueryBuilder {
    private static final String FROM = "gte";
    private static final String TO = "lte";
    private static final String RANGE = "range";
    private static final String QUERY_KEY = "query";
    private static final String REGEXP = "regexp";
    private static final String MUST = "must";
    private static final String SHOULD = "should";
    private static final String BOOL = "bool";
    private static final String SIZE = "size";
    private static final String CONSTANT_SCORE = "constant_score";
    private static final String FILTER = "filter";
    private static final String TIMESTAMP_FIELD = "@timestamp";
    private static final String MESSAGE_FIELD = "message";
    private final SearchCriteria searchCriteria;
    private SearchConstants.InstantGenerator instantGenerator = new SearchConstants.InstantGenerator();

    private List<String> queries = new LinkedList<>();

    public ElasticSearchQueryBuilder(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    private void getKeywordsAndRegexesToQuery() {
        final JsonArrayBuilder regexpArrayBuilder = Json.createArrayBuilder();
        final List<String> regexes = searchCriteria.getRegexes();
        if (regexes != null && !regexes.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String regex : regexes) {
                regexpArrayBuilder.add(regexp(MESSAGE_FIELD, ".*" + regex + ".*"));
                stringBuilder.append(multiSearchQuery(header(), body(regexpArrayBuilder.build())));
                queries.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }
        final List<String> keywords = searchCriteria.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String keyword : keywords) {
                regexpArrayBuilder.add(regexp(MESSAGE_FIELD, ".*\"" + keyword + "\".*"));
                stringBuilder.append(multiSearchQuery(header(), body(regexpArrayBuilder.build())));
                queries.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }
    }

    private JsonObject range() {
        long searchFrom;
        long searchTo;

        if (searchCriteria.isFromToTimeSet()) {
            final String fromStr = searchCriteria.getFromTime();
            final String toStr = searchCriteria.getToTime();
            searchFrom = parse(fromStr).toEpochMilli();
            searchTo = parse(toStr).toEpochMilli();
        } else {
            final int rangeDuration = searchCriteria.getDurationMinutes();
            searchFrom = instantGenerator.now().minusMillis(rangeDuration * MINS_TO_MILLIS_MULTIPLIER).toEpochMilli();
            searchTo = instantGenerator.now().toEpochMilli();
            searchCriteria.setFromTime(ofEpochMilli(searchFrom).toString());
            searchCriteria.setToTime(ofEpochMilli(searchTo).toString());
        }
        final JsonObject timeStampRange = createObjectBuilder().add(FROM, searchFrom).add(TO, searchTo).build();
        final JsonObject timestamp = createObjectBuilder().add(TIMESTAMP_FIELD, timeStampRange).build();

        return createObjectBuilder().add(RANGE, timestamp).build();
    }

    private JsonObject regexp(final String field, final String regex) {
        final JsonObject regexJsonObj = createObjectBuilder().add(field, regex).build();
        return createObjectBuilder().add(REGEXP, regexJsonObj).build();
    }

    private JsonObject header() {
        return createObjectBuilder().build();
    }

    private JsonObject body(final JsonValue queryValue) {
        final JsonObjectBuilder bool = createObjectBuilder().add(SHOULD, queryValue).add(MUST, range());
        final JsonObjectBuilder boolQuery = createObjectBuilder().add(BOOL, bool);
        final JsonObjectBuilder filter = createObjectBuilder().add(FILTER, boolQuery);
        final JsonObjectBuilder constantSore = createObjectBuilder().add(CONSTANT_SCORE, filter);
        return createObjectBuilder().add(SIZE, searchCriteria.getResponseSize()).add(QUERY_KEY, constantSore).build();
    }

    private String multiSearchQuery(final JsonObject header, final JsonObject body) {
        return body.toString() + NEW_LINE;
    }

    public void setInstantGenerator(final SearchConstants.InstantGenerator instantGenerator) {
        this.instantGenerator = instantGenerator;
    }

    public SearchCriteria searchCriteria() {
        return searchCriteria;
    }

    public List<String> queries() {
        getKeywordsAndRegexesToQuery();
        return this.queries;
    }
}