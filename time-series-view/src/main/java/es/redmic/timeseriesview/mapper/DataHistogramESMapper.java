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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;

import es.redmic.models.es.geojson.common.model.Aggregations;
import es.redmic.models.es.series.timeseries.dto.DataHistogramDTO;
import es.redmic.models.es.series.timeseries.dto.DataHistogramItemDTO;
import es.redmic.models.es.series.timeseries.dto.DataHistogramStatsDTO;

@Mapper
public abstract class DataHistogramESMapper {

	@SuppressWarnings("unchecked")
	public DataHistogramDTO map(Aggregations source) {

		DataHistogramDTO data = new DataHistogramDTO();
		Map<String, Object> aggregations = source.getAttributes();
		if (aggregations == null || aggregations.size() == 0)
			return data;

		Map<String, Object> dateHistogram = (Map<String, Object>) aggregations.get("date_histogram#dateHistogram");
		if (dateHistogram == null || dateHistogram.size() == 0)
			return data;

		List<Map<String, Object>> hits = (List<Map<String, Object>>) dateHistogram.get("buckets");
		int size = hits.size();

		DataHistogramItemDTO lastItem = new DataHistogramItemDTO();
		for (int i = 0; i < size; i++) {
			DataHistogramItemDTO item = map(hits.get(i));
			if (canInsertItem(lastItem, item)) {
				data.setItemData(item);
				lastItem = item;
			}
		}
		return data;
	}

	@SuppressWarnings("unchecked")
	private DataHistogramItemDTO map(Map<String, Object> source) {
		DataHistogramItemDTO item = new DataHistogramItemDTO();
		item.setKey_as_string((String) source.get("key_as_string"));
		item.setValue(getDataHistogramStats((LinkedHashMap<String, Object>) source.get("stats#value")));
		return item;
	}

	private DataHistogramStatsDTO getDataHistogramStats(Map<String, Object> source) {
		DataHistogramStatsDTO item = new DataHistogramStatsDTO();
		item.setAvg((Double) source.get("avg"));
		item.setCount((Integer) source.get("count"));
		item.setMax((Double) source.get("max"));
		item.setMin((Double) source.get("min"));
		item.setSum((Double) source.get("sum"));
		return item;
	}

	protected static boolean canInsertItem(DataHistogramItemDTO lastItem, DataHistogramItemDTO newItem) {

		if ((newItem.hasData()) || (lastItem.hasData() && !newItem.hasData())) {
			return true;
		}
		return false;
	}
}
