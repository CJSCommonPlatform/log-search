package uk.gov.justice.framework.tools.command;

import uk.gov.justice.framework.tools.common.command.ShellCommand;
import uk.gov.justice.log.search.KibanaQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.search.main.output.HTMLPrinter;
import uk.gov.justice.log.utils.ConnectionManager;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.RestConfig;
import uk.gov.justice.log.wrapper.HttpAsyncClientBuilderWrapper;
import uk.gov.justice.log.wrapper.RequestConfigBuilderWrapper;
import uk.gov.justice.log.wrapper.RestClientFactory;

import java.io.IOException;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class SearchLogs extends AbstractLogSearcherCommand implements ShellCommand {

    public static void main(String[] args) {
        PropertyReader propertyReader = null;
        propertyReader = new PropertyReader(args[0], args[1], args[2], args[3]);

        new SearchLogs().search(propertyReader);
    }

    private void search(final PropertyReader propertyReader) {
        final RestConfig restConfig = propertyReader.restConfig();
        final SearchCriteria searchCriteria = propertyReader.searchCriteria();
        final HTMLPrinter filePrinter = new HTMLPrinter(propertyReader.responseOutputPath());

        final RestClient restClient = restClient(restConfig);
        final SearchService searchService = new SearchService(restClient);

        try {
            final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);
            final Response response = searchService.search(kibanaQueryBuilder);
            final String responseStrActual = EntityUtils.toString(response.getEntity());
            final Integer hits = JsonPath.read(responseStrActual, "$.responses[0].hits.total");
            final JSONArray messages = JsonPath.read(responseStrActual, "$.responses[0].hits..message");
            consolePrinter.write("Hits: " + hits);
            filePrinter.writeMessages(kibanaQueryBuilder.query().toString(),
                    searchCriteria.getFromTime(), searchCriteria.getToTime(),
                    hits + "",
                    messages);
        } catch (IOException exception) {
            consolePrinter.writeStackTrace(exception);
        }
    }

    private RestClient restClient(RestConfig restConfig) {
        final ConnectionManager connectionManager = new ConnectionManager();
        final RequestConfigBuilderWrapper requestConfigBuilderWrapper = new RequestConfigBuilderWrapper(restConfig);
        final HttpAsyncClientBuilderWrapper httpAsyncClientBuilderWrapper = new HttpAsyncClientBuilderWrapper(connectionManager);

        final RestClientFactory restClientFactory = new RestClientFactory();
        return restClientFactory.buildRestClient(restConfig,
                requestConfigBuilderWrapper, httpAsyncClientBuilderWrapper);
    }

    @Override
    public void run(final String[] strings) {
        final PropertyReader propertyReader = new PropertyReader(configYamlPath, searchCriteriaYamlPath, outputFilePath, userListFilePath);
        search(propertyReader);
    }
}