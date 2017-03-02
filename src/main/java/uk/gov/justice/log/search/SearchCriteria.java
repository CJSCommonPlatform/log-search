package uk.gov.justice.log.search;

import uk.gov.justice.log.utils.CommonConstant;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteria {
    private int durationMinutes;
    private List<String> keywords;
    private List<String> regexes = new ArrayList<>();
    private int responseSize;
    private String fromTime;
    private String toTime;
    private boolean fromToTimeSet;

    public SearchCriteria(final int durationMinutes,
                          final List<String> keywords,
                          final List<String> regexes,
                          final int responseSize,

                          final String fromTime,
                          final String toTime) {
        this.durationMinutes = durationMinutes;
        this.keywords = keywords;
        this.regexes = regexes;
        this.responseSize = responseSize;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    public SearchCriteria() {
    }

    public boolean isFromToTimeSet() {
        return fromToTimeSet;
    }

    public void setFromToTimeSet(boolean fromToTimeSet) {
        this.fromToTimeSet = fromToTimeSet;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRegexes() {
        return regexes;
    }

    public void setRegexes(List<String> regexes) {
        this.regexes = regexes;
    }

    public int getResponseSize() {
        if (responseSize == 0) {
            return CommonConstant.DEFAULT_RESPONSE_OUTPUT_SIZE;
        }
        return responseSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }

    public void addKeyword(String name) {
        keywords.add(name);
    }
}
