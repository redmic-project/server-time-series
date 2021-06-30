package es.redmic.timeseriesview.controller;

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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.redmic.exception.databinding.DTONotValidException;
import es.redmic.models.es.common.dto.SuperDTO;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeserieslib.dto.objectcollecting.ObjectCollectingSeriesDTO;
import es.redmic.timeseriesview.common.controller.RSeriesController;
import es.redmic.timeseriesview.model.objectcollectingseries.ObjectCollectingSeries;
import es.redmic.timeseriesview.service.ObjectCollectingSeriesESService;

@RestController
@RequestMapping(value = "${controller.mapping.OBJECTCOLLECTING}")
public class ObjectCollectingSeriesController extends RSeriesController<ObjectCollectingSeries, ObjectCollectingSeriesDTO, DataQueryDTO> {

	private ObjectCollectingSeriesESService service;

	@Autowired
	public ObjectCollectingSeriesController(ObjectCollectingSeriesESService service) {
		super(service);
		this.service = service;
	}

	@PostMapping(value = "${controller.mapping.OBJECT_CLASSIFICATION_LIST}/_search")
	@ResponseBody
	public SuperDTO findClassificationList(@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		AggsPropertiesDTO agg = new AggsPropertiesDTO();
		agg.setField("classificationList");
		queryDTO.addAgg(agg);
		queryDTO.setSize(0);

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		return service.findClassificationList(queryDTO);
	}

	@PostMapping(value = "${controller.mapping.OBJECT_CLASSIFICATION}/_search")
	@ResponseBody
	public SuperDTO findClassification(@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		AggsPropertiesDTO agg = new AggsPropertiesDTO();
		agg.setField("classification");
		queryDTO.addAgg(agg);
		queryDTO.setSize(0);

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		return service.findClassificationStatistics(queryDTO);
	}

	@PostMapping(value = "${controller.mapping.SERIES_TEMPORALDATA}/_search")
	@ResponseBody
	public SuperDTO findTemporalData(@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		AggsPropertiesDTO agg = new AggsPropertiesDTO();
		agg.setField("temporaldata");
		queryDTO.addAgg(agg);

		queryDTO.setSize(0);

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		return service.findTemporalDataStatistics(queryDTO);
	}
}
