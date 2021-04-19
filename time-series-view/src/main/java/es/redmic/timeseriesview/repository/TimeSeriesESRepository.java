package es.redmic.timeseriesview.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;

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

import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.common.query.SeriesQueryUtils;
import es.redmic.elasticsearchlib.series.repository.RWSeriesESRepository;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;

import es.redmic.timeseriesview.model.timeseries.TimeSeries;

@Repository
public class TimeSeriesESRepository extends RWSeriesESRepository<TimeSeries, DataQueryDTO> {

	public TimeSeriesESRepository() {
		super();
	}

	@Override
	protected List<BaseAggregationBuilder> getAggs(DataQueryDTO elasticQueryDTO) {

		List<AggsPropertiesDTO> aggs = elasticQueryDTO.getAggs();

		if (elasticQueryDTO.getInterval() == null && (aggs == null || aggs.isEmpty()))
			return Collections.emptyList();

		List<BaseAggregationBuilder> aggsBuilder = new ArrayList<>();

		if (aggs.get(0) == null)
			return Collections.emptyList();

		if (elasticQueryDTO.getInterval() != null && (aggs.get(0).getField().equals("temporaldata"))) {

			aggsBuilder.add(getTemporalDataAggregationBuilder(elasticQueryDTO));
		}
		return aggsBuilder;
	}

	private DateHistogramAggregationBuilder getTemporalDataAggregationBuilder(DataQueryDTO elasticQueryDTO) {

		return getDateHistogramAggregation(SeriesQueryUtils.getInterval(elasticQueryDTO.getInterval()))
				.subAggregation(AggregationBuilders.stats(defaultField).field(defaultField));
	}
}
