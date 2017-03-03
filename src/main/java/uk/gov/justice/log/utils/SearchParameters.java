package uk.gov.justice.log.utils;

public class SearchParameters {
    private final String responseOutputPath;
    private final String configFilePath;
    private final String userListFilePath;
    private final String searchCriteriaPath;

    public SearchParameters(String configFilePath, String searchCriteriaPath, String userListFilePath, String responseOutputPath) {
        this.responseOutputPath = responseOutputPath;
        this.configFilePath = configFilePath;
        this.userListFilePath = userListFilePath;
        this.searchCriteriaPath = searchCriteriaPath;
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
