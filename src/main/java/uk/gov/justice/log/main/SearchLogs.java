package uk.gov.justice.log.main;

import uk.gov.justice.log.factory.RestClientFactory;
import uk.gov.justice.log.factory.ResultsPrinterFactory;
import uk.gov.justice.log.factory.SearchLogsFactory;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.SearchConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchLogs {

    private final Logger LOGGER = LoggerFactory.getLogger(SearchLogs.class);


    @Parameter(names = "-search", description = "search criteria yaml path", required = true)
    String searchCriteriaYamlPath;
    @Parameter(names = "-userlist", description = "user list path", required = false)
    String userListJsonFilePath;
    @Parameter(names = "-output", description = "output file path", required = false)
    String resultsFilePath;
    @Parameter(names = "-config", description = "config file yaml path", required = true)
    String configYamlFilePath;
    @Parameter(names = "-msg", description = "display console messages", required = false)
    String displayConsoleMessages;

    public static void main(String... args) {
        SearchLogs searchLogs = new SearchLogs();
        new JCommander(searchLogs, args);
        searchLogs.run();
    }

    public void run() {
        final SearchConfig searchConfig = new SearchConfig(
                configYamlFilePath, searchCriteriaYamlPath, userListJsonFilePath, resultsFilePath,displayConsoleMessages);

        Path resultsPath = null;
        if (resultsFilePath != null) {
            resultsPath = Paths.get(resultsFilePath);
        }
        final PropertyReader propertyReader = new PropertyReader(searchConfig);
        final RestClientFactory restClientFactory = new RestClientFactory(propertyReader.restConfig());
        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder((propertyReader.searchCriteria()));

        new SearchHandler(elasticSearchQueryBuilder, restClientFactory.restClient(),
                new SearchLogsFactory(propertyReader.searchCriteria()),
                new ResultsPrinterFactory(resultsPath)
        ).searchLogs(displayConsoleMessages);
    }
}