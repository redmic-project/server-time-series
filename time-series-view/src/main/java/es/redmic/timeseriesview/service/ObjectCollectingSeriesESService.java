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

import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.series.common.model.SeriesHitWrapper;
import es.redmic.models.es.series.common.model.SeriesHitsWrapper;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationsForListDTO;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationsForPieChartDTO;
import es.redmic.models.es.series.timeseries.dto.DataHistogramDTO;
import es.redmic.timeserieslib.dto.objectcollecting.ObjectCollectingSeriesDTO;
import es.redmic.timeseriesview.common.service.RSeriesESService;
import es.redmic.timeseriesview.mapper.DataHistogramESMapper;
import es.redmic.timeseriesview.mapper.ObjectCollectingSeriesESMapper;
import es.redmic.timeseriesview.model.objectcollectingseries.ObjectCollectingSeries;
import es.redmic.timeseriesview.repository.ObjectCollectingSeriesESRepository;

@Service
public class ObjectCollectingSeriesESService
		extends RSeriesESService<ObjectCollectingSeries, ObjectCollectingSeriesDTO, DataQueryDTO> {

	private static final String DEFAULT_SEARCH_FIELD = "remark";

	int nestingDepth = 2;

	ObjectCollectingSeriesESRepository repository;

	@Autowired
	public ObjectCollectingSeriesESService(ObjectCollectingSeriesESRepository repository) {
		super(repository);
		this.repository = repository;
	}

	@SuppressWarnings("unchecked")
	public ElasticSearchDTO findClassificationList(DataQueryDTO query) {

		SeriesSearchWrapper<ObjectCollectingSeries> response = repository.find(query);

		ClassificationsForListDTO dtoOut = Mappers.getMapper(ObjectCollectingSeriesESMapper.class)
			.convertToList(response.getAggregations());

		return new ElasticSearchDTO(dtoOut.getClassification(), dtoOut.getClassification().size());
	}

	@SuppressWarnings("unchecked")
	public ElasticSearchDTO findClassificationStatistics(DataQueryDTO query) {

		SeriesSearchWrapper<ObjectCollectingSeries> response = repository.find(query);

		ClassificationsForPieChartDTO dtoOut = Mappers.getMapper(ObjectCollectingSeriesESMapper.class)
			.convertToPieChart(response.getAggregations());

		return new ElasticSearchDTO(dtoOut.getClassification(), dtoOut.getClassification().size());
	}

	@SuppressWarnings("unchecked")
	public ElasticSearchDTO findTemporalDataStatistics(DataQueryDTO query) {

		SeriesSearchWrapper<ObjectCollectingSeries> response = repository.find(query);

		DataHistogramDTO dtoOut = Mappers.getMapper(DataHistogramESMapper.class).map(response.getAggregations());

		dtoOut.setDataDefinitionIds((List<Integer>) query.getTerms().get("dataDefinition"));
		return new ElasticSearchDTO(dtoOut, dtoOut.getData().size());
	}

	@Override
	protected ObjectCollectingSeriesDTO viewResultToDTO(SeriesHitWrapper<ObjectCollectingSeries> viewResult) {
		return Mappers.getMapper(ObjectCollectingSeriesESMapper.class).map(viewResult);
	}

	@Override
	protected ObjectCollectingSeriesDTO viewResultToDTO(ObjectCollectingSeries model) {
		return Mappers.getMapper(ObjectCollectingSeriesESMapper.class).map(model);
	}

	@Override
	protected JSONCollectionDTO viewResultToDTO(SeriesSearchWrapper<ObjectCollectingSeries> viewResult) {
		return Mappers.getMapper(ObjectCollectingSeriesESMapper.class).map(viewResult);
	}

	@Override
	protected JSONCollectionDTO viewResultToDTO(SeriesHitsWrapper<ObjectCollectingSeries> viewResult) {
		return Mappers.getMapper(ObjectCollectingSeriesESMapper.class).map(viewResult);
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
		return new String[] { DEFAULT_SEARCH_FIELD };;
	}
}
