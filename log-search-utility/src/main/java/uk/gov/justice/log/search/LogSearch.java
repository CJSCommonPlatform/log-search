package uk.gov.justice.log.search;


import java.net.URL;
import java.util.Properties;

public interface LogSearch {

    void search(final URL url, final Properties properties) throws KeywordFoundException;

}
