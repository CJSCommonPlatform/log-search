package uk.gov.justice.log.search;

import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;


public class LogSearchImpl implements LogSearch {

    @Inject
    KabanaRestClient restClient;

    public LogSearchImpl() {
    }

    @Override
    public void search(final URL url,final Properties properties) throws KeywordFoundException {
        restClient.searchKabana(url,properties);
    }
}
