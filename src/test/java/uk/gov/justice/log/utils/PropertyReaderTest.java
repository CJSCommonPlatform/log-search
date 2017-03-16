package uk.gov.justice.log.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForConfig;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForUserListFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.hamcrest.junit.ExpectedException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyReaderTest {
    protected static final String HOST_SCHEME = "http";
    protected static final String HOST_NAME = "localhost";
    protected static final String PROXY_HOST = "proxy_host";
    protected static final String FROM = "2015-05-17T09:03:25.877Z";
    protected static final String TO = "2015-05-18T07:03:25.877Z";
    private final static String prefix = "PropertyReaderTest_";
    protected static String USER_LIST_PATH;
    protected static String CONFIG_FILE_PATH;
    protected static String SEARCH_CRITERIA_PATH;
    protected static String RESPONSE_PATH;
    private static Path tempPath;

    @Rule
    public ExpectedException expectedExption = ExpectedException.none();

    @Mock
    private RestConfig restConfig;

    @BeforeClass
    public static void setUp() throws IOException {
        tempPath = Files.createTempDirectory(prefix);
        USER_LIST_PATH = tempPath + "/test-userlist.csv";
        CONFIG_FILE_PATH = tempPath + "/test-config.yaml";
        SEARCH_CRITERIA_PATH = tempPath + "/search-criteria.yaml";
        RESPONSE_PATH = tempPath + "/results.html";
    }

    @AfterClass
    public static void destroy() throws IOException {
        Files.delete(Paths.get(USER_LIST_PATH));
        Files.delete(Paths.get(CONFIG_FILE_PATH));
        Files.delete(Paths.get(SEARCH_CRITERIA_PATH));
        Files.delete(tempPath);
    }


    @Test
    public void shouldFailWhenSearchCriteriaFileIsNotFound() throws IOException,ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[criteria.yaml is not present at given location: /xyz]");

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST, CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForUserListFile(USER_LIST_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, "/xyz", USER_LIST_PATH, RESPONSE_PATH));

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat("criteria.yaml is not present at given location: /xyz", is(propertyReader.errors().get(0).trim()));
        assertThat(propertyReader.errors().size(), is(1));
    }

    @Test
    public void shouldFailWhenConfigFileIsNotFound() throws IOException,ValidationException{
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[config.yaml is not present at given location: /xyz]");
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST, CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForUserListFile(USER_LIST_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters("/xyz", SEARCH_CRITERIA_PATH, USER_LIST_PATH, RESPONSE_PATH));

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat(propertyReader.errors().get(0).trim(), is("config.yaml is not present at given location: " + "/xyz"));
        assertThat(propertyReader.errors().size(), is(1));
    }

    @Test
    public void shouldFailWhenUserListFileIsNotFound() throws IOException,ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[User list file is not present at given location: /Users/user.json]");

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST, CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO, SEARCH_CRITERIA_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, "/Users/user.json", RESPONSE_PATH));

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat("User list file is not present at given location: " + "/Users/user.json", is(propertyReader.errors().get(0).trim()));
        assertThat(propertyReader.errors().size(), is(1));
    }

    @Test
    public void shouldPassWhenUserListFileIsFound() throws IOException,ValidationException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST, CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForUserListFile(USER_LIST_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH
                , USER_LIST_PATH, RESPONSE_PATH));

        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenBothFilesFound() throws IOException,ValidationException {
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH
                , null, null));

        assertThat(propertyReader.errors().size(), is(0));
    }

    protected SearchConfig setUpSearchParameters(String configPath, String searchPath, String userListPath, String responsePath) {
        return new SearchConfig(configPath, searchPath, userListPath, responsePath, "yes");
    }
}