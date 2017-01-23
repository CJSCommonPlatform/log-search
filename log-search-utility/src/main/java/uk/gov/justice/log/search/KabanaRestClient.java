package uk.gov.justice.log.search;


import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KabanaRestClient {
    
    private Logger LOGGER = LoggerFactory.getLogger(KabanaRestClient.class);

    public KabanaRestClient() {
    }

    public void searchKabana(final URL url, final Properties properties) {
        throw new KeywordFoundException("");
    }

    
}
