package es.redmic.timeseriesview.common.controller;

import javax.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.redmic.exception.custom.ResourceNotFoundException;
import es.redmic.exception.databinding.DTONotValidException;
import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.common.dto.SuperDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeserieslib.dto.series.SeriesBaseDTO;
import es.redmic.timeseriesview.common.service.RSeriesESService;
import es.redmic.timeseriesview.model.common.SeriesCommon;
import es.redmic.viewlib.common.controller.RController;

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

public abstract class RSeriesController<TModel extends SeriesCommon, TDTO extends SeriesBaseDTO, TQueryDTO extends DataQueryDTO>
	extends RController<TModel, TDTO, TQueryDTO> {

	private RSeriesESService<TModel, TDTO, TQueryDTO> serviceES;

	protected RSeriesController(RSeriesESService<TModel, TDTO, TQueryDTO> service) {
		super(service);
		this.serviceES = service;
	}

	@GetMapping(value = "")
	@ResponseBody
	public SuperDTO search(@RequestParam(required = false, value = "from") Integer from,
			@RequestParam(required = false, value = "size") Integer size) {

		DataQueryDTO queryDTO = serviceES.createSimpleQueryDTOFromQueryParams(from, size);
		return new ElasticSearchDTO(serviceES.find(queryDTO));
	}

	@PostMapping(value = "/_search")
	@ResponseBody
	public SuperDTO advancedSearch(@Valid @RequestBody TQueryDTO queryDTO, BindingResult bindingResult) {

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		JSONCollectionDTO result = serviceES.find(queryDTO);
		return new ElasticSearchDTO(result, result.getTotal());
	}

	@GetMapping(value = "/{id}")
	@ResponseBody
	public SuperDTO get(@PathVariable("id") String id) {

		TDTO result = serviceES.searchById(id);
		return new ElasticSearchDTO(result, result == null ? 0 : 1);
	}

	@GetMapping(value = "/_suggest")
	@ResponseBody
	public SuperDTO suggest() {

		throw new ResourceNotFoundException();
	}
}
