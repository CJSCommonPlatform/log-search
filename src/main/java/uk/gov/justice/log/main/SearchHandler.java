package uk.gov.justice.log.main;

import uk.gov.justice.log.factory.RestClientFactory;
import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.Result;
import uk.gov.justice.log.search.main.output.ResultsPrinter;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.client.Response;

public class SearchHandler {
    private final SearchLogsFactory searchLogsFactory;
    private final ResultsPrinterFactory resultsPrinterFactory;
    private final ElasticSearchQueryBuilder elasticSearchQueryBuilder;
    private final ResultExtractor resultExtractor;
    private final RestClientFactory restClientFactory;

    public SearchHandler(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                         final RestClientFactory restClientFactory,
                         final SearchLogsFactory searchLogsFactory,
                         final ResultExtractor resultExtractor,
                         final ResultsPrinterFactory resultsPrinterFactory) {
        this.elasticSearchQueryBuilder = elasticSearchQueryBuilder;
        this.restClientFactory = restClientFactory;
        this.searchLogsFactory = searchLogsFactory;
        this.resultExtractor = resultExtractor;
        this.resultsPrinterFactory = resultsPrinterFactory;
    }

    public void searchLogs(final String displayConsoleMessages) throws IOException {

        final ResultsPrinter resultsPrinter = resultsPrinterFactory.createResultsPrinters(displayConsoleMessages);

        try {
            final SearchService searchService = searchLogsFactory.create(restClientFactory, elasticSearchQueryBuilder);

            final List<Response> responses = searchService.search();

            final List<Result> results = resultExtractor.extractResults(elasticSearchQueryBuilder, responses);

            resultsPrinter.printResults(results);

        } catch (IOException exception) {
            resultsPrinter.printException(exception);
        }
    }
}
