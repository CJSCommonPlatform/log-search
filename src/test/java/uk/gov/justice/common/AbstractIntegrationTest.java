package uk.gov.justice.common;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForConfig;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;
import static uk.gov.justice.log.utils.SearchConstants.DELETE;
import static uk.gov.justice.log.utils.SearchConstants.ELASTIC_SEARCH_ALL;
import static uk.gov.justice.log.utils.SearchConstants.ELASTIC_SEARCH_CLUSTER_URL;
import static uk.gov.justice.log.utils.SearchConstants.POST;
import static uk.gov.justice.log.utils.SearchConstants.PUT;

import uk.gov.justice.log.factory.RestClientFactory;
import uk.gov.justice.log.integration.SearchServiceIT;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.RestConfig;
import uk.gov.justice.log.utils.SearchConfig;
import uk.gov.justice.log.utils.ValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.node.NodeValidationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIntegrationTest {
    protected static final String HOST_SCHEME = "http";
    protected static final String HOST_NAME = "localhost";
    protected static final int HOST_PORT = 9205;
    protected static final String ES_WORKING_DIR = "target/es";
    protected static final String SAMPLE_LOGS_PATH = "src/test/resources/products.json";


    protected static String CONFIG_FILE_PATH;
    protected static String SEARCH_CRITERIA_FILE_PATH;
    protected static String USER_LIST_FILE_PATH;
    protected static String RESULTS_FILE_PATH;

    protected static RestConfig restConfig;
    protected static Path tempPath;
    protected final String ipRegex = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    private final Logger LOGGER = LoggerFactory.getLogger(SearchServiceIT.class);
    protected SearchCriteria searchCriteria;

    @BeforeClass
    public static void startElasticsearch() throws IOException, ValidationException,InterruptedException {
        setFilePaths();
        removeOldDataDir(ES_WORKING_DIR);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList("testuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(new SearchConfig(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null,"true"));
        restConfig = propertyReader.restConfig();
        deleteAndCreateMapping();
        uploadSampleData();
        Thread.sleep(1000);
    }

    private static void setFilePaths() throws IOException {
        tempPath = Files.createTempDirectory("IT_");
        CONFIG_FILE_PATH = tempPath + "/test-config.yaml";
        SEARCH_CRITERIA_FILE_PATH = tempPath + "/search-criteria.yaml";
        RESULTS_FILE_PATH = tempPath + "/results.html";
        USER_LIST_FILE_PATH = tempPath + "/userlist.csv";
    }

    @AfterClass
    public static void destroy() throws IOException {
        Files.deleteIfExists(Paths.get(SEARCH_CRITERIA_FILE_PATH));
        Files.deleteIfExists(Paths.get(CONFIG_FILE_PATH));
        Files.deleteIfExists(Paths.get(USER_LIST_FILE_PATH));
        Files.deleteIfExists(Paths.get(RESULTS_FILE_PATH));
        Files.deleteIfExists(tempPath);
        removeOldDataDir(ES_WORKING_DIR);
    }

    private static void removeOldDataDir(String datadir) throws IOException {
        File dataDir = new File(datadir);
        if (dataDir.exists()) {
            FileSystemUtils.deleteSubDirectories(Paths.get(datadir));
        }
    }

    public static RestClient restClient() {
        RestClientFactory factory = new RestClientFactory(restConfig);
        return factory.restClient();
    }

    private static void uploadSampleData() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(SAMPLE_LOGS_PATH));
        final String logs = new String(encoded, "UTF-8");
        final String endpoint = ELASTIC_SEARCH_CLUSTER_URL + "_bulk?pretty";
        final HttpEntity data = new NStringEntity(logs, APPLICATION_JSON);
        restClient().performRequest(POST, endpoint, Collections.emptyMap(), data);
    }

    private static JsonObject mappings() {

        final JsonObjectBuilder type = Json.createObjectBuilder().add("type", "keyword");
        final JsonObjectBuilder productId = Json.createObjectBuilder().add("message", type);
        final JsonObjectBuilder properties = Json.createObjectBuilder().add("properties", productId);
        final JsonObjectBuilder products = Json.createObjectBuilder().add("elasticsearch", properties);
        final JsonObject mappings = Json.createObjectBuilder().add("mappings", products).build();
        return mappings;
    }

    private static void deleteAndCreateMapping() throws IOException {
        restClient().performRequest(DELETE, ELASTIC_SEARCH_ALL);

        final String mappings = mappings().toString();
        final HttpEntity mappingEntity = new NStringEntity(mappings, APPLICATION_JSON);

        restClient().performRequest(PUT, ELASTIC_SEARCH_CLUSTER_URL, Collections.emptyMap(), mappingEntity);
    }
}
