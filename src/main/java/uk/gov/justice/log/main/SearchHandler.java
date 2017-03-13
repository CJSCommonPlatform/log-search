package uk.gov.justice.log.main;

import static uk.gov.justice.log.utils.SearchConstants.RESPONSE_HITS;
import static uk.gov.justice.log.utils.SearchConstants.RESPONSE_MESSAGES;

import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.Result;
import uk.gov.justice.log.search.main.output.ResultsPrinter;

import java.io.IOException;

import javax.json.Json;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class SearchHandler {
    private final RestClient restClient;
    private final SearchLogsFactory searchLogsFactory;
    private final ResultsPrinterFactory resultsPrinterFactory;
    private final ElasticSearchQueryBuilder elasticSearchQueryBuilder;

    public SearchHandler(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                         final RestClient restClient,
                         final SearchLogsFactory searchLogsFactory,
                         final ResultsPrinterFactory resultsPrinterFactory) {
        this.elasticSearchQueryBuilder = elasticSearchQueryBuilder;
        this.restClient = restClient;
        this.searchLogsFactory = searchLogsFactory;
        this.resultsPrinterFactory = resultsPrinterFactory;
    }

    public void searchLogs(final String displayConsoleMessages) {

        final ResultsPrinter resultsPrinter = resultsPrinterFactory.createResultsPrinters(displayConsoleMessages);

        try {
            final SearchService searchService = searchLogsFactory.create(restClient, elasticSearchQueryBuilder);

            final Response response = searchService.search();

            final Result result = extractResult(elasticSearchQueryBuilder, response);
            resultsPrinter.printResults(Json.createObjectBuilder(),result);

        } catch (Exception exception) {
            resultsPrinter.printException(exception);
        }
    }

    private Result extractResult(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                                 final Response response) throws IOException {
        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, RESPONSE_HITS);
        final JSONArray messages = JsonPath.read(responseStrActual, RESPONSE_MESSAGES);
        final SearchCriteria searchCriteria = elasticSearchQueryBuilder.searchCriteria();
        final String query = elasticSearchQueryBuilder.query();
        final String fromTime = searchCriteria.getFromTime();
        final String toTime = searchCriteria.getToTime();
        return new Result(query, fromTime, toTime, hits, messages);
    }
}
