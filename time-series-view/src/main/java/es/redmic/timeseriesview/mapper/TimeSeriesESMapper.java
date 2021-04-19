package es.redmic.timeseriesview.mapper;

/*-
 * #%L
 * Time series view
 * %%
 * Copyright (C) 2019 - 2021 REDMIC Project / Server
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

import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.series.common.model.SeriesHitWrapper;
import es.redmic.models.es.series.common.model.SeriesHitsWrapper;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.models.es.series.timeseries.dto.RawDataItemDTO;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;
import es.redmic.timeseriesview.common.mapper.SeriesESMapper;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;

@Mapper
public abstract class TimeSeriesESMapper extends SeriesESMapper<TimeSeriesDTO, TimeSeries> {

	protected final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesESMapper.class);

	public abstract TimeSeriesDTO map(TimeSeries model);

	public abstract TimeSeries map(TimeSeriesDTO dto);

	public RawDataItemDTO convertToRaw(TimeSeriesDTO dto) {

		RawDataItemDTO rawDataItemDTO = new RawDataItemDTO();
		rawDataItemDTO.setDate(dto.getDate().toString());
		rawDataItemDTO.setValue(dto.getValue());
		return rawDataItemDTO;
	}

	public TimeSeriesDTO map(SeriesHitWrapper<TimeSeries> viewResult) {
		return map(viewResult.get_source());
	}

	public JSONCollectionDTO map(SeriesSearchWrapper<TimeSeries> viewResult) {

		JSONCollectionDTO result = map(viewResult.getHits());
		result.set_aggs(getAggs(viewResult.getAggregations()));
		return result;
	}

	public JSONCollectionDTO map(SeriesHitsWrapper<TimeSeries> seriesHitsWrapper) {

		JSONCollectionDTO result = new JSONCollectionDTO();
		result.setData(mapList(seriesHitsWrapper.getHits()));
		result.get_meta().setMax_score(seriesHitsWrapper.getMax_score());
		result.setTotal(seriesHitsWrapper.getTotal());
		return result;
	}

	public List<TimeSeriesDTO> mapList(List<SeriesHitWrapper<TimeSeries>> dataHitWrapper) {

		List<TimeSeriesDTO> list = new ArrayList<>();
		for (SeriesHitWrapper<TimeSeries> entity : dataHitWrapper) {
			list.add(map(entity));
		}
		return list;
	}
}
