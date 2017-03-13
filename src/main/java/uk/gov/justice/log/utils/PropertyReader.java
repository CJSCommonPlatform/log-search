package uk.gov.justice.log.utils;

import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class PropertyReader {

    private final SearchConfig searchConfig;
    private final List<String> errors = new ArrayList<>();
    private SearchCriteria searchCriteria;
    private RestConfig restConfig;

    public PropertyReader(final SearchConfig searchConfig) {
        this.searchConfig = searchConfig;
        createAndValidate();
        if (errors().size() > 0) {
            throw new IllegalArgumentException(errors().toString());
        }
    }

    private void createAndValidate() {
        createAndValidateRestConfig();
        createAndValidateSearchCriteria();
        if (searchConfig.getUserListFilePath() != null)
            includeUserListIntoSearchCriteria();
    }

    private void includeUserListIntoSearchCriteria() {
        final String userListFilePath = searchConfig.getUserListFilePath();
        if (userListFilePath != null) {
            try {
                try (InputStream in = Files.newInputStream(Paths.get(userListFilePath))) {
                    Scanner inputStream = new Scanner(in);
                    while(inputStream.hasNext()){
                        String data = inputStream.next();
                        if (searchCriteria != null) {
                            searchCriteria.addKeyword(data);
                       }
                    }
                }
            } catch (IOException e) {
                errors.add("User list file is not present at given location: " + userListFilePath);
            }
        }
    }

    private void createAndValidateRestConfig() {
        final String configFilePath = searchConfig.getConfigFilePath();
        try {
            try (InputStream in = Files.newInputStream(Paths.get(configFilePath))) {
                final Yaml yaml = new Yaml();
                restConfig = yaml.loadAs(in, RestConfig.class);
            }
            validateRestConfig();

        } catch (IOException e) {
            errors.add("config.yaml is not present at given location: " + configFilePath);
        }
    }

    private void createAndValidateSearchCriteria() {
        final String searchCriteriaPath = searchConfig.getSearchCriteriaPath();
        try {
            try (InputStream in = Files.newInputStream(Paths.get(searchCriteriaPath))) {
                final Yaml yaml = new Yaml();
                searchCriteria = yaml.loadAs(in, SearchCriteria.class);
            }
            validateSearchCriteria();
        } catch (IOException e) {
            errors.add("criteria.yaml is not present at given location: " + searchCriteriaPath);
        }
    }

    public void validateRestConfig() {
        validateHostName();
        validateHostPort();
        validateHostScheme();
        validateHostAndProxyPort();
    }

    public void validateSearchCriteria() {
        validateKeywords();
        validateDurationAndFromAndToTime();
    }

    private void validateHostName() {
        if (StringUtils.isEmpty(restConfig.getHostName())) {
            errors.add("Host name cannot be empty or null");
        }
    }

    private void validateHostPort() {
        if (restConfig.getHostPort() == null || restConfig.getHostPort() <= 0) {
            errors.add("Port cannot be empty or null");
        }
    }

    private void validateHostScheme() {
        if (StringUtils.isEmpty(restConfig.getHostScheme())) {
            errors.add("Scheme cannot be empty or null");
        }
    }

    private void validateHostAndProxyPort() {
        if (!StringUtils.isEmpty(restConfig.getHostName())) {
            if (restConfig.getProxyPort() < 0) {
                errors.add("Proxy port has wrong value");
            }
        }
    }

    private void validateKeywords() {
        if (searchCriteria.getKeywords() == null || searchCriteria.getKeywords().contains(null)) {
            errors.add("Search Keywords cannot be empty or null");
        } else {
            searchCriteria.setKeywords(searchCriteria.getKeywords().stream().collect(Collectors.toList()));
        }
    }

    private void validateDurationAndFromAndToTime() {
        boolean validFromTime = false;
        boolean validToTime = false;
        boolean validDuration = false;
        if (searchCriteria.getDurationMinutes() > 0) {
            validDuration = true;
        }
        if (!StringUtils.isEmpty(searchCriteria.getFromTime()) && validISODate(searchCriteria.getFromTime())) {
            validFromTime = true;
        }
        if (!StringUtils.isEmpty(searchCriteria.getToTime()) && validISODate(searchCriteria.getToTime())) {
            validToTime = true;
        }
        if (!validFromTime && validToTime || validFromTime && !validToTime) {
            errors.add("From time and to time both should be entered using ISO_8601 format '" + ZonedDateTimes.ISO_8601 + "'");
        }
        if (validFromTime && validToTime) {
            searchCriteria.setFromToTimeSet(true);
        }
        if (validDuration && searchCriteria.isFromToTimeSet()) {
            errors.add("Either duration or from/to time should only be entered ");
        }
    }

    private boolean validISODate(final String date) {
        try {
            ZonedDateTimes.fromString(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public RestConfig restConfig() {
        return restConfig;
    }

    public SearchCriteria searchCriteria() {
        return searchCriteria;
    }

    public List<String> errors() {
        return errors;
    }
}
