package uk.gov.justice.log.wrapper;

import static uk.gov.justice.log.utils.CommonConstant.MAX_RETRY_TIMEOUT_MILLIS;

import uk.gov.justice.log.utils.RestConfig;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class RestClientFactory {

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
