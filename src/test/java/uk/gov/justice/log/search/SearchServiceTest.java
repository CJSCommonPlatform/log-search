package uk.gov.justice.log.search;


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.log.utils.SearchConstants.ELASTIC_MULTI_SEARCH_URL;

import uk.gov.justice.log.factory.RestClientFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {

    @Mock
    RestClientFactory restClientFactory;

    @Mock
    RestClient restClient;

    @Mock
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Mock
    Response response;

    private SearchService searchService;

    @Before
    public void setUp() {
        searchService = new SearchService(restClientFactory, elasticSearchQueryBuilder);
    }

    @Test
    public void shouldReturnReponse() throws IOException {
        final String queryStr = "queryStr";
        final HttpEntity query = new NStringEntity(queryStr);
        final String responseStr = "responseStr";

        final HttpEntity responseEntity = new NStringEntity(responseStr, ContentType.APPLICATION_JSON);
        final Map<String, String> params = new HashMap<>();

        when(restClientFactory.restClient()).thenReturn(restClient);
        when(restClient.performRequest("POST", ELASTIC_MULTI_SEARCH_URL, params, query)).
                thenReturn(response);
        when(response.getEntity()).thenReturn(responseEntity);

        final List<Response> response = searchService.search();

        assertThat(response, is(response));
    }
}
