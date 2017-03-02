package uk.gov.justice.log.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.ISO_8601;

import java.io.IOException;
import java.util.Arrays;

import net.minidev.json.parser.ParseException;
import org.junit.Test;


public class SearchCriteriaPropertyReaderTest extends PropertyReaderTest {
    @Test
    public void shouldPassWhenKeywordsProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1", "key2", "key3"), null, 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.searchCriteria().getKeywords().size(), is(3));
        assertThat(propertyReader.searchCriteria().getKeywords().get(0), is("key1"));
        assertThat(propertyReader.searchCriteria().getKeywords().get(1), is("key2"));
        assertThat(propertyReader.searchCriteria().getKeywords().get(2), is("key3"));
    }

    @Test
    public void shouldFailWhenKeywordsNotProvided() throws IOException, ParseException {

        mockSetupForSearchCriteria(null, null, 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);
        assertThat(propertyReader.errors().size(), is(1));
        assertThat(propertyReader.errors().get(0), is("Search Keywords cannot be empty or null"));
    }

    @Test
    public void shouldNotTrimWhenKeywordsContainSpaces() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList(" key1 ", " key2 "), null, 0, FROM, TO);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.searchCriteria().getKeywords().size(), is(2));
        assertThat(propertyReader.searchCriteria().getKeywords().get(0), is(" key1 "));
        assertThat(propertyReader.searchCriteria().getKeywords().get(1), is(" key2 "));
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldPassWhenRegexesProvided() throws IOException, ParseException {
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.searchCriteria().getRegexes().size(), is(2));
        assertThat(propertyReader.searchCriteria().getRegexes().get(0), is("[2][0][2]"));
        assertThat(propertyReader.searchCriteria().getRegexes().get(1), is("[4][0][0]"));
    }

    @Test
    public void shouldPassWhenNoFromOrToTimeEntered() throws IOException, ParseException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, null, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.searchCriteria().getFromTime(), nullValue());
        assertThat(propertyReader.searchCriteria().getToTime(), nullValue());
        assertThat(propertyReader.errors().size(), is(0));
    }

    @Test
    public void shouldFailWhenOnlyValidToTimeEntered() throws IOException, ParseException {

        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, null, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("From time and to time both should be entered using ISO_8601 format \"" + ISO_8601 + "\"", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldFailWhenOnlyValidFromTimeEntered() throws IOException, ParseException {

        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, null);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.errors().size(), is(1));
        assertThat("From time and to time both should be entered using ISO_8601 format \"" + ISO_8601 + "\"", is(propertyReader.errors().get(0).trim()));
    }

    @Test
    public void shouldPassWhenValidFromTimeAndToTimeEntered() throws IOException, ParseException {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, 8080, 8000, PROXY_HOST);
        mockSetupForSearchCriteria(Arrays.asList("key1"), Arrays.asList("[2][0][2]", "[4][0][0]"), 0, FROM, TO);

        final PropertyReader propertyReader = new PropertyReader(CONFIG_PATH, SEARCH_PATH, null, null);

        assertThat(propertyReader.searchCriteria().getFromTime(), is(FROM));
        assertThat(propertyReader.searchCriteria().getToTime(), is(TO));
        assertThat(propertyReader.searchCriteria().getDurationMinutes(), is(0));
        assertThat(propertyReader.errors().size(), is(0));
    }

}
