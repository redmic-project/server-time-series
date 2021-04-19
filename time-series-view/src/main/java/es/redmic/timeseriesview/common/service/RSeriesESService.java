package es.redmic.timeseriesview.common.service;

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

import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.redmic.elasticsearchlib.series.repository.RSeriesESRepository;
import es.redmic.exception.common.ExceptionType;
import es.redmic.exception.common.InternalException;
import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.common.query.dto.MgetDTO;
import es.redmic.models.es.series.common.model.SeriesHitWrapper;
import es.redmic.models.es.series.common.model.SeriesHitsWrapper;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.timeserieslib.dto.series.SeriesBaseDTO;
import es.redmic.timeseriesview.mapper.TimeSeriesESMapper;
import es.redmic.timeseriesview.model.common.SeriesCommon;
import es.redmic.viewlib.common.service.IBaseService;
import es.redmic.viewlib.common.service.RBaseService;

public abstract class RSeriesESService<TModel extends SeriesCommon, TDTO extends SeriesBaseDTO, TQueryDTO extends DataQueryDTO>
	extends RBaseService<TModel, TDTO, TQueryDTO> implements IBaseService<TModel, TDTO, TQueryDTO> {

	private RSeriesESRepository<TModel, TQueryDTO> repository;

	protected final Logger LOGGER = LoggerFactory.getLogger(RSeriesESService.class);

	protected RSeriesESService() {
		super();
	}

	protected RSeriesESService(RSeriesESRepository<TModel, TQueryDTO> repository) {
		super();
		this.repository = repository;
	}

	public TDTO get(String id) {

		return viewResultToDTO(repository.findById(id));
	}

	@SuppressWarnings("unchecked")
	public TModel findById(String id) {

		SeriesHitWrapper<?> hitWrapper = repository.findById(id);
		return (TModel) hitWrapper.get_source();
	}

	@SuppressWarnings("unchecked")
	public TDTO searchById(String id) {

		SeriesSearchWrapper<?> hitsWrapper = repository.searchByIds(new String[] { id });
		if (hitsWrapper.getTotal() == 1)
			return viewResultToDTO((TModel) hitsWrapper.getSource(0));
		else if (hitsWrapper.getTotal() > 1) {
			LOGGER.debug("Existe más de un resultado para el mismo id");
			throw new InternalException(ExceptionType.INTERNAL_EXCEPTION);
		}
		return null;
	}

	public JSONCollectionDTO find(DataQueryDTO query) {

		SeriesSearchWrapper<TModel> result = repository.find(query);

		JSONCollectionDTO collection = viewResultToDTO(result.getHits());
		collection.set_aggs(Mappers.getMapper(TimeSeriesESMapper.class).map(result.getAggregations()));
		return collection;
	}

	public JSONCollectionDTO mget(MgetDTO dto) {

		return viewResultToDTO(repository.mget(dto));
	}

	public DataQueryDTO createSimpleQueryDTOFromQueryParams(Integer from, Integer size) {
		return repository.createSimpleQueryDTOFromQueryParams(from, size);
	}

	protected abstract TDTO viewResultToDTO(SeriesHitWrapper<TModel> viewResult);

	protected abstract TDTO viewResultToDTO(TModel model);

	protected abstract JSONCollectionDTO viewResultToDTO(SeriesSearchWrapper<TModel> viewResult);

	protected abstract JSONCollectionDTO viewResultToDTO(SeriesHitsWrapper<TModel> viewResult);

}
