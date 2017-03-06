package uk.gov.justice.log.wrapper;

import static uk.gov.justice.log.utils.CommonConstant.MAX_RETRY_TIMEOUT_MILLIS;

import uk.gov.justice.log.utils.RestConfig;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

/**
 * Builds ElasticSearch RestClient
 */
public class RestClientFactory {

    /**
     * Build rest client rest client.
     *
     * @param restConfig                    the rest config
     * @param requestConfigBuilderWrapper   the request config builder wrapper
     * @param httpAsyncClientBuilderWrapper the http async client builder wrapper
     * @return the rest client
     */
    public RestClient buildRestClient(final RestConfig restConfig,
                                      final RequestConfigBuilderWrapper requestConfigBuilderWrapper,
                                      final HttpAsyncClientBuilderWrapper httpAsyncClientBuilderWrapper) {
        return RestClient.builder(new HttpHost(restConfig.getHostName(),
                restConfig.getHostPort(), restConfig.getHostScheme()))
                .setMaxRetryTimeoutMillis(MAX_RETRY_TIMEOUT_MILLIS)
                .setRequestConfigCallback(requestConfigBuilderWrapper)
                .setHttpClientConfigCallback(httpAsyncClientBuilderWrapper)
                .build();
    }
}
