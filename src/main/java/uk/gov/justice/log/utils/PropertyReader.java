package uk.gov.justice.log.utils;

import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.services.common.converter.ZonedDateTimes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class PropertyReader {

    private SearchCriteria searchCriteria;
    private RestConfig restConfig;
    private String responseOutputPath;
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public PropertyReader(final String configFilePath,
                          final String searchCriteriaPath,
                          final String outputFilePath) throws IOException {
        createAndValidateRestConfig(configFilePath);
        createAndValidateSearchCriteria(searchCriteriaPath);
        this.responseOutputPath = outputFilePath;

    }

    private void createAndValidateRestConfig(final String configFilePath) throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get(configFilePath))) {
            final Yaml yaml = new Yaml();
            restConfig = yaml.loadAs(in, RestConfig.class);
        }
        validateRestConfig();
    }

    private void createAndValidateSearchCriteria(final String searchCriteriaPath) throws IOException {
        try (InputStream in = Files.newInputStream(Paths.get(searchCriteriaPath))) {
            final Yaml yaml = new Yaml();
            searchCriteria = yaml.loadAs(in, SearchCriteria.class);
        }
        validateSearchCriteria();
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
            throw new IllegalArgumentException("Host name cannot be empty or null");
        }
    }

    private void validateHostPort() {
        if (restConfig.getHostPort() == null || restConfig.getHostPort() <= 0) {
            throw new IllegalArgumentException("Port cannot be empty or null");
        }
    }

    private void validateHostScheme() {
        if (StringUtils.isEmpty(restConfig.getHostScheme())) {
            throw new IllegalArgumentException("Scheme cannot be empty or null");
        }
    }

    private void validateHostAndProxyPort() {
        if (!StringUtils.isEmpty(restConfig.getHostName())) {
            if (restConfig.getProxyPort() < 0) {
                throw new IllegalArgumentException("Proxy port has wrong value");
            }
        }
    }

    private void validateKeywords() {
        if (searchCriteria.getKeywords() == null || searchCriteria.getKeywords().contains(null)) {
            throw new IllegalArgumentException("Search Keywords cannot be empty or null");
        }
        searchCriteria.setKeywords(searchCriteria.getKeywords().stream().collect(Collectors.toList()));
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
            throw new IllegalArgumentException("From time and to time both should be entered using ISO_8601 format \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\" ");
        }
        if (validFromTime && validToTime) {
            searchCriteria.setFromToTimeSet(true);
        }
        if (validDuration && searchCriteria.isFromToTimeSet()) {
            throw new IllegalArgumentException("Either duration or from/to time should only be entered ");

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

}
