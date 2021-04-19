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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.joda.time.format.DateTimeFormat;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.data.repository.RDataESRepository;
import es.redmic.elasticsearchlib.series.repository.IBaseSeriesESRepository;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.common.query.SeriesQueryUtils;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import es.redmic.timeseriesview.mapper.WindRoseESMapper;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;

@Repository
public class WindRoseESRepository extends RDataESRepository<TimeSeries, DataQueryDTO> implements IBaseSeriesESRepository {

	@Value("${timeseries.index.pattern}")
	String timeSeriesIndexPattern;

	public WindRoseESRepository() {
		super(IBaseSeriesESRepository.INDEX, IBaseSeriesESRepository.TYPE, true);
	}

	private static final String DATA_DEFINITION_PROPERTY = "dataDefinition";

	@Override
	protected String getIndex(final TimeSeries modelToIndex) {
		return getIndex()[0] + "-" + modelToIndex.getDate().toString(DateTimeFormat.forPattern(timeSeriesIndexPattern));
	}

	/**
	 * Método que realiza la consulta y manda a convertir el resultado al dto
	 * esperado
	 */

	public WindRoseDataDTO getWindRoseData(DataQueryDTO query, Integer numSectors, Integer partitionNumber) {

		Map<Object, Object> globalProperties = new HashMap<>();
		globalProperties.put("numSectors", numSectors);
		globalProperties.put("partitionNumber", partitionNumber);

		return Mappers.getMapper(WindRoseESMapper.class).map(find(query).getAggregations(), globalProperties);
	}

	/**
	 * Sobrescribe método original para evitar que cree las agregaciones comunes, ya
	 * que se usarán las específicas para windrose
	 */
	@Override
	protected List<BaseAggregationBuilder> getAggs(List<AggsPropertiesDTO> aggs) {

		String listSplitRegex = "\\s*,\\s*";

		List<BaseAggregationBuilder> aggBuilders = new ArrayList<>();

		List<String> speedDataDefinitions = Arrays.asList(aggs.get(0).getTerm().split(listSplitRegex));
		List<String> directionDataDefinitions = Arrays.asList(aggs.get(1).getTerm().split(listSplitRegex));

		TermsQueryBuilder speedTermQuery = QueryBuilders.termsQuery(DATA_DEFINITION_PROPERTY, speedDataDefinitions);

		aggBuilders.add(PipelineAggregatorBuilders.statsBucket("stats-buckets",
				"avg_values_by_interval>speedDataDefinitionFilter>avg_speed"));

		// Se consulta el número de elementos total para las estadísticas
		aggBuilders.add(AggregationBuilders.filter("dataDefinitionFilter", speedTermQuery)
				.subAggregation(AggregationBuilders.count("speed_count").field(defaultField)));

		// Desde el cliente se envía en ms y aquí se pasa a seg
		int timeInterval = (int) (Long.parseLong(aggs.get(2).getTerm()) / 1000);

		// Se consultan direcciones y velocidades agregadas por el timeInterval definido

		AggregationBuilder directionValues = AggregationBuilders
				.filter("directionDataDefinitionFilter",
						QueryBuilders.termsQuery(DATA_DEFINITION_PROPERTY, directionDataDefinitions))
				.subAggregation(AggregationBuilders.avg("avg_direction").field(defaultField));

		AggregationBuilder speedValues = AggregationBuilders.filter("speedDataDefinitionFilter", speedTermQuery)
				.subAggregation(AggregationBuilders.avg("avg_speed").field(defaultField));

		aggBuilders.add(AggregationBuilders.dateHistogram("avg_values_by_interval").field(dateTimeField).minDocCount(1)
				.dateHistogramInterval(DateHistogramInterval.seconds(timeInterval)).subAggregation(directionValues)
				.subAggregation(speedValues));

		return aggBuilders;
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryBuilder getTermQuery(Map<String, Object> terms, BoolQueryBuilder query) {

		if (terms.containsKey(DATA_DEFINITION_PROPERTY)) {
			List<Integer> ids = (List<Integer>) terms.get(DATA_DEFINITION_PROPERTY);
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(DATA_DEFINITION_PROPERTY, ids)));
		}

		if (terms.containsKey("dates")) {
			List<String> dates = (List<String>) terms.get("dates");
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("date", dates)));
		}
		return super.getTermQuery(terms, query);
	}

	@Override
	protected BoolQueryBuilder getQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery, QueryBuilder partialQuery) {
		return SeriesQueryUtils.getQuery(queryDTO, getInternalQuery(), partialQuery);
	}
}
