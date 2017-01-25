package uk.gov.justice.log.wrapper;


import uk.gov.justice.log.utils.ConnectionManager;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;

public class HttpAsyncClientBuilderWrapper implements RestClientBuilder.HttpClientConfigCallback {

    private final ConnectionManager connectionManager;

    public HttpAsyncClientBuilderWrapper(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(final HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return httpAsyncClientBuilder.setConnectionManager(this.connectionManager.connectionManager());
    }
}
