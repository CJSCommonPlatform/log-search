package uk.gov.justice.log.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String HOST_SCHEME = "http";
    private static final String HOST_NAME = "localhost";
    private static final String PROXY_HOST = "proxy_host";
    private static final String FROM = "2015-05-17T09:03:25.877Z";
    private static final String TO = "2015-05-18T07:03:25.877Z";
    private static final String CONFIG_PATH = "src/test/resources/test-config.yaml";
    private static final String SEARCH_PATH = "src/test/resources/test-search.yaml";

    @Rule
    public ExpectedException expectedExption = ExpectedException.none();

    @Mock
    private RestConfig restConfig;

    @AfterClass
    public static void destroy() {
        new File(CONFIG_PATH).delete();
        new File(SEARCH_PATH).delete();
    }

    @Test
    public void shouldFailWhenHostNameMissingKey() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Host name cannot be empty or null");

        mockSetupForConfig(null, HOST_SCHEME, 9, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldFailWhenHostNameMissingValue() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Host name cannot be empty or null");

        mockSetupForConfig("", HOST_SCHEME, 9, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldPassWhenHostNameProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");
        PropertyReader reader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(reader.restConfig().getHostName(), is(HOST_NAME));
    }

    @Test
    public void shouldFailWhenSchemeMissingKey() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Scheme cannot be empty or null");

        mockSetupForConfig(HOST_NAME, null, 9, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldFailWhenSchemeMissingValue() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Scheme cannot be empty or null");

        mockSetupForConfig(HOST_NAME, "", 8080, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldPassWhenSchemeProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");
        final PropertyReader reader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(reader.restConfig().getHostScheme(), is(HOST_SCHEME));
    }

    @Test
    public void shouldFailWhenHostPortMissingKey() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Port cannot be empty or null");

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, null, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldFailWhenHostPortMissingValue() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Port cannot be empty or null");

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 0, 0, "");
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldPassWhenHostPortProvided() throws IOException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getHostPort(), is(8080));
    }

    @Test
    public void shouldPassWhenProxyPortMissingKey() throws IOException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortMissingValue() throws IOException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, null);
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getProxyPort(), is(8000));
    }

    @Test
    public void shouldPassWhenProxyHostMissingKey() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null);
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getProxyPort(), is(0));

    }

    @Test
    public void shouldPassWhenProxyHostMissingValue() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null);
        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);

        assertThat(propertyReader.restConfig().getProxyHost(), is(PROXY_HOST));
    }

    @Test
    public void shouldPassWhenKeywordsProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);

        assertThat(propertyReader.searchCriteria().getKeywords().size(), is(3));
        assertThat(propertyReader.searchCriteria().getKeywords().get(0), is("key1"));
        assertThat(propertyReader.searchCriteria().getKeywords().get(1), is("key2"));
        assertThat(propertyReader.searchCriteria().getKeywords().get(2), is("key3"));
    }

    @Test
    public void shouldFailWhenKeywordsNotProvided() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("Search Keywords cannot be empty or null");

        mockSetupForSearchCriteria(null, null, 0, FROM, TO);
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldNotTrimWhenKeywordsContainSpaces() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" key1 ", " key2 "), null, 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, "");

        assertThat(propertyReader.searchCriteria().getKeywords().size(), is(2));
        assertThat(propertyReader.searchCriteria().getKeywords().get(0), is(" key1 "));
        assertThat(propertyReader.searchCriteria().getKeywords().get(1), is(" key2 "));
    }

    @Test
    public void shouldPassWhenRegexesProvided() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat(propertyReader.searchCriteria().getRegexes().get(0), is("[2][0][2]"));
        assertThat(propertyReader.searchCriteria().getRegexes().get(1), is("[4][0][0]"));
    }

    @Test
    public void shouldPassWhenBothFilesFound() throws IOException {
        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldPassWhenNoFromOrToTimeEntered() throws IOException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, null, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);

        assertThat(propertyReader.searchCriteria().getFromTime(), nullValue());
        assertThat(propertyReader.searchCriteria().getToTime(), nullValue());
    }

    @Test
    public void shouldFailWhenOnlyValidToTimeEntered() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("From time and to time both should be entered using ISO_8601 format \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, null, TO);

        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldFailWhenOnlyValidFromTimeEntered() throws IOException {
        expectedExption.expect(IllegalArgumentException.class);
        expectedExption.expectMessage("From time and to time both should be entered using ISO_8601 format \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, null);

        new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);
    }

    @Test
    public void shouldPassWhenValidFromTimeAndToTimeEntered() throws IOException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null);

        assertThat(propertyReader.searchCriteria().getFromTime(), is(FROM));
        assertThat(propertyReader.searchCriteria().getToTime(), is(TO));
        assertThat(propertyReader.searchCriteria().getDurationMinutes(), is(0));
    }

    private void mockSetupForConfig(final String hostName,
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

    private void mockSetupForSearchCriteria(final List<String> keywords,
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
}