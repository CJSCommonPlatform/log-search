package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;
import uk.gov.justice.log.search.KibanaQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.HTMLPrinter;
import uk.gov.justice.log.utils.ConnectionManager;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.RestConfig;
import uk.gov.justice.log.utils.SearchParameters;
import uk.gov.justice.log.wrapper.HttpAsyncClientBuilderWrapper;
import uk.gov.justice.log.wrapper.RequestConfigBuilderWrapper;
import uk.gov.justice.log.wrapper.RestClientFactory;

import java.io.IOException;

import com.google.common.annotations.VisibleForTesting;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class SearchLogs extends AbstractLogSearcherCommand implements ShellCommand {

    protected KibanaQueryBuilder kibanaQueryBuilder;
    private SearchService searchService;
    private SearchCriteria searchCriteria;
    private RestConfig restConfig;
    private SearchParameters searchParameters;

    public SearchLogs(final KibanaQueryBuilder kibanaQueryBuilder) {
        this.kibanaQueryBuilder = kibanaQueryBuilder;
        this.searchService = new SearchService(restClient());
    }
    @VisibleForTesting
    public SearchLogs(final KibanaQueryBuilder kibanaQueryBuilder, final SearchService searchService) {
        this.kibanaQueryBuilder = kibanaQueryBuilder;
        this.searchService =searchService;
    }

    @Override
    public void run(String[] strings) {
        try {
            this.searchParameters = new SearchParameters(strings[0], strings[1], strings[2], strings[3]);
            final PropertyReader propertyReader = new PropertyReader(searchParameters);
            this.restConfig = propertyReader.restConfig();
            this.searchCriteria = propertyReader.searchCriteria();
            final Response response = searchService.search(kibanaQueryBuilder);
            printResults(kibanaQueryBuilder, response);
        } catch (IOException e) {
            consolePrinter.writeException(e);
        }
    }

    private void printResults(final KibanaQueryBuilder kibanaQueryBuilder, final Response response) {
        final HTMLPrinter printer = new HTMLPrinter(searchParameters.getResponseOutputPath());
        try {
            final String responseStrActual = EntityUtils.toString(response.getEntity());
            final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");
            final JSONArray messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");
            consolePrinter.write("Hits: " + hits);
            final String query =kibanaQueryBuilder.query();
            printer.writeMessages(query, searchCriteria.getFromTime(), searchCriteria.getToTime(), hits + "", messages);
        } catch (IOException e) {
            consolePrinter.writeException(e);
        }
    }

    private RestClient restClient() {
        final ConnectionManager connectionManager = new ConnectionManager();
        final RequestConfigBuilderWrapper requestConfigBuilderWrapper = new RequestConfigBuilderWrapper(restConfig);
        final HttpAsyncClientBuilderWrapper httpAsyncClientBuilderWrapper = new HttpAsyncClientBuilderWrapper(connectionManager);
        final RestClientFactory restClientFactory = new RestClientFactory();
        return restClientFactory.buildRestClient(restConfig,
                requestConfigBuilderWrapper, httpAsyncClientBuilderWrapper);
    }
}