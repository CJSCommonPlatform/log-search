package uk.gov.justice.log.main;

import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.Result;
import uk.gov.justice.log.search.main.output.ResultsPrinter;

import java.io.IOException;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class SearchHandler {
    private final RestClient restClient;
    private final SearchLogsFactory searchLogsFactory;
    private final ResultsPrinterFactory resultsPrinterFactory;
    private final ElasticSearchQueryBuilder elasticSearchQueryBuilder;
    private final ResultExtractor resultExtractor;

    public SearchHandler(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                         final RestClient restClient,
                         final SearchLogsFactory searchLogsFactory,
                         final ResultExtractor resultExtractor,
                         final ResultsPrinterFactory resultsPrinterFactory) {
        this.elasticSearchQueryBuilder = elasticSearchQueryBuilder;
        this.restClient = restClient;
        this.searchLogsFactory = searchLogsFactory;
        this.resultExtractor = resultExtractor;
        this.resultsPrinterFactory = resultsPrinterFactory;
    }

    public void searchLogs(final String displayConsoleMessages) throws IOException {

        final ResultsPrinter resultsPrinter = resultsPrinterFactory.createResultsPrinters(displayConsoleMessages);

        try {
            final SearchService searchService = searchLogsFactory.create(restClient, elasticSearchQueryBuilder);

            final Response response = searchService.search();

            final Result result = resultExtractor.extractResult(elasticSearchQueryBuilder, response);

            resultsPrinter.printResults(result);

        } catch (IOException exception) {
            resultsPrinter.printException(exception);
        }
    }
}
