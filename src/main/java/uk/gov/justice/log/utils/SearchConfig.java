package uk.gov.justice.log.utils;

public class SearchConfig {
    private final String responseOutputPath;
    private final String configFilePath;
    private final String userListFilePath;
    private final String searchCriteriaPath;
    private final String displayConsoleMessages;

    public String getDisplayConsoleMessages() {
        return displayConsoleMessages;
    }

    public SearchConfig(final String configFilePath,
                        final String searchCriteriaPath,
                        final String userListFilePath,
                        final String responseOutputPath,

                        final String displayConsoleMessages) {
        this.configFilePath = configFilePath;
        this.searchCriteriaPath = searchCriteriaPath;
        this.userListFilePath = userListFilePath;
        this.responseOutputPath = responseOutputPath;
        this.displayConsoleMessages = displayConsoleMessages;
    }

    public String getResponseOutputPath() {
        return responseOutputPath;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public String getUserListFilePath() {
        return userListFilePath;
    }

    public String getSearchCriteriaPath() {
        return searchCriteriaPath;
    }
}
