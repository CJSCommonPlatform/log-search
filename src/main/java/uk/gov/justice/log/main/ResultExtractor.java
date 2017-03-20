package uk.gov.justice.log.main;

import static uk.gov.justice.log.utils.SearchConstants.HITS_MESSAGE_LIST;
import static uk.gov.justice.log.utils.SearchConstants.HITS_TOTAL;

import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.main.output.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

public class ResultExtractor {

    public List<Result> extractResults(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                                       final List<Response> responses) throws IOException {
        final List<Result> results = new ArrayList<>();
        int queryIndex = 0;
        for (Response response : responses) {
            results.add(extractResult(elasticSearchQueryBuilder, response, queryIndex));
            queryIndex++;
        }
        return results;
    }

    private Result extractResult(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                                 final Response response, final int queryIndex) throws IOException {
        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, HITS_TOTAL);
        final List<String> messages = JsonPath.read(responseStrActual, HITS_MESSAGE_LIST);
        final SearchCriteria searchCriteria = elasticSearchQueryBuilder.searchCriteria();
        final String query = elasticSearchQueryBuilder.queries().get(queryIndex);
        final String fromTime = searchCriteria.getFromTime();
        final String toTime = searchCriteria.getToTime();
        return new Result(query, fromTime, toTime, hits, messages);
    }
}
