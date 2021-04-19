package es.redmic.timeseriesview.utils;

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

import es.redmic.models.es.common.query.dto.DateLimitsDTO;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.models.es.series.timeseries.dto.RawDataDTO;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;
import es.redmic.timeseriesview.mapper.TimeSeriesESMapper;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;

import java.util.List;

import org.mapstruct.factory.Mappers;

public abstract class TimeSeriesUtils {

	// @formatter:off

	public static final Long TIME_INTERVAL_DEFAULT = 3600000L;
	public static final Long MAX_BUCKETS = 10000L;

	// @formatter:on

	private TimeSeriesUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Long getTimeInterval(Long timeIntervalDefault, DateLimitsDTO dateLimits) {

		if (timeIntervalDefault == null || dateLimits.getEndDate() == null || dateLimits.getStartDate() == null) {
			return TIME_INTERVAL_DEFAULT;
		}

		Long dateRangeMs = dateLimits.getEndDate().getMillis() - dateLimits.getStartDate().getMillis();

		if ((dateRangeMs / TIME_INTERVAL_DEFAULT) < MAX_BUCKETS) {
			return TIME_INTERVAL_DEFAULT;
		}

		// TimeInterval para maximizar el número de buckets devueltos
		return dateRangeMs / MAX_BUCKETS;
	}

	// TODO: rescatar el control de DataDefinition
	public static RawDataDTO getSourceToResult(SeriesSearchWrapper<TimeSeries> result, List<Integer> dataDefinitionIds) {

		RawDataDTO data = new RawDataDTO();

		if (result.getTotal() == 0)
			return data;

		int size = result.getTotal();

		for (int i = 0; i < size; i++) {
			TimeSeriesDTO item = Mappers.getMapper(TimeSeriesESMapper.class).map(result.getSource(i));
			if (item != null) {
				data.setItemData(Mappers.getMapper(TimeSeriesESMapper.class).convertToRaw(item));
			}
		}
		data.setDataDefinitionIds(dataDefinitionIds);
		return data;
	}
}
