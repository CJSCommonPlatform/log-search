package uk.gov.justice.log.factory;

import static uk.gov.justice.log.utils.SearchConstants.MAX_RETRY_TIMEOUT_MILLIS;

import uk.gov.justice.log.utils.ConnectionManager;
import uk.gov.justice.log.utils.RestConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

/**
 * Builds ElasticSearch RestClient
 */
public class RestClientFactory implements RestClientBuilder.RequestConfigCallback, RestClientBuilder.HttpClientConfigCallback {

    private final RestConfig restConfig;
    private final ConnectionManager connectionManager;

    public RestClientFactory(final RestConfig restConfig) {
        this.restConfig = restConfig;
        this.connectionManager = new ConnectionManager(restConfig.getMaximumConnections());
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(final HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return httpAsyncClientBuilder.setConnectionManager(this.connectionManager.connectionManager());
    }

    @Override
    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
        if (!StringUtils.isEmpty(restConfig.getProxyHost()) && restConfig.getProxyPort() != 0) {
            final HttpHost proxy = new HttpHost(restConfig.getProxyHost(), restConfig.getProxyPort());
            return builder.setProxy(proxy);
        }
        return builder;
    }

    /**
     * Build rest client rest client.
     *
     * @return the rest client
     */
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(restConfig.getHostName(),
                restConfig.getHostPort(), restConfig.getHostScheme()))
                .setMaxRetryTimeoutMillis(restConfig.getRestClientTimeout())
                .setRequestConfigCallback(this)
                .setHttpClientConfigCallback(this)
                .build();
    }
}
