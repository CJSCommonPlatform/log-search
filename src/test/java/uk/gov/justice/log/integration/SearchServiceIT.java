package uk.gov.justice.log.integration;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;
import static uk.gov.justice.log.utils.SearchConstants.HITS_MESSAGE_LIST;
import static uk.gov.justice.log.utils.SearchConstants.HITS_MESSAGE_SIZE;
import static uk.gov.justice.log.utils.SearchConstants.HITS_TIMESTAMP_LIST;
import static uk.gov.justice.log.utils.SearchConstants.HITS_TIMESTAMP_SIZE;
import static uk.gov.justice.log.utils.SearchConstants.HITS_TOTAL;

import uk.gov.justice.common.AbstractIntegrationTest;
import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchService;
import uk.gov.justice.log.utils.PropertyReader;
import uk.gov.justice.log.utils.SearchConfig;
import uk.gov.justice.log.utils.ValidationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchServiceIT extends AbstractIntegrationTest {
    private final Logger LOGGER = LoggerFactory.getLogger(SearchServiceIT.class);


    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingOnly() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> responses = logSearcher.search();
        final String responseString = EntityUtils.toString(responses.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:26.877Z", "2015-05-18T11:03:24.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(" space in the beginning space in the middle", " space in the beginning space in the middle space in the end ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(2)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(2)),
                withJsonPath(HITS_TOTAL, is(2))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneLowerCaseKeywordAdminUser() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();
        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(0)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(0)),
                withJsonPath(HITS_TOTAL, is(0))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordUpperCaseAdminUser() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("ADMINUSER"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();
        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T05:03:25.877Z", "2015-05-17T10:03:25.877Z", "2015-05-18T07:03:25.877Z", "2015-05-18T06:03:25.877Z", "2015-05-18T08:03:25.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(
                        "TESTUSER1, ADMINUSER1, PASSWORD1, USERNAME1, USER1, PASSWORD1",
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ",
                        "TESTUSER12$1, ADMINUSER12$1, PASSWORD12$1, USERNAME12$1, USER12$1, PASSWORD12$1",
                        "TESTUSER12, ADMINUSER12, PASSWORD12, USERNAME12, USER12, PASSWORD12",
                        "TESTADMINUSER12$1, ADMINTESTYUSER12$1, PASSWORD12$1, USERNAME12$1, USER12$1, PASSWORD12$1")),

                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(5)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(5)),
                withJsonPath(HITS_TOTAL, is(5))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneKeywordTes() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("tes"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();
        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);

        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-17T09:03:25.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("testuser, adminpassword, username, user, password,")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleKeywords() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), null, 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-17T09:03:25.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("testuser, adminpassword, username, user, password,")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
        final String responseString2 = EntityUtils.toString(response.get(1).getEntity());
        LOGGER.info("test12:" + System.getProperty("line.separator") + responseString2);
        assertThat(responseString2, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(0)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(0)),
                withJsonPath(HITS_TOTAL, is(0))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeyword() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-16T09:03:23.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString1 = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info("test11:" + System.getProperty("line.separator") + responseString1);

        assertThat(responseString1, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder(
                        "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T01:03:25.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-17T10:03:25.877Z", "2015-05-18T10:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T04:03:25.877Z"
                )),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ", "TES, ADMIN, PASS, USERNAME12$1, USER12$1, PASSWORD12$1 127.0.0.1:41388", "10 12 34 34 12.12.34.34 ",
                        "10$12$34$34 12.12.34.34 ", "99.99.99.99 12.12.34  ", "$99.99.99.99 12.12.34  ", " 192.12.123.4 10.12.34  ",
                        "10.12.34.34 12.12.34  ", "10.12.244.123 10.12.34.34 ", "10.12.34.34 12.12.34  "
                )),

                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(10)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(10)),
                withJsonPath(HITS_TOTAL, is(10))
                ))
        );
        final String responseString2 = EntityUtils.toString(response.get(1).getEntity());
        LOGGER.info("test12:" + System.getProperty("line.separator") + responseString2);

        assertThat(responseString2, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-17T09:03:25.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("testuser, adminpassword, username, user, password,")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
        final String responseString3 = EntityUtils.toString(response.get(2).getEntity());
        LOGGER.info("test13:" + System.getProperty("line.separator") + responseString2);
        assertThat(responseString3, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(0)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(0)),
                withJsonPath(HITS_TOTAL, is(0))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingMultipleRegexAndMultipleKeywordLimitByTime() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("testuser", "adminuser"), Arrays.asList(ipRegex), 0, "2015-05-17T09:05:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder(
                        "2015-05-17T10:03:25.877Z", "2015-05-18T04:03:25.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T01:03:25.877Z", "2015-05-18T10:03:25.877Z",
                        "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z", "2015-05-18T11:03:25.877Z"

                )),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(
                        "TESTUSER, ADMINUSER, ADMINPASSWORD, USERNAME, USER, PASSWORD 12.21.32.45 ", "10 12 34 34 12.12.34.34 ",
                        "10.12.244.123 10.12.34.34 ", "10$12$34$34 12.12.34.34 ", "10.12.34.34 12.12.34  ", "99.99.99.99 12.12.34  ",
                        " 192.12.123.4 10.12.34  ", "10.12.34.34 12.12.34  ", "$99.99.99.99 12.12.34  ", "TES, ADMIN, PASS, USERNAME12$1, USER12$1, PASSWORD12$1 127.0.0.1:41388"
                )),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(10)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(10)),
                withJsonPath(HITS_TOTAL, is(10))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBegining() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginining"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:22.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(".special$characterbeginining")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInEnd() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("xspecial$characterx."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:23.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("xspecial$characterx.")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );

    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInBeginingAndEnd() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList(".special$characterbeginingandend."), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:22.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(".special$characterbeginingandend.")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheMiddle() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("space in the middle"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:27.877Z", "2015-05-18T11:03:25.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:24.877Z", "2015-05-18T11:03:26.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("space in the middle", "vspace in the middlev", " space in the beginning space in the middle", "space in the middle space at the end ", " space in the beginning space in the middle space in the end ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(5)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(5)),
                withJsonPath(HITS_TOTAL, is(5))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpaceInTheBeginingMiddleAndEnd() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning space in the middle space in the end "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:26.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(" space in the beginning space in the middle space in the end ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWordsWithSpecialCharactersInMiddle() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("space. in the middle with special characters"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info("test:" + System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:28.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("space. in the middle with special characters ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(1)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(1)),
                withJsonPath(HITS_TOTAL, is(1))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIP() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());

        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("$99.99.99.99 12.12.34  ", "99.99.99.99 12.12.34  ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(2)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(2)),
                withJsonPath(HITS_TOTAL, is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchSpecificIPWithspecialCharacterInBegining() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("99.99.99.99"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:28.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("$99.99.99.99 12.12.34  ", "99.99.99.99 12.12.34  ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(2)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(2)),
                withJsonPath(HITS_TOTAL, is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedforContainingWords() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("containingword "), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:40.879Z", SEARCH_CRITERIA_FILE_PATH);

        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> response = logSearcher.search();

        final String responseString = EntityUtils.toString(response.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString);
        assertThat(responseString, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:28.877Z", "2015-05-18T11:03:27.877Z")),
                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder("containingword ", "prefixedcontainingword ")),
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(2)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(2)),
                withJsonPath(HITS_TOTAL, is(2))
                ))
        );
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchedUsingOneRegexAndOneKeyword() throws IOException, ValidationException {
        mockSetupForSearchCriteria(Arrays.asList("abcd"), Arrays.asList(ipRegex), 0, "2015-05-17T09:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        final PropertyReader propertyReader = new PropertyReader(setUpSearchParameters(CONFIG_FILE_PATH, SEARCH_CRITERIA_FILE_PATH, null, null));
        searchCriteria = propertyReader.searchCriteria();
        restConfig = propertyReader.restConfig();

        final ElasticSearchQueryBuilder elasticSearchQueryBuilder = new ElasticSearchQueryBuilder(searchCriteria);

        final SearchService logSearcher = new SearchService(restClientFactory(), elasticSearchQueryBuilder);
        final List<Response> responses = logSearcher.search();

        final String responseString1 = EntityUtils.toString(responses.get(0).getEntity());
        LOGGER.info(System.getProperty("line.separator") + responseString1);
        assertThat(responseString1, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_LIST, containsInAnyOrder("2015-05-18T11:03:25.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-17T10:03:25.877Z",
                        "2015-05-18T11:03:28.877Z",
                        "2015-05-18T04:03:25.877Z",
                        "2015-05-18T01:03:25.877Z",
                        "2015-05-18T10:03:25.877Z",
                        "2015-05-18T11:03:25.877Z",
                        "2015-05-18T11:03:28.877Z")),

                withJsonPath(HITS_MESSAGE_LIST, containsInAnyOrder(
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
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(10)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(10)),
                withJsonPath(HITS_TOTAL, is(10))
                ))
        );
        final String responseString2 = EntityUtils.toString(responses.get(1).getEntity());
        LOGGER.info("hello" + System.getProperty("line.separator") + responseString2);

        assertThat(responseString2, isJson(allOf(
                withJsonPath(HITS_TIMESTAMP_SIZE, hasSize(0)),
                withJsonPath(HITS_MESSAGE_SIZE, hasSize(0)),
                withJsonPath(HITS_TOTAL, is(0))
                ))
        );
    }

    protected SearchConfig setUpSearchParameters(String configPath, String searchPath, String userListPath, String responsePath) {
        return new SearchConfig(configPath, searchPath, userListPath, responsePath, "yes");
    }
}