package uk.gov.justice.log.main;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
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
        byte[] encoded = Files.readAllBytes(Paths.get(RESPONSE_STR_PATH));
        final String responseStr = new String(encoded, "UTF-8");
        final HttpEntity responseEntity = new NStringEntity(responseStr, ContentType.APPLICATION_JSON);
        when(elasticSearchQueryBuilder.searchCriteria()).thenReturn(searchCriteria);
        when(response.getEntity()).thenReturn(responseEntity);
        when(searchCriteria.getFromTime()).thenReturn("fromtime");
        when(searchCriteria.getToTime()).thenReturn("totime");

        resultExtractor.extractResult(elasticSearchQueryBuilder, response);

        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");
        final List<String> messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");

        assertThat(hits, is(1));
        assertThat(messages.size(), is(1));
    }
}