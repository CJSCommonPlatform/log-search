package uk.gov.justice.log.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minidev.json.parser.ParseException;
import org.hamcrest.junit.ExpectedException;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaml.snakeyaml.Yaml;

@RunWith(MockitoJUnitRunner.class)
public class PropertyReaderTest {

    protected static final String HOST_SCHEME = "http";
    protected static final String HOST_NAME = "localhost";
    protected static final String PROXY_HOST = "proxy_host";
    protected static final String FROM = "2015-05-17T09:03:25.877Z";
    protected static final String TO = "2015-05-18T07:03:25.877Z";

    protected static final String CONFIG_PATH = "src/test/resources/test-config.yaml";
    protected static final String SEARCH_PATH = "src/test/resources/test-search.yaml";
    protected static final String USER_LIST_PATH = "src/test/resources/user-list.yaml";
    protected static final String RESPONSE_PATH = "src/test/resources/output.html";

    @Rule
    public ExpectedException expectedExption = ExpectedException.none();

    @Mock
    private RestConfig restConfig;

    @AfterClass
    public static void destroy() {
        new File(CONFIG_PATH).delete();
        new File(SEARCH_PATH).delete();
        new File(USER_LIST_PATH).delete();
    }

    @Test
    public void shouldFailWhenUserListFileIsNotFound() throws IOException, ParseException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, "/Users/user.json", RESPONSE_PATH));

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat("User list file is not present at given location: " + "/Users/user.json", is(propertyReader.errors().get(0).trim()));
        assertThat(propertyReader.errors().size(), is(1));
    }

    @Test
    public void shouldFailWhenConfigFileIsNotFound() throws IOException, ParseException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);
        mockSetupForUserListFile();


        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters("/xyz", SEARCH_PATH, USER_LIST_PATH, RESPONSE_PATH));

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat(propertyReader.errors().get(0).trim(), is("config.yaml is not present at given location: " + "/xyz"));
        assertThat(propertyReader.errors().size(), is(1));
    }

    @Test
    public void shouldPassWhenUserListFileIsFound() throws IOException, ParseException {

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);
        final Map<String, String> userData = new HashMap<>();
        userData.put("u1", "p1");
        userData.put("u2", "p2");
        mockSetupForUserListFile();

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, USER_LIST_PATH, RESPONSE_PATH));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenBothFilesFound() throws IOException, ParseException {
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_PATH, SEARCH_PATH, null, RESPONSE_PATH));
        assertThat(propertyReader.errors().size(), is(0));
    }

    protected void mockSetupForConfig(final String hostName,
                                      final String hostScheme,
                                      final Integer hostPort,
                                      final int proxyPort,
                                      final String proxyHost) throws IOException {

        final Map<String, Object> data = new HashMap<>();
        data.put("hostName", hostName);
        data.put("hostScheme", hostScheme);
        data.put("hostPort", hostPort);
        data.put("proxyPort", proxyPort);
        data.put("proxyHost", proxyHost);

        new Yaml().dump(data, new FileWriter(CONFIG_PATH));
    }

    protected void mockSetupForSearchCriteria(final List<String> keywords,
                                              final List<String> regexes,
                                              final int durationMinutes,
                                              final String from,
                                              final String to) throws IOException {
        final Map<String, Object> data = new HashMap<>();
        data.put("keywords", keywords);
        data.put("regexes", regexes);
        data.put("durationMinutes", durationMinutes);
        data.put("fromTime", from);
        data.put("toTime", to);
        new Yaml().dump(data, new FileWriter(SEARCH_PATH));
    }

    protected void mockSetupForUserListFile() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ArrayNode array = mapper.createArrayNode();

        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode row = new ObjectNode(factory);
        row.put("user", "u1");
        row.put("pass", "p1");
        array.add(row);

        mapper.writeValue(new FileWriter(USER_LIST_PATH), array);
    }

    protected SearchParameters setUpSearchParameters(String configPath, String searchPath, String userListPath, String responsePath) {
        return new SearchParameters(configPath, searchPath, userListPath, responsePath);
    }

}