package uk.gov.justice.log.main;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.log.factory.RestClientFactory;
import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.ResultsPrinter;
import uk.gov.justice.log.utils.SearchConstants;

import java.util.List;

import org.elasticsearch.client.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SearchHandlerTest {
    @Mock
    SearchService searchService;

    @Mock
    ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Mock
    List<Response> responses;

    @Mock
    RestClientFactory restClientFactory;

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
        when(searchLogsFactory.create(restClientFactory, elasticSearchQueryBuilder)).thenReturn(searchService);
        when(resultsPrinterFactory.createResultsPrinters(SearchConstants.YES)).thenReturn(resultsPrinter);
        when(searchService.search()).thenReturn(responses);

        final SearchHandler searchLogs = new SearchHandler(elasticSearchQueryBuilder, restClientFactory, searchLogsFactory, resultExtractor, resultsPrinterFactory);

        searchLogs.searchLogs(SearchConstants.YES);

        verify(searchService).search();
        verify(resultExtractor).extractResults(elasticSearchQueryBuilder, responses);
    }
}
