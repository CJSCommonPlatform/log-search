package uk.gov.justice.log.factory;


import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;
import uk.gov.justice.log.search.SearchService;

public class SearchLogsFactory {

    private final SearchCriteria searchCriteria;

    public SearchLogsFactory(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public SearchService create(final RestClientFactory restClient,
                                final ElasticSearchQueryBuilder elasticSearchQueryBuilder) {
        return new SearchService(restClient, elasticSearchQueryBuilder);
    }
}
