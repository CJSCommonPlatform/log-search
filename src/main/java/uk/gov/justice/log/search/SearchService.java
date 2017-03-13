package uk.gov.justice.log.search;


import static uk.gov.justice.log.utils.SearchConstants.ELASTIC_MULTI_SEARCH_URL;
import static uk.gov.justice.log.utils.SearchConstants.POST;

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
    private final RestClient restClient;
    private final ElasticSearchQueryBuilder elasticSearchQueryBuilder;
    private Map<String, String> params = Collections.emptyMap();

    public SearchService(final RestClient restClient,
                         final ElasticSearchQueryBuilder elasticSearchQueryBuilder) {
        this.restClient = restClient;
        this.elasticSearchQueryBuilder = elasticSearchQueryBuilder;
    }

    public Response search() throws IOException {

        final HttpEntity query = elasticSearchQueryBuilder.entityQuery();

        LOGGER.info(EntityUtils.toString(query));

        final Response response = restClient.performRequest(POST, ELASTIC_MULTI_SEARCH_URL, params, query);

        restClient.close();

        return response;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

}