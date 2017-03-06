package uk.gov.justice.log.wrapper;


import uk.gov.justice.log.utils.RestConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClientBuilder;

/**
 * Callback allows proxy based connections
 */
public class RequestConfigBuilderWrapper implements RestClientBuilder.RequestConfigCallback {

    private final RestConfig restConfig;

    /**
     * Instantiates a new Request config builder wrapper.
     *
     * @param restConfig the rest config
     */
    public RequestConfigBuilderWrapper(final RestConfig restConfig) {
        this.restConfig = restConfig;
    }

    @Override
    public RequestConfig.Builder customizeRequestConfig(final RequestConfig.Builder builder) {
        if (!StringUtils.isEmpty(restConfig.getProxyHost()) && restConfig.getProxyPort() != 0) {
            final HttpHost proxy = new HttpHost(restConfig.getProxyHost(), restConfig.getProxyPort());
            return builder.setProxy(proxy);
        }
        return builder;
    }
}
