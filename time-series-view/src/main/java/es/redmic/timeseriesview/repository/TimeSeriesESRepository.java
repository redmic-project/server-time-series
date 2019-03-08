package es.redmic.timeseriesview.repository;

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
