package uk.gov.justice.log.integration;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.log.utils.CommonConstant.ELASTIC_SEARCH_CLUSTER_URL;
import static uk.gov.justice.log.utils.CommonConstant.MAX_RETRY_TIMEOUT_MILLIS;
import static uk.gov.justice.log.utils.CommonConstant.POST;
import static uk.gov.justice.log.utils.CommonConstant.PUT;

import uk.gov.justice.log.search.KibanaQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.utils.ConnectionManager;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.RestConfig;
import uk.gov.justice.log.wrapper.HttpAsyncClientBuilderWrapper;
import uk.gov.justice.log.wrapper.RequestConfigBuilderWrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.node.internal.InternalSettingsPreparer;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchServiceIT {

    private static final String HOST_SCHEME = "http";
    private static final String HOST_NAME = "localhost";
    private static final int HOST_PORT = 9205;
    private static final String PROXY_HOST = "proxy_host";
    private static final String SAMPLE_LOGS_PATH = "src/test/resources/products.json";
    private static final String HTTP_TRANSPORT_PORT = "9307";
    private static final String ES_WORKING_DIR = "target/es";
    private static final String CONFIG_PATH = "src/test/resources/config.yaml";
    private static final String SEARCH_PATH = "src/test/resources/search.yaml";
    private static Node node;
    private static RestConfig restConfig;

    private final Logger LOGGER = LoggerFactory.getLogger(SearchServiceIT.class);
    private final String ipRegex = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    private SearchCriteria searchCriteria;

    @BeforeClass
    public static void startElasticsearch() throws IOException, NodeValidationException,InterruptedException {
        removeOldDataDir(ES_WORKING_DIR);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "");
        mockSetupForSearchCriteria(Arrays.asList("testuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        restConfig = propertyReader.restConfig();

        Settings settings = Settings.builder()
                .put("path.home", ES_WORKING_DIR)
                .put("path.conf", ES_WORKING_DIR)
                .put("path.data", ES_WORKING_DIR)
                .put("path.logs", ES_WORKING_DIR)
                .put("http.port", restConfig.getHostPort())
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("cluster.name", "elasticsearch")
                .build();

        final Collection plugins = Arrays.asList(Netty4Plugin.class);
        node = new PluginConfigurableNode(settings, plugins).start();
        createMapping();
        uploadSampleData(SAMPLE_LOGS_PATH);
        Thread.sleep(1000);
    }

    @AfterClass
    public static void destroy() throws IOException {
        new File(CONFIG_PATH).delete();
        new File(SEARCH_PATH).delete();
        node.close();
    }

    private static void removeOldDataDir(String datadir) throws IOException {
        File dataDir = new File(datadir);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(Paths.get(datadir));
        }
    }

    public static RestClient restClient() {
        return RestClient.builder(new HttpHost(restConfig.getHostName(),
                restConfig.getHostPort(), restConfig.getHostScheme()))
                .setMaxRetryTimeoutMillis(MAX_RETRY_TIMEOUT_MILLIS)
                .setRequestConfigCallback(new RequestConfigBuilderWrapper(restConfig))
                .setHttpClientConfigCallback(new HttpAsyncClientBuilderWrapper(new ConnectionManager()))
                .build();
    }

    private static void uploadSampleData(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(SAMPLE_LOGS_PATH));
        final String logs = new String(encoded, "UTF-8");

        final String endpoint = ELASTIC_SEARCH_CLUSTER_URL + "_bulk?pretty";
        final HttpEntity data = new NStringEntity(logs, APPLICATION_JSON);
        restClient().performRequest(POST, endpoint, Collections.emptyMap(), data);
    }

    private static void mockSetupForConfig(final String hostName, final String hostScheme, final Integer hostPort,
                                           final int proxyPort, final String proxyHost) throws IOException {

        final Map<String, Object> data = new HashMap<>();
        data.put("hostName", hostName);
        data.put("hostScheme", hostScheme);
        data.put("hostPort", hostPort);
        data.put("proxyPort", proxyPort);
        data.put("proxyHost", proxyHost);

        new Yaml().dump(data, new FileWriter(CONFIG_PATH));
    }

    private static void mockSetupForSearchCriteria(final List<String> keywords, final List<String> regexes,
                                                   final int durationMinutes, final String fromTime, final String toTime) throws IOException {
        final Map<String, Object> data = new HashMap<>();
        data.put("keywords", keywords);
        data.put("regexes", regexes);
        data.put("durationMinutes", durationMinutes);
        if (!StringUtils.isEmpty(fromTime)) {
            data.put("fromTime", fromTime);
        }
        if (!StringUtils.isEmpty(toTime)) {
            data.put("toTime", toTime);
        }
        new Yaml().dump(data, new FileWriter(SEARCH_PATH));
    }

    /*
    curl -XPUT 'localhost:9200/_template/all' -d '
{
        "order": 0,
        "template": "*",
        "settings": {
            "index.number_of_shards": "1"
        },
        "mappings": {
            "_default_": {
                "dynamic_templates": [
                    {
                        "string": {
                            "mapping": {
                                "index": "not_analyzed",
                                "type": "string"
                            },
                            "match_mapping_type": "string"
                        }
                    }
                ]
            }
        },
        "aliases": {}
    }
}
     */
    private static JsonObject templateMappingForAllStringTypeFields() {
        final JsonObject mappingContents = Json.createObjectBuilder().add("index", "not_analyzed").build();
        final JsonObject mapping = Json.createObjectBuilder().add("mapping", mappingContents).add("match", "message").build();
        final JsonObject string = Json.createObjectBuilder().add("message", mapping).build();
        final JsonArray dynamicTemplates = Json.createArrayBuilder().add(string).build();
        final JsonObject dynamicTemplatesObject = Json.createObjectBuilder().add("dynamic_templates", dynamicTemplates).build();
        final JsonObject products = Json.createObjectBuilder().add("_default_", dynamicTemplatesObject).build();
        final JsonObject mappings = Json.createObjectBuilder().add("mappings", products).build();
        System.out.println(mappings);
        return mappings;
    }

    private static void createMapping() throws IOException {
        final String mappings = templateMappingForAllStringTypeFields().toString();
        final HttpEntity mappingEntity = new NStringEntity(mappings, APPLICATION_JSON);

        restClient().performRequest(PUT, ELASTIC_SEARCH_CLUSTER_URL, Collections.emptyMap(), mappingEntity);
    }

    @After
    public void removeFiles() throws IOException {
        new File(SEARCH_PATH).delete();
        removeOldDataDir(ES_WORKING_DIR);
    }


    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingOnly() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 2, 2);
    }


    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneLowerCaseKeywordAdminUser() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 0, 0);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordUpperCaseAdminUser() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("ADMINUSER"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 5, 5);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordTes() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("tes"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleKeywords() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneRegexAndOneKeyword() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("abcd"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 10, 10);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeyword() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 11, 11);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeywordLimitByTime() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:05:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 10, 10);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBeggining() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(".log$output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("log$output."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBeginingAndEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(".log$output."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheMiddle() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 4, 4);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingMiddleAndEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" log output "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInMiddle() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("log.output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 1, 1);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIP() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 2, 2);
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIPWithspecialCharacterInBegining() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        printHitsAndMessages(EntityUtils.toString(response.getEntity()), 2, 2);
    }

    private void printHitsAndMessages(final String responseString, final int hits, final int messagesSize) {

        final Integer hitsActual = JsonPath.read(responseString, "$.responses[0].hits.total");
        final JSONArray messagesActualArray = JsonPath.read(responseString, "$.responses[0].hits..message");
        final JSONArray timeStampActualArray = JsonPath.read(responseString, "$.responses[0].hits..@timestamp");

        LOGGER.info("Hits:" + hitsActual);

        for (int i = 0; i < messagesActualArray.size(); i++) {
            LOGGER.info("Timestamp: " + timeStampActualArray.get(i) +
                    "  Message:" + messagesActualArray.get(i).toString());
        }

        assertThat(hitsActual, is(hits));
        assertThat(messagesActualArray.size(), is(messagesSize));
    }

    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
        }
    }
}