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

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.timeseries.repository.RWTimeSeriesESRepository;
import es.redmic.models.es.common.dto.EventApplicationResult;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.common.query.SeriesQueryUtils;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;

@Repository
public class TimeSeriesESRepository extends RWTimeSeriesESRepository<TimeSeries, DataQueryDTO> {

	public TimeSeriesESRepository() {
		super();
	}

	@Override
	protected BoolQueryBuilder getQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery, QueryBuilder partialQuery) {
		return SeriesQueryUtils.getQuery(queryDTO, getInternalQuery(), partialQuery);
	}

	@Override
	protected EventApplicationResult checkDeleteConstraintsFulfilled(String modelToIndex) {
		// TODO Implementar comprobaciones
		return new EventApplicationResult(true);
	}

	@Override
	protected EventApplicationResult checkInsertConstraintsFulfilled(TimeSeries modelToIndex) {
		// TODO Implementar comprobaciones
		return new EventApplicationResult(true);
	}

	@Override
	protected EventApplicationResult checkUpdateConstraintsFulfilled(TimeSeries modelToIndex) {
		// TODO Implementar comprobaciones
		return new EventApplicationResult(true);
	}
}
