package uk.gov.justice.log.main;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.main.output.Result;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultExtractorTest {

    private final static String RESPONSE_STR_PATH = "src/test/resources/response.json";

    @Mock
    SearchCriteria searchCriteria;

    @Mock
    Response response;

    @Mock
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @InjectMocks
    ResultExtractor resultExtractor;


    @Test
    public void shouldExtractResultsCorrects() throws Exception {
        final List<Response> responses = new LinkedList<>();
        responses.add(response);
        final List<String> queries = new ArrayList<>();
        queries.add("query");
        byte[] encoded = Files.readAllBytes(Paths.get(RESPONSE_STR_PATH));
        final String responseStr = new String(encoded, "UTF-8");
        final HttpEntity responseEntity = new NStringEntity(responseStr, ContentType.APPLICATION_JSON);
        when(elasticSearchQueryBuilder.queries()).thenReturn(queries);

        when(elasticSearchQueryBuilder.searchCriteria()).thenReturn(searchCriteria);
        when(searchCriteria.getFromTime()).thenReturn("fromtime");
        when(searchCriteria.getToTime()).thenReturn("totime");
        when(response.getEntity()).thenReturn(responseEntity);

        final List<Result> results = resultExtractor.extractResults(elasticSearchQueryBuilder, responses);

        assertThat(results.size(), is(1));
        assertThat(results.get(0).getHits(), is(1));
        assertThat(results.get(0).getMessage().size(), is(1));

    }
}