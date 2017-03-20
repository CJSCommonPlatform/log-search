package uk.gov.justice.log.search;


import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static uk.gov.justice.log.utils.SearchConstants.ELASTIC_MULTI_SEARCH_URL;
import static uk.gov.justice.log.utils.SearchConstants.POST;

import uk.gov.justice.log.factory.RestClientFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchService {

    private final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
    private final ElasticSearchQueryBuilder elasticSearchQueryBuilder;
    private volatile RestClientFactory restClientFactory;
    private volatile List<Response> responses = new LinkedList<>();

    public SearchService(final RestClientFactory restClientFactory,
                         final ElasticSearchQueryBuilder elasticSearchQueryBuilder) {
        this.restClientFactory = restClientFactory;
        this.elasticSearchQueryBuilder = elasticSearchQueryBuilder;
    }

    public List<Response> search() throws IOException {
        runSingleThreadedOneRestClient();
        return responses;
    }

    private void runSingleThreadedOneRestClient() throws IOException {
        final RestClient restClient = restClientFactory.restClient();
        final Map<String, String> params = Collections.emptyMap();

        for (final String query : elasticSearchQueryBuilder.queries()) {
            final NStringEntity entityQuery = new NStringEntity(query, APPLICATION_JSON);
            final String query1str = EntityUtils.toString(entityQuery);
            LOGGER.info(query1str);
            responses.add(restClient.performRequest(POST, ELASTIC_MULTI_SEARCH_URL, params, entityQuery));
        }
        restClient.close();
    }
}
