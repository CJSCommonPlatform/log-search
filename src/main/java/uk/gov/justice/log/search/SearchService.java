package uk.gov.justice.log.search;


import static uk.gov.justice.log.utils.CommonConstant.ELASTIC_MULTI_SEARCH_URL;
import static uk.gov.justice.log.utils.CommonConstant.POST;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchService {

    private final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    private RestClient restClient;

    private Map<String, String> params = Collections.emptyMap();

    public SearchService(final RestClient restClient) {
        this.restClient = restClient;
    }

    public Response search(final KibanaQueryBuilder kibanaQueryBuilder) throws IOException {

        final HttpEntity query = kibanaQueryBuilder.entityQuery();

        LOGGER.info(EntityUtils.toString(query));

        final Response response = restClient.performRequest(POST, ELASTIC_MULTI_SEARCH_URL, params, query);

        restClient.close();

        return response;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

}