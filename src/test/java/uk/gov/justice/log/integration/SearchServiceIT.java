package uk.gov.justice.log.integration;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;

import uk.gov.justice.common.AbstractIntegrationTest;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.SearchConfig;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchServiceIT extends AbstractIntegrationTest {
    private final Logger LOGGER = LoggerFactory.getLogger(SearchServiceIT.class);


    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingOnly() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();
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
        mockSetupForSearchCriteria(Arrays.asList("adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();
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
        mockSetupForSearchCriteria(Arrays.asList("ADMINUSER"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();
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
        mockSetupForSearchCriteria(Arrays.asList("tes"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();
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
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("abcd"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:05:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginining"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("xspecial$characterx."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginingandend."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("space in the middle"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath("$.responses[0].hits.hits[*]..@timestamp", containsInAnyOrder("2015-05-18T11:03:27.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:26.877Z")),
                withJsonPath("$.responses[0].hits.hits[*]._source.message", containsInAnyOrder("space in the middle", "vspace in the middlev", " space in the beginning space in the middle", "space in the middle space at the end ", " space in the beginning space in the middle space in the end ")),
                withJsonPath("$.responses[0].hits..@timestamp", hasSize(5)),
                withJsonPath("$.responses[0].hits..message", hasSize(5)),
                withJsonPath("$.responses[0].hits.total", is(5))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingMiddleAndEnd() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning space in the middle space in the end "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("space. in the middle with special characters"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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
        mockSetupForSearchCriteria(Arrays.asList("containingword "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClient(), elasticSearchQueryBuilder);
        final Response response = logSearcher.search();

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

    protected SearchConfig setUpSearchParameters(String configPath, String searchPath, String userListPath, String responsePath) {
        return new SearchConfig(configPath, searchPath, userListPath, responsePath,"yes");
    }
}