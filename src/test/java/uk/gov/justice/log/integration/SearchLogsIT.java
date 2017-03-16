package uk.gov.justice.log.integration;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForConfig;
import static uk.gov.justice.common.TestMockDataFiles.mockSetupForSearchCriteria;
import static uk.gov.justice.log.utils.SearchConstants.MESSAGE_RESULT;

import uk.gov.justice.common.AbstractIntegrationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SearchLogsIT extends AbstractIntegrationTest {
    protected static String COMMAND;


    @Test
    public void shouldFailWhenConfigParameterNotPassed() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        COMMAND = "java -jar target/log-search.jar " + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("The following option is required: -config"));
    }

    @Test
    public void shouldFailWhenConfigFileNotFound() throws Exception {
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        Files.deleteIfExists(Paths.get(CONFIG_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar  " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("config.yaml is not present at given location:"));
    }

    @Test
    public void shouldFailWhenSearchCriteriaParameterNotPassed() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("The following option is required: -search"));
    }

    @Test
    public void shouldFailWhenSearchCriteriaFileNotFound() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        Files.deleteIfExists(Paths.get(SEARCH_CRITERIA_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("criteria.yaml is not present at given location:"));
    }

    @Test
    public void shouldFailWhenUserListParameterNotPassed() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, is(emptyString()));
    }

    @Test
    public void shouldFailWhenUserListFileNotFound() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        Files.deleteIfExists(Paths.get(USER_LIST_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH + " -userlist " + USER_LIST_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("User list file is not present at given location:"));
    }


    @Test
    public void shouldFailWhenUserListFileFound() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        Files.deleteIfExists(Paths.get(USER_LIST_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH + " -userlist " + USER_LIST_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, containsString("User list file is not present at given location:"));
    }

    @Test
    public void shouldFailWhenResultsParameterNotPassed() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, is(emptyString()));
    }

    @Test
    public void shouldCreateResultsIsFound() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        Files.deleteIfExists(Paths.get(RESULTS_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH + " -output " + RESULTS_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(Files.exists(Paths.get(RESULTS_FILE_PATH)), is(true));
    }

    @Test
    public void shouldCreateResultsIsDefaultLocation() throws Exception {
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);

        Files.deleteIfExists(Paths.get(RESULTS_FILE_PATH));
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;
        final Output output = execute(COMMAND);
        assertThat(Files.exists(Paths.get("results.html")), is(true));
    }

    @Test
    public void shouldFailReportingMultipleValidationFailuresBeforeSearching() throws Exception {

        mockSetupForConfig(HOST_NAME, null, 0, 0, "", CONFIG_FILE_PATH);
        mockSetupForSearchCriteria(Arrays.asList(" log output"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;

        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, is(notNullValue()));
    }

    @Test
    public void shouldFindCorrectHitsWhenSearchWords() throws IOException {
        mockSetupForSearchCriteria(Arrays.asList(" space in the beginning"), null, 0, "2015-05-17T06:03:25.877Z", "2015-05-18T11:03:28.877Z", SEARCH_CRITERIA_FILE_PATH);
        mockSetupForConfig(HOST_NAME, HOST_SCHEME, HOST_PORT, 0, "", CONFIG_FILE_PATH);
        COMMAND = "java -jar target/log-search.jar " + " -config " + CONFIG_FILE_PATH + " -search " + SEARCH_CRITERIA_FILE_PATH;

        final Output output = execute(COMMAND);
        assertThat(output.errorOutput, emptyString());
        System.out.println(output.standardOutput);
        final String[] response = output.standardOutput.split(MESSAGE_RESULT);
        assertThat(response[1], isJson(allOf(
                withJsonPath("$.hits", is(2)),
                withJsonPath("$.messages[*]", containsInAnyOrder(" space in the beginning space in the middle", " space in the beginning space in the middle space in the end "))
                ))
        );
    }

    private Output execute(final String cmd) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec(cmd);
        return new Output(IOUtils.toString(process.getInputStream()), IOUtils.toString(process.getErrorStream()));
    }


    private static class Output {
        private String standardOutput;
        private String errorOutput;

        public Output(final String standardOutput, final String errorOutput) {
            this.standardOutput = standardOutput;
            this.errorOutput = errorOutput;
        }

        public String standardOutput() {
            return standardOutput;
        }

        public String errorOutput() {
            return errorOutput;
        }
    }

}
