package uk.gov.justice.log.main;

import static uk.gov.justice.log.utils.SearchConstants.RESPONSE_HITS;
import static uk.gov.justice.log.utils.SearchConstants.RESPONSE_MESSAGES;

import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.main.output.Result;

import java.io.IOException;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

public class ResultExtractor {

    public Result extractResult(final ElasticSearchQueryBuilder elasticSearchQueryBuilder,
                                   final Response response) throws IOException {
        final String responseStrActual = EntityUtils.toString(response.getEntity());
        final Integer hits = JsonPath.read(responseStrActual, RESPONSE_HITS);
        final List<String> messages = JsonPath.read(responseStrActual, RESPONSE_MESSAGES);
        final SearchCriteria searchCriteria = elasticSearchQueryBuilder.searchCriteria();
        final String query = elasticSearchQueryBuilder.query();
        final String fromTime = searchCriteria.getFromTime();
        final String toTime = searchCriteria.getToTime();
        return new Result(query, fromTime, toTime, hits, messages);
    }
}
