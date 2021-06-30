package es.redmic.timeseriesview.repository;

/*-
 * #%L
 * Time series view
 * %%
 * Copyright (C) 2019 REDMIC Project / Server
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.series.repository.RWSeriesESRepository;
import es.redmic.exception.elasticsearch.ESTermQueryException;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.common.query.SeriesQueryUtils;
import es.redmic.timeseriesview.model.objectcollectingseries.ObjectCollectingSeries;

@Repository
public class ObjectCollectingSeriesESRepository extends RWSeriesESRepository<ObjectCollectingSeries, DataQueryDTO> {

	private static final String objectNestedPath = "object";

	private static int aggsSize = 200;

	private static QueryBuilder INTERNAL_QUERY = QueryBuilders.nestedQuery(objectNestedPath,
		QueryBuilders.existsQuery(objectNestedPath), ScoreMode.Avg);

	public ObjectCollectingSeriesESRepository() {
		super();
	}

	/*
	 * Sobrescribe getAggs para hacer las agregaciones mediante histogramas
	 *
	 */

	@Override
	protected List<BaseAggregationBuilder> getAggs(DataQueryDTO elasticQueryDTO) {

		List<AggsPropertiesDTO> aggs = elasticQueryDTO.getAggs();

		if (elasticQueryDTO.getInterval() == null && (aggs == null || aggs.isEmpty()))
			return new ArrayList<>();

		List<BaseAggregationBuilder> histogramAggs = new ArrayList<>();

		if (aggs.get(0).getField().equals("temporaldata")) {

			histogramAggs.add(getTemporalDataAggregationBuilder(elasticQueryDTO));
		} else if (aggs.get(0).getField().equals("classificationList")) {

			histogramAggs.add(getClassificationList(elasticQueryDTO));
		} else if (aggs.get(0).getField().equals("classification")) {

			histogramAggs.add(getClassification(elasticQueryDTO));
		}
		return histogramAggs;
	}

	private DateHistogramAggregationBuilder getTemporalDataAggregationBuilder(DataQueryDTO elasticQueryDTO) {

		return getDateHistogramAggregation(elasticQueryDTO.getInterval() != null
				? SeriesQueryUtils.getInterval(elasticQueryDTO.getInterval()) : DateHistogramInterval.QUARTER)
						.subAggregation(AggregationBuilders.stats(defaultField).field(defaultField));
	}

	private NestedAggregationBuilder getClassificationList(DataQueryDTO elasticQueryDTO) {

		DateHistogramAggregationBuilder dateHistogramBuilder = getDateHistogramAggregation(
				elasticQueryDTO.getInterval() != null ? SeriesQueryUtils.getInterval(elasticQueryDTO.getInterval())
						: DateHistogramInterval.QUARTER)
								.subAggregation(AggregationBuilders.stats(defaultField).field(defaultField));

		return AggregationBuilders.nested("object", "object")
				.subAggregation(AggregationBuilders.terms("objectType").field("object.name")
						.subAggregation(
								AggregationBuilders.reverseNested("timeIntervals").subAggregation(dateHistogramBuilder))
						.subAggregation(AggregationBuilders.nested("objectClassification", "object.classification")
								.subAggregation(AggregationBuilders.terms("level")
										.field("object.classification.objectType.level").subAggregation(
												AggregationBuilders
														.terms("objectClassificationPath").field(
																"object.classification.objectType.path")
														.size(aggsSize)
														.subAggregation(AggregationBuilders
																.terms("objectClassificationName")
																.field("object.classification.objectType.name")
																.subAggregation(AggregationBuilders
																		.reverseNested("timeIntervals").subAggregation(
																				dateHistogramBuilder)))))));
	}

	private DateHistogramAggregationBuilder getClassification(DataQueryDTO elasticQueryDTO) {

		return getDateHistogramAggregation(
			elasticQueryDTO.getInterval() != null ? SeriesQueryUtils.getInterval(elasticQueryDTO.getInterval())
				: DateHistogramInterval.QUARTER)
					.subAggregation(AggregationBuilders.nested("object", "object")
						.subAggregation(AggregationBuilders.terms("objectType").field("object.name")
							.subAggregation(AggregationBuilders.nested("objectClassification", "object.classification")
								.subAggregation(AggregationBuilders
									.terms("level")
									.field("object.classification.objectType.level")
									.includeExclude(new IncludeExclude(null, new String[] { "1", "2" }))
										.subAggregation(AggregationBuilders
											.terms("objectClassificationPath")
											.field("object.classification.objectType.path")
											.size(aggsSize)
												.subAggregation(AggregationBuilders
													.terms("objectClassificationName")
													.field("object.classification.objectType.name")
														.subAggregation(AggregationBuilders
															.reverseNested("stats")
																.subAggregation(
																	AggregationBuilders
																		.stats(defaultField)
																		.field(defaultField)))))))));
	}

	@Override
	public QueryBuilder getInternalQuery() {
		return INTERNAL_QUERY;
	}

	@Override
	protected QueryBuilder getTermQuery(Map<String, Object> terms, BoolQueryBuilder query) {

		if (terms != null && terms.containsKey("grandparentId")) {
			String activityId = (String) terms.get("grandparentId");

			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("activityId", activityId)));

		} else {
			throw new ESTermQueryException("activityId", "null");
		}

		return query;
	}
}
