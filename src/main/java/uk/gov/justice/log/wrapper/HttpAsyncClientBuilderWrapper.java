package uk.gov.justice.log.wrapper;


import uk.gov.justice.log.utils.ConnectionManager;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;

/**
 * Will set up custom connection manager to avoid self signed certificate issues
 */
public class HttpAsyncClientBuilderWrapper implements RestClientBuilder.HttpClientConfigCallback {

    private final ConnectionManager connectionManager;

    /**
     * Instantiates a new Http async client builder wrapper.
     *
     * @param connectionManager the connection manager
     */
    public HttpAsyncClientBuilderWrapper(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(final HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return httpAsyncClientBuilder.setConnectionManager(this.connectionManager.connectionManager());
    }
}
