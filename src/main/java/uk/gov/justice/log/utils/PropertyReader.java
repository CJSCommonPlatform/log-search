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
import java.util.stream.Collectors;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class PropertyReader {

    private final String responseOutputPath;
    private final String configFilePath;
    private final String userListFilePath;
    private final String searchCriteriaPath;
    private final List<String> errors = new ArrayList<>();
    private SearchCriteria searchCriteria;
    private RestConfig restConfig;

    public PropertyReader(final String configFilePath,
                          final String searchCriteriaPath,
                          final String userListFilePath,
                          final String outputFilePath) {
        this.responseOutputPath = outputFilePath;
        this.configFilePath = configFilePath;
        this.searchCriteriaPath = searchCriteriaPath;
        this.userListFilePath = userListFilePath;
        createAndValidate();
    }

    private void createAndValidate() {
        createAndValidateRestConfig();
        createAndValidateSearchCriteria();
        includeUserListIntoSearchCriteria();
    }

    private void includeUserListIntoSearchCriteria() {
        if (userListFilePath != null) {
            try {
                try (InputStream in = Files.newInputStream(Paths.get(userListFilePath))) {
                    final JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
                    final Object obj = parser.parse(in);
                    final JSONArray jsonArray = (JSONArray) obj;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject row = (JSONObject) jsonArray.get(i);
                        searchCriteria.addKeyword((String) row.get("user"));
                        searchCriteria.addKeyword((String) row.get("pass"));
                    }
                }
            } catch (IOException e) {
                errors.add("User list file is not present at given location: " + userListFilePath);
            } catch (ParseException e) {
                errors.add("User list has incorrect data resulting in parse error");
            }
        }
    }

    private void createAndValidateRestConfig() {
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
        try {
            try (InputStream in = Files.newInputStream(Paths.get(searchCriteriaPath))) {
                final Yaml yaml = new Yaml();
                searchCriteria = yaml.loadAs(in, SearchCriteria.class);
            }
            validateSearchCriteria();
        } catch (IOException e) {
            errors.add("criteria.yaml is not present at given location: " + configFilePath);
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
            errors.add("From time and to time both should be entered using ISO_8601 format \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\" ");
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

    public String responseOutputPath() {
        return responseOutputPath;
    }

    public List<String> errors() {
        return errors;
    }
}
