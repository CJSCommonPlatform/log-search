package uk.gov.justice.log.main;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.ResultsPrinter;
import uk.gov.justice.log.utils.SearchConstants;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SearchHandlerTest {

    private final String queryStr = "{}{\"query\":{\"bool\":{\"should\":" +
            "{\"terms\":{\"message\":[\"202\"]}},\"must\":{\"range\":" +
            "{\"@timestamp\":{\"from\":1486654918164,\"to\":1486658518164}}}}}}";

    @Mock
    SearchService searchService;

    @Mock
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Mock
    Response response;

    @Mock
    RestClient restClient;

    @Mock
    SearchLogsFactory searchLogsFactory;

    @Mock
    ResultsPrinterFactory resultsPrinterFactory;

    @Mock
    ResultsPrinter resultsPrinter;

    @Mock
    ResultExtractor resultExtractor;

    @InjectMocks
    SearchHandler searchHandler;

    @Test
    public void shouldSearchViaSearchService() throws Throwable {
        when(searchLogsFactory.create(restClient, elasticSearchQueryBuilder)).thenReturn(searchService);
        when(resultsPrinterFactory.createResultsPrinters(SearchConstants.YES)).thenReturn(resultsPrinter);
        when(searchService.search()).thenReturn(response);
        when(elasticSearchQueryBuilder.query()).thenReturn(queryStr);

        final SearchHandler searchLogs = new SearchHandler(elasticSearchQueryBuilder, restClient, searchLogsFactory, resultExtractor, resultsPrinterFactory);

        searchLogs.searchLogs(SearchConstants.YES);

        verify(searchService).search();
        verify(resultExtractor).extractResult(elasticSearchQueryBuilder,response);
    }
}
