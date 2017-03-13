package uk.gov.justice.log.main;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.factory.RestClientFactory;
import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.ResultsPrinter;
import uk.gov.justice.log.utils.ConnectionManager;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.RestConfig;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.hamcrest.junit.ExpectedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SearchHandlerTest {
    private final static String RESPONSE_STR_PATH = "src/test/resources/response.json";

    private final String queryStr = "{}{\"query\":{\"bool\":{\"should\":" +
            "{\"terms\":{\"message\":[\"202\"]}},\"must\":{\"range\":" +
            "{\"@timestamp\":{\"from\":1486654918164,\"to\":1486658518164}}}}}}";
    @Rule
    public ExpectedException expectedExption = ExpectedException.none();

    @Mock
    PropertyReader propertyReader;

    @Mock
    SearchService searchService;

    @Mock
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Mock
    Response response;

    @Mock
    RestClient restClient;

    @Mock
    RestConfig restConfig;

    @Mock
    SearchCriteria searchCriteria;

    @Mock
    RestClientFactory restClientFactory;

    @Mock
    SearchLogsFactory searchLogsFactory;

    @Mock
    ResultsPrinterFactory resultsPrinterFactory;

    @Mock
    ResultsPrinter resultsPrinter;

    @Mock
    ConnectionManager connectionManager;

    @InjectMocks
    SearchHandler searchHandler;


    @Test
    public void shouldSearchViaSearchService() throws Exception {
        final HttpEntity query = new NStringEntity(queryStr);
        byte[] encoded = Files.readAllBytes(Paths.get(RESPONSE_STR_PATH));
        final String responseStr = new String(encoded, "UTF-8");
        final HttpEntity responseEntity = new NStringEntity(responseStr, ContentType.APPLICATION_JSON);
        final JSONArray array = new JSONArray();
        when(propertyReader.searchCriteria()).thenReturn(searchCriteria);
        when(propertyReader.restConfig()).thenReturn(restConfig);
        when(restClientFactory.restClient()).thenReturn(restClient);
        when(searchLogsFactory.create(restClient, elasticSearchQueryBuilder)).thenReturn(searchService);
        when(resultsPrinterFactory.createResultsPrinters("yes")).thenReturn(resultsPrinter);
        when(searchService.search()).thenReturn(response);
        when(elasticSearchQueryBuilder.searchCriteria()).thenReturn(searchCriteria);
        when(elasticSearchQueryBuilder.entityQuery()).thenReturn(query);
        when(elasticSearchQueryBuilder.query()).thenReturn(queryStr);
        when(searchCriteria.getFromTime()).thenReturn("fromtime");
        when(searchCriteria.getToTime()).thenReturn("totime");

        when(response.getEntity()).thenReturn(responseEntity);

        final SearchHandler searchLogs = new SearchHandler(elasticSearchQueryBuilder, restClient, searchLogsFactory, resultsPrinterFactory);

        searchLogs.searchLogs("yes");

        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");
        final JSONArray messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");

        assertThat(hits, is(1));
        assertThat(messages.size(), is(1));

        verify(searchService).search();
    }
}
