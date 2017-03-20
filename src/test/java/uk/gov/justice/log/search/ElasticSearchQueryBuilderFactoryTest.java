package uk.gov.justice.log.search;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.log.utils.SearchConstants.MINS_TO_MILLIS_MULTIPLIER;

import uk.gov.justice.log.utils.SearchConstants;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchQueryBuilderFactoryTest {

    private static final int DURATION_MINUTES = 60;
    private static final Instant NOW = Instant.now();
    @Mock
    SearchConstants.InstantGenerator instantGenerator;
    @Mock
    SearchCriteria searchCriteria;
    @InjectMocks
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    private Logger LOGGER = LoggerFactory.getLogger(ElasticSearchQueryBuilderFactoryTest.class);

    @Before
    public void setUp() {
        elasticSearchQueryBuilder.setInstantGenerator(instantGenerator);
        when(instantGenerator.now()).thenReturn(NOW);
        when(searchCriteria.getDurationMinutes()).thenReturn(DURATION_MINUTES);
        when(searchCriteria.getResponseSize()).thenReturn(50);
    }


    @Test
    public void shouldCreateRangeBasedOnDurationProvided() throws IOException {
        when(searchCriteria.getKeywords()).thenReturn(Arrays.asList("202"));
        when(searchCriteria.getRegexes()).thenReturn(new ArrayList<>());

        final int durationMinutes = 60;
        final List<String> keywords = new ArrayList<>();
        keywords.add("202");

        final List<String> regexes = new ArrayList<>();
        when(searchCriteria.getKeywords()).thenReturn(keywords);
        when(searchCriteria.getRegexes()).thenReturn(regexes);
        when(searchCriteria.getDurationMinutes()).thenReturn(durationMinutes);
        when(searchCriteria.getResponseSize()).thenReturn(50);

        final int durationMillis = durationMinutes * MINS_TO_MILLIS_MULTIPLIER;
        final long expectedTo = NOW.toEpochMilli();
        final long expectedFrom = NOW.minusMillis(durationMillis).toEpochMilli();

        final List<String> queries = elasticSearchQueryBuilder.queries();
        final String query = queries.get(0).trim();
        final List<String> from = JsonPath.read(query, "$.query..range.@timestamp.gte");
        final List<String> to = JsonPath.read(query, "$.query..range.@timestamp.lte");

        assertThat(queries.get(0), is(notNullValue()));
        assertThat(from.get(0), is(expectedFrom));
        assertThat(to.get(0), is(expectedTo));
    }

    @Test
    public void shouldSearchWithOneKeywordWithOneField() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        elasticSearchQueryBuilder.setInstantGenerator(instantGenerator);
        when(instantGenerator.now()).thenReturn(now);

        final List<String> keywords = new ArrayList<>();
        keywords.add("202");

        final List<String> regexes = new ArrayList<>();

        when(searchCriteria.getKeywords()).thenReturn(keywords);
        when(searchCriteria.getRegexes()).thenReturn(regexes);
        when(searchCriteria.getDurationMinutes()).thenReturn(durationMinutes);
        when(searchCriteria.getResponseSize()).thenReturn(50);

        final int durationMillis = durationMinutes * MINS_TO_MILLIS_MULTIPLIER;
        final long expectedTo = now.toEpochMilli();
        final long expectedFrom = now.minusMillis(durationMillis).toEpochMilli();

        final String regexpExpectedString = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final List<String> expQueries = new ArrayList<>();
        expQueries.add(regexpExpectedString);

        final List<String> actualQueries = elasticSearchQueryBuilder.queries();
        assertThat(actualQueries, is(expQueries));
    }

    @Test
    public void shouldSearchWithMultipleKeywordWithOneField() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        elasticSearchQueryBuilder.setInstantGenerator(instantGenerator);
        when(instantGenerator.now()).thenReturn(now);

        final List<String> keywords = new ArrayList<>();
        keywords.add("202");
        keywords.add("404");

        final List<String> regexes = new ArrayList<>();

        when(searchCriteria.getKeywords()).thenReturn(keywords);
        when(searchCriteria.getRegexes()).thenReturn(regexes);
        when(searchCriteria.getDurationMinutes()).thenReturn(durationMinutes);
        when(searchCriteria.getResponseSize()).thenReturn(50);

        final int durationMillis = durationMinutes * MINS_TO_MILLIS_MULTIPLIER;
        final long expectedTo = now.toEpochMilli();
        final long expectedFrom = now.minusMillis(durationMillis).toEpochMilli();

        final String regexpExpectedString1 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";
        final String regexpExpectedString2 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"404\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";
        final List<String> expQueries = new ArrayList<>();
        expQueries.add(regexpExpectedString1);
        expQueries.add(regexpExpectedString2);

        final List<String> queries = elasticSearchQueryBuilder.queries();

        assertThat(queries, is(expQueries));
    }


    @Test
    public void shouldSearchWithOneRegex() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        elasticSearchQueryBuilder.setInstantGenerator(instantGenerator);
        when(instantGenerator.now()).thenReturn(now);

        final List<String> keywords = new ArrayList<>();
        keywords.add("202");

        final List<String> regexes = new ArrayList<>();
        regexes.add("[2][0][2]");

        when(searchCriteria.getKeywords()).thenReturn(keywords);
        when(searchCriteria.getRegexes()).thenReturn(regexes);
        when(searchCriteria.getDurationMinutes()).thenReturn(durationMinutes);
        when(searchCriteria.getResponseSize()).thenReturn(50);

        final int durationMillis = durationMinutes * MINS_TO_MILLIS_MULTIPLIER;
        final long expectedTo = now.toEpochMilli();
        final long expectedFrom = now.minusMillis(durationMillis).toEpochMilli();

        final String regexpExpectedString1 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*[2][0][2].*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";
        final String regexpExpectedString2 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final List<String> expQueries = new ArrayList<>();
        expQueries.add(regexpExpectedString1);
        expQueries.add(regexpExpectedString2);

        final List<String> queries = elasticSearchQueryBuilder.queries();

        assertThat(queries, is(expQueries));

    }

    @Test
    public void shouldSearchWithMultipleRegex() throws IOException {
        final int durationMinutes = 60;
        final Instant now = Instant.now();
        elasticSearchQueryBuilder.setInstantGenerator(instantGenerator);

        when(instantGenerator.now()).thenReturn(now);

        final List<String> keywords = new ArrayList<>();
        keywords.add("202");

        final List<String> regexes = new ArrayList<>();

        regexes.add("[2][0][2]");
        regexes.add("[4][0][4]");
        when(searchCriteria.getKeywords()).thenReturn(keywords);
        when(searchCriteria.getRegexes()).thenReturn(regexes);
        when(searchCriteria.getDurationMinutes()).thenReturn(durationMinutes);
        when(searchCriteria.getResponseSize()).thenReturn(50);

        final int durationMillis = durationMinutes * MINS_TO_MILLIS_MULTIPLIER;
        final long expectedTo = now.toEpochMilli();
        final long expectedFrom = now.minusMillis(durationMillis).toEpochMilli();

        final String regexpExpectedString1 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*[2][0][2].*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final String regexpExpectedString2 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*[4][0][4].*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final String regexpExpectedString3 = "{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final List<String> expQueries = new ArrayList<>();
        expQueries.add(regexpExpectedString1);
        expQueries.add(regexpExpectedString2);
        expQueries.add(regexpExpectedString3);

        final List<String> queries = elasticSearchQueryBuilder.queries();

        assertThat(queries, is(expQueries));
    }

}