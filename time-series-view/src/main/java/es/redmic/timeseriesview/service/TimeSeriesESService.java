package es.redmic.timeseriesview.service;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.series.common.model.SeriesHitWrapper;
import es.redmic.models.es.series.common.model.SeriesHitsWrapper;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.models.es.series.timeseries.dto.DataHistogramDTO;
import es.redmic.models.es.series.timeseries.dto.RawDataDTO;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;
import es.redmic.timeseriesview.common.service.RSeriesESService;
import es.redmic.timeseriesview.common.utils.DataDefinitionUtils;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import es.redmic.timeseriesview.mapper.DataHistogramESMapper;
import es.redmic.timeseriesview.mapper.TimeSeriesESMapper;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.timeseriesview.repository.TimeSeriesESRepository;
import es.redmic.timeseriesview.repository.WindRoseESRepository;
import es.redmic.timeseriesview.utils.TimeSeriesUtils;
import es.redmic.timeseriesview.utils.WindRoseUtils;

@Service
public class TimeSeriesESService extends RSeriesESService<TimeSeries, TimeSeriesDTO, DataQueryDTO> {

	private TimeSeriesESRepository repository;

	private static final String GOOD_QFLAG = "1";

	private static final String DATA_DEFINITION_PROPERTY = "dataDefinition";
	private static final String DEFAULT_SEARCH_FIELD = "remark";

	@Autowired
	WindRoseESRepository windRoseESRepository;

	@Autowired
	public TimeSeriesESService(TimeSeriesESRepository repository) {
		super(repository);
		this.repository = repository;
	}

	@SuppressWarnings("unchecked")
	public ElasticSearchDTO findTemporalDataStatistics(DataQueryDTO query) {

		if (query.getInterval() == null) {
			DataDefinitionUtils.addDataDefinitionFieldToReturn(query);
		}

		SeriesSearchWrapper<TimeSeries> response = repository.find(query);
		List<Integer> dataDefinitionIds = (List<Integer>) query.getTerms().get(DATA_DEFINITION_PROPERTY);

		if (query.getInterval() != null) {

			DataHistogramDTO dtoOut = Mappers.getMapper(DataHistogramESMapper.class).map(response.getAggregations());
			dtoOut.setDataDefinitionIds((List<Integer>) query.getTerms().get(DATA_DEFINITION_PROPERTY));
			return new ElasticSearchDTO(dtoOut, dtoOut.getData().size());
		}

		RawDataDTO result = TimeSeriesUtils.getSourceToResult(response, dataDefinitionIds);

		return new ElasticSearchDTO(result, result.getData().size());
	}

	@SuppressWarnings({ "unchecked" })
	public ElasticSearchDTO getWindRoseData(DataQueryDTO query, String activityId) {

		// Obtiene datos de la query

		Map<String, Object> dataDefinitionMap = (Map<String, Object>) query.getTerms().get(DATA_DEFINITION_PROPERTY);

		Integer numSectors = (Integer) query.getTerms().get("numSectors");
		Integer partitionNumber = (Integer) query.getTerms().get("numSplits");

		List<Integer> speedDataDefinition = (List<Integer>) dataDefinitionMap.get("speed");
		List<Integer> directionDataDefinition = (List<Integer>) dataDefinitionMap.get("direction");

		Long timeIntervalDefault = new Long(query.getTerms().get("timeInterval").toString());

		WindRoseUtils.checkValidNumSectors(numSectors);
		WindRoseUtils.checkValidPartitionNumber(partitionNumber);

		// Añade a query para comprobar que la actividad corresponde con la buscada
		query.setActivityId(activityId);

		// Se obliga a que los datos sean buenos
		if (query.getQFlags() != null)
			query.getQFlags().clear();

		query.setQFlags(Arrays.asList(GOOD_QFLAG));

		query.setSize(0);

		List<Integer> dataDefinitionList = new ArrayList<>();
		dataDefinitionList.addAll(speedDataDefinition);
		dataDefinitionList.addAll(directionDataDefinition);

		query.getTerms().put(DATA_DEFINITION_PROPERTY, dataDefinitionList);

		query.addAgg(new AggsPropertiesDTO(DATA_DEFINITION_PROPERTY, StringUtils.join(speedDataDefinition, ",")));
		query.addAgg(new AggsPropertiesDTO(DATA_DEFINITION_PROPERTY, StringUtils.join(directionDataDefinition, ",")));
		query.addAgg(new AggsPropertiesDTO("interval",
				TimeSeriesUtils.getTimeInterval(timeIntervalDefault, query.getDateLimits()).toString()));

		WindRoseDataDTO windroseDataDTO = windRoseESRepository.getWindRoseData(query, numSectors, partitionNumber);

		return new ElasticSearchDTO(windroseDataDTO, windroseDataDTO.getData().size());
	}

	@Override
	protected TimeSeriesDTO viewResultToDTO(SeriesHitWrapper<TimeSeries> viewResult) {
		return Mappers.getMapper(TimeSeriesESMapper.class).map(viewResult);
	}

	@Override
	protected TimeSeriesDTO viewResultToDTO(TimeSeries model) {
		return Mappers.getMapper(TimeSeriesESMapper.class).map(model);
	}

	@Override
	protected JSONCollectionDTO viewResultToDTO(SeriesSearchWrapper<TimeSeries> viewResult) {
		return Mappers.getMapper(TimeSeriesESMapper.class).map(viewResult);
	}

	@Override
	protected JSONCollectionDTO viewResultToDTO(SeriesHitsWrapper<TimeSeries> viewResult) {
		return Mappers.getMapper(TimeSeriesESMapper.class).map(viewResult);
	}

	@Override
	protected String[] getDefaultSearchFields() {
		return new String[] { DEFAULT_SEARCH_FIELD };
	}

	@Override
	protected String[] getDefaultHighlightFields() {
		return new String[] { DEFAULT_SEARCH_FIELD };
	}

	@Override
	protected String[] getDefaultSuggestFields() {
		return new String[] { DEFAULT_SEARCH_FIELD };
	}
}
