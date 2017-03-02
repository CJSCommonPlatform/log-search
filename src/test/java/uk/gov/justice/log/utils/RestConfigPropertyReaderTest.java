package uk.gov.justice.log.utils;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;

import net.minidev.json.parser.ParseException;
import org.junit.Test;

public class RestConfigPropertyReaderTest extends PropertyReaderTest{
    @Test
    public void shouldFailWhenHostNameMissingKey() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(null, HOST_SCHEME, 9, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Host name cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenHostNameMissingValue() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig("", HOST_SCHEME, 9, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Host name cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenHostNameProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getHostName(), is(HOST_NAME));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldFailWhenSchemeMissingKey() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, null, 9, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Scheme cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenSchemeMissingValue() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, "", 8080, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Scheme cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenSchemeProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");

        final PropertyReader reader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(reader.restConfig().getHostScheme(), is(HOST_SCHEME));
        assertThat(reader.errors().size(), is(0));
    }

    @Test
    public void shouldFailWhenHostPortMissingKey() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, null, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Port cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenHostPortMissingValue() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 0, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("Port cannot be empty or null", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenHostPortProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getHostPort(), is(8080));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortMissingKey() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortMissingValue() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, "");

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyPortProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyPort(), is(8000));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostMissingKey() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostMissingValue() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 0, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyPort(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenProxyHostProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.restConfig().getProxyHost(), is(PROXY_HOST));
        assertThat(propertyReader.errors().size(), is(0));
    }

}
