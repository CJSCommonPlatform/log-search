package uk.gov.justice.log.integration;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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
import uk.gov.justice.log.utils.SearchParameters;
import uk.gov.justice.log.wrapper.HttpAsyncClientBuilderWrapper;
import uk.gov.justice.log.wrapper.RequestConfigBuilderWrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public static void startElasticsearch() throws IOException, NodeValidationException, InterruptedException {
        removeOldDataDir(ES_WORKING_DIR);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "");
        mockSetupForSearchCriteria(Arrays.asList("testuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(new SearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
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

    private static JsonObject mappings() {

        final JsonObject type = Json.createObjectBuilder().add("type", "keyword").build();
        final JsonObject productId = Json.createObjectBuilder().add("message", type).build();
        final JsonObject properties = Json.createObjectBuilder().add("properties", productId).build();
        final JsonObject products = Json.createObjectBuilder().add("elasticsearch", properties).build();
        final JsonObject mappings = Json.createObjectBuilder().add("mappings", products).build();

        return mappings;
    }

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
        final String mappings = mappings().toString();
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
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:26.877Z", "2015-05-18T11:03:24.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]..message", containsInAnyOrder(" space in the beginning space in the middle", " space in the beginning space in the middle space in the end ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(2)),
                withJsonPath("$.responses[0].hits..message", hasSize(2)),
                withJsonPath("$.responses[0].hits.total", is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneLowerCaseKeywordAdminUser() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(0)),
                withJsonPath("$.responses[0].hits..message", hasSize(0)),
                withJsonPath("$.responses[0].hits.total", is(0))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordUpperCaseAdminUser() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("ADMINUSER"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T05:03:25.877Z", "2015-05-17T10:03:25.877Z", "2015-05-18T07:03:25.877Z", "2015-05-18T06:03:25.877Z", "2015-05-18T08:03:25.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]..message", containsInAnyOrder(
                        "TESTUSER1, ADMINUSER1, PASSWORD1, USERNAME1, USER1, PASSWORD1",
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ",
                        "TESTUSER12$1, ADMINUSER12$1, PASSWORD12$1, USERNAME12$1, USER12$1, PASSWORD12$1",
                        "TESTUSER12, ADMINUSER12, PASSWORD12, USERNAME12, USER12, PASSWORD12",
                        "TESTADMINUSER12$1, ADMINTESTYUSER12$1, PASSWORD12$1, USERNAME12$1, USER12$1, PASSWORD12$1")),

                withJsonPath("$.responses[0].hits..@timestamp", hasSize(5)),
                withJsonPath("$.responses[0].hits..message", hasSize(5)),
                withJsonPath("$.responses[0].hits.total", is(5))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordTes() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("tes"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-17T09:03:25.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]..message", containsInAnyOrder("testuser, adminpassword, username, user, password,")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0].hits.total", is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleKeywords() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());

        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-17T09:03:25.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("testuser, adminpassword, username, user, password,")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0].hits.total", is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneRegexAndOneKeyword() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("abcd"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:25.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-17T10:03:25.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-18T04:03:25.877Z",
                        "2015-05-18T01:03:25.877Z",
                        "2015-05-18T10:03:25.877Z",
                        "2015-05-18T11:03:25.877Z",
                        "2015-05-18T11:03:28.877Z")),

                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(
                        "10.12.244.123 10.12.34.34 ",
                        "10.12.34.34 12.12.34  ",
                        " 192.12.123.4 10.12.34  ",
                        "10.12.34.34 12.12.34  ",
                        "$99.99.99.99 12.12.34  ",
                        "10$12$34$34 12.12.34.34 ",
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ",
                        "TES, ADMIN, PASS, USERNAME12$1, USER12$1, PASSWORD12$1 127.0.0.1:41388",
                        "99.99.99.99 12.12.34  ",
                        "10 12 34 34 12.12.34.34 ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(10)),
                withJsonPath("$.responses[0].hits..message", hasSize(10)),
                withJsonPath("$.responses[0].hits.total", is(10))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeyword() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder(
                        "2015-05-18T04:03:25.877Z", "2015-05-18T10:03:25.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-17T10:03:25.877Z",
                        "2015-05-18T11:03:28.877Z", "2015-05-18T01:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:25.877Z"
                )),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ", "TES, ADMIN, PASS, USERNAME12$1, USER12$1, PASSWORD12$1 127.0.0.1:41388", "10 12 34 34 12.12.34.34 ",
                        "10$12$34$34 12.12.34.34 ", "99.99.99.99 12.12.34  ", "$99.99.99.99 12.12.34  ", "testuser, adminpassword, username, user, password,", " 192.12.123.4 10.12.34  ",
                        "10.12.34.34 12.12.34  ", "10.12.244.123 10.12.34.34 ", "10.12.34.34 12.12.34  "
                )),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(11)),
                withJsonPath("$.responses[0].hits..message", hasSize(11)),
                withJsonPath("$.responses[0].hits.total", is(11))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeywordLimitByTime() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:05:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder(
                        "2015-05-17T10:03:25.877Z", "2015-05-18T04:03:25.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T01:03:25.877Z", "2015-05-18T10:03:25.877Z",
                        "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:25.877Z"

                )),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ", "10 12 34 34 12.12.34.34 ",
                        "10.12.244.123 10.12.34.34 ", "10$12$34$34 12.12.34.34 ", "10.12.34.34 12.12.34  ", "99.99.99.99 12.12.34  ",
                        " 192.12.123.4 10.12.34  ", "10.12.34.34 12.12.34  ", "$99.99.99.99 12.12.34  ", "TES, ADMIN, PASS, USERNAME12$1, USER12$1, PASSWORD12$1 127.0.0.1:41388"
                )),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(10)),
                withJsonPath("$.responses[0].hits..message", hasSize(10)),
                withJsonPath("$.responses[0].hits.total", is(10))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBegining() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginining"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-18T11:03:22.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(".special$characterbeginining")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0].hits..total", hasSize(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("xspecial$characterx."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-18T11:03:23.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("xspecial$characterx.")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0].hits..total", hasSize(1))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBeginingAndEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginingandend."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-18T11:03:22.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(".special$characterbeginingandend.")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0].hits..total", hasSize(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheMiddle() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("space in the middle"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:27.877Z","2015-05-18T11:03:25.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:26.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("space in the middle", "vspace in the middlev"," space in the beginning space in the middle", "space in the middle space at the end ", " space in the beginning space in the middle space in the end ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(5)),
                withJsonPath("$.responses[0].hits..message", hasSize(5)),
                withJsonPath("$.responses[0].hits.total", is(5))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingMiddleAndEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning space in the middle space in the end "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-18T11:03:26.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder(" space in the beginning space in the middle space in the end ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0]..hits.total", hasSize(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInMiddle() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("space. in the middle with special characters"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]._source.@timestamp", containsInAnyOrder("2015-05-18T11:03:28.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("space. in the middle with special characters ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(1)),
                withJsonPath("$.responses[0].hits..message", hasSize(1)),
                withJsonPath("$.responses[0]..hits..total", hasSize(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIP() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);
        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("$99.99.99.99 12.12.34  ", "99.99.99.99 12.12.34  ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(2)),
                withJsonPath("$.responses[0].hits..message", hasSize(2)),
                withJsonPath("$.responses[0].hits.total", is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIPWithspecialCharacterInBegining() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("$99.99.99.99 12.12.34  ", "99.99.99.99 12.12.34  ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(2)),
                withJsonPath("$.responses[0].hits..message", hasSize(2)),
                withJsonPath("$.responses[0].hits.total", is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedforContainingWords() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("containingword "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z");

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final KibanaQueryBuilder kibanaQueryBuilder = new KibanaQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient());
        final Response response = logSearcher.search(kibanaQueryBuilder);

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:27.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("containingword ", "prefixedcontainingword ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(2)),
                withJsonPath("$.responses[0].hits..message", hasSize(2)),
                withJsonPath("$.responses[0].hits.total", is(2))
                ))
        );
    }

    protected SearchParameters setUpSearchParameters(String configPath, String searchPath, String userListPath, String responsePath) {
        return new SearchParameters(configPath, searchPath, userListPath, responsePath);
    }

    private static class PluginConfigurableNode extends Node {
        public PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, null), classpathPlugins);
        }
    }
}