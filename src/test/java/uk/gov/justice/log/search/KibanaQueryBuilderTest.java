package uk.gov.justice.log.search;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.log.utils.CommonConstant.MINS_TO_MILLIS_MULTIPLIER;

import uk.gov.justice.log.wrapper.InstantWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class KibanaQueryBuilderTest {

    private static final int DURATION_MINUTES = 60;
    private static final Instant now = Instant.now();
    @Mock
    InstantWrapper instantWrapper;
    @Mock
    SearchCriteria searchCriteria;
    @InjectMocks
    KibanaQueryBuilder kibanaQueryBuilder;
    private Logger LOGGER = LoggerFactory.getLogger(KibanaQueryBuilderTest.class);

    @Before
    public void setUp() {
        kibanaQueryBuilder.setInstantWrapper(instantWrapper);
        when(instantWrapper.now()).thenReturn(now);
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
        final long expectedTo = now.toEpochMilli();
        final long expectedFrom = now.minusMillis(durationMillis).toEpochMilli();

        final HttpEntity queryJson = kibanaQueryBuilder.entityQuery();
        final String queryJsonBody = EntityUtils.toString(queryJson).substring(3);

        final JSONArray from = JsonPath.read(queryJsonBody, "$.query..range.@timestamp.gte");
        final JSONArray to = JsonPath.read(queryJsonBody, "$.query..range.@timestamp.lte");

        assertThat(queryJson, is(notNullValue()));
        assertThat(from.get(0), is(expectedFrom));
        assertThat(to.get(0), is(expectedTo));
    }

    @Test
    public void shouldSearchWithOneKeywordWithOneField() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        kibanaQueryBuilder.setInstantWrapper(instantWrapper);
        when(instantWrapper.now()).thenReturn(now);

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

        final String queryExpected = "{}\n{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";
        final NStringEntity stringEntity = (NStringEntity) kibanaQueryBuilder.entityQuery();

        LOGGER.info(queryStr(stringEntity));
        assertThat(queryStr(stringEntity), is(queryExpected));
    }


    @Test
    public void shouldSearchWithMultipleKeywordWithOneField() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        kibanaQueryBuilder.setInstantWrapper(instantWrapper);
        when(instantWrapper.now()).thenReturn(now);

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

        final String queryExpected = "{}\n{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}},{\"regexp\":{\"message\":\".*\\\"404\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final NStringEntity stringEntity = (NStringEntity) kibanaQueryBuilder.entityQuery();
        final String queryActual = queryStr(stringEntity);
        LOGGER.info(queryActual);
        assertThat(queryActual, is(queryExpected));
    }


    @Test
    public void shouldSearchWithOneRegex() throws IOException {

        final int durationMinutes = 60;

        final Instant now = Instant.now();
        kibanaQueryBuilder.setInstantWrapper(instantWrapper);
        when(instantWrapper.now()).thenReturn(now);

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


        final String regexpExpectedString = "{}\n{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*[2][0][2].*\"}},{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final NStringEntity stringEntity = (NStringEntity) kibanaQueryBuilder.entityQuery();
        LOGGER.info(queryStr(stringEntity));
        assertThat(queryStr(stringEntity), is(regexpExpectedString));
    }

    @Test
    public void shouldSearchWithMultipleRegex() throws IOException {
        final int durationMinutes = 60;
        final Instant now = Instant.now();
        kibanaQueryBuilder.setInstantWrapper(instantWrapper);

        when(instantWrapper.now()).thenReturn(now);

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
        final String regexpExpectedString = "{}\n{\"size\":50,\"query\":{\"constant_score\":{\"filter\":{\"bool\":{\"should\":[{\"regexp\":{\"message\":\".*[2][0][2].*\"}},{\"regexp\":{\"message\":\".*[4][0][4].*\"}},{\"regexp\":{\"message\":\".*\\\"202\\\".*\"}}],\"must\":{\"range\":{\"@timestamp\":{\"gte\":" + expectedFrom + ",\"lte\":" + expectedTo + "}}}}}}}}\n";

        final NStringEntity stringEntity = (NStringEntity) kibanaQueryBuilder.entityQuery();
        LOGGER.info(queryStr(stringEntity));
        assertThat(queryStr(stringEntity), is(regexpExpectedString));
    }

    private String queryStr(final NStringEntity stringEntity) throws IOException {
        return EntityUtils.toString(stringEntity);
    }
}