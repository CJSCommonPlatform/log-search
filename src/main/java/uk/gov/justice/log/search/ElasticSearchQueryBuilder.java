package uk.gov.justice.log.search;


import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static uk.gov.justice.log.utils.SearchConstants.MINS_TO_MILLIS_MULTIPLIER;
import static uk.gov.justice.log.utils.SearchConstants.NEW_LINE;

import uk.gov.justice.log.utils.SearchConstants;

import java.time.Instant;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;

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
    private String query;
    private HttpEntity entityQuery;

    public ElasticSearchQueryBuilder(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    private String getKeywordsAndRegexesToQuery() {
        final StringBuilder stringBuilder = new StringBuilder();

        final JsonArrayBuilder regexpArrayBuilder = Json.createArrayBuilder();
        final List<String> regexes = searchCriteria.getRegexes();
        if (regexes != null && !regexes.isEmpty()) {
            for (String regex : regexes) {
                regexpArrayBuilder.add(regexp(MESSAGE_FIELD, ".*" + regex + ".*"));
            }
        }
        final List<String> keywords = searchCriteria.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            for (String keyword : keywords) {
                regexpArrayBuilder.add(regexp(MESSAGE_FIELD, ".*\"" + keyword + "\".*"));
            }
            stringBuilder.append(multiSearchQuery(header(), body(regexpArrayBuilder.build())));
        }
        return stringBuilder.toString();
    }


    private JsonObject range() {
        long searchFrom;
        long searchTo;

        if (searchCriteria.isFromToTimeSet()) {
            final String fromStr = searchCriteria.getFromTime();
            final String toStr = searchCriteria.getToTime();
            searchFrom = Instant.parse(fromStr).toEpochMilli();
            searchTo = Instant.parse(toStr).toEpochMilli();
        } else {
            final int rangeDuration = searchCriteria.getDurationMinutes();
            searchFrom = instantGenerator.now().minusMillis(rangeDuration * MINS_TO_MILLIS_MULTIPLIER).toEpochMilli();
            searchTo = instantGenerator.now().toEpochMilli();
            searchCriteria.setFromTime(Instant.ofEpochMilli(searchFrom).toString());
            searchCriteria.setToTime(Instant.ofEpochMilli(searchTo).toString());
        }
        final JsonObject timeStampRange = Json.createObjectBuilder().add(FROM, searchFrom).add(TO, searchTo).build();
        final JsonObject timestamp = Json.createObjectBuilder().add(TIMESTAMP_FIELD, timeStampRange).build();

        return Json.createObjectBuilder().add(RANGE, timestamp).build();
    }

    private JsonObject regexp(final String field, final String regex) {
        final JsonObject regexJsonObj = Json.createObjectBuilder().add(field, regex).build();
        return Json.createObjectBuilder().add(REGEXP, regexJsonObj).build();
    }

    private JsonObject header() {
        return Json.createObjectBuilder().build();
    }

    private JsonObject body(final JsonValue queryValue) {
        final JsonObjectBuilder bool = Json.createObjectBuilder().add(SHOULD, queryValue).add(MUST, range());
        final JsonObjectBuilder boolQuery = Json.createObjectBuilder().add(BOOL, bool);
        final JsonObjectBuilder filter = Json.createObjectBuilder().add(FILTER, boolQuery);
        final JsonObjectBuilder constantSore = Json.createObjectBuilder().add(CONSTANT_SCORE, filter);
        return Json.createObjectBuilder().add(SIZE, searchCriteria.getResponseSize()).add(QUERY_KEY, constantSore).build();
    }

    private String multiSearchQuery(final JsonObject header, final JsonObject body) {
        this.query = header.toString() + NEW_LINE + body.toString() + NEW_LINE;
        return query;
    }

    public void setInstantGenerator(final SearchConstants.InstantGenerator instantGenerator) {
        this.instantGenerator = instantGenerator;
    }

    public String query() {
        return this.query;
    }

    public HttpEntity entityQuery() {
        this.entityQuery = new NStringEntity(getKeywordsAndRegexesToQuery(), APPLICATION_JSON);
        return entityQuery;
    }

    public SearchCriteria searchCriteria() {
        return searchCriteria;
    }
}