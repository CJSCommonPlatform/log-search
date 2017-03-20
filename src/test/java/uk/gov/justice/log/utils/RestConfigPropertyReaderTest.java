package uk.gov.justice.log.utils;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForConfig;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class RestConfigPropertyReaderTest extends PropertyReaderTest {
    @Test
    public void shouldFailWhenHostNameMissingKey() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Host name cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(null, HOST_SCHEME, 9, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Host name cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenHostNameMissingValue() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Host name cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig("", HOST_SCHEME, 9, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Host name cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenHostNameProvided() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getHostName(), is(HOST_NAME));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldFailWhenSchemeMissingKey() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Scheme cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, null, 9, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Scheme cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenSchemeMissingValue() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Scheme cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, "", 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Scheme cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenSchemeProvided() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader reader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(reader.restConfig().getHostScheme(), is(HOST_SCHEME));
        assertThat(reader.errors().size(), is(0));
    }

    @Test
    public void shouldFailWhenHostPortMissingKey() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Port cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, null, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Port cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenHostPortMissingValue() throws IOException, ValidationException {
        expectedExption.expect(ValidationException.class);
        expectedExption.expectMessage("[Port cannot be empty or null]");

        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 0, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Port cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenHostPortProvided() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getHostPort(), is(8080));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortMissingKey() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortMissingValue() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "", CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortProvided() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, null, CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyPort(), is(8000));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostMissingKey() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null, CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostMissingValue() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null, CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostProvided() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO, SEARCH_CRITERIA_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST, CONFIG_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_PATH, null, null));

        assertThat(propertyReader.restConfig().getProxyHost(), is(PROXY_HOST));
        assertThat(propertyReader.errors().size(), is(0));
    }

}
