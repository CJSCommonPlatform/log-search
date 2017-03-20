package uk.gov.justice.log.factory;

import uk.gov.justice.log.search.ElasticSearchQueryBuilder;
import uk.gov.justice.log.search.SearchCriteria;

public class ElasticSearchQueryBuilderFactory {

    private final SearchCriteria searchCriteria;

    public ElasticSearchQueryBuilderFactory(final SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public ElasticSearchQueryBuilder createElasticSearchQueryBuilder() {
        return new ElasticSearchQueryBuilder(searchCriteria);
    }
}

