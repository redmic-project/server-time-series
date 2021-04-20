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

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.redmic.elasticsearchlib.common.query.SeriesQueryUtils;
import es.redmic.exception.databinding.DTONotValidException;
import es.redmic.models.es.common.DataPrefixType;
import es.redmic.models.es.common.dto.SuperDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.timeseriesview.common.controller.RSeriesController;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.timeseriesview.service.TimeSeriesESService;

@RestController
@RequestMapping()
public class TimeSeriesController extends RSeriesController<TimeSeries, TimeSeriesDTO, DataQueryDTO>{

	private TimeSeriesESService serviceES;

	@Autowired
	public TimeSeriesController(TimeSeriesESService service) {
		super(service);
		this.serviceES = service;
	}

	@PostConstruct
	private void postConstruct() {
		setFieldsExcludedOnQuery(SeriesQueryUtils.getFieldsExcludedOnQuery());
	}

	@PostMapping(value = "${controller.mapping.SERIES_TEMPORALDATA}/_search")
	@ResponseBody
	public SuperDTO findTemporalData(@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		if (queryDTO.getInterval() != null) {
			AggsPropertiesDTO agg = new AggsPropertiesDTO();
			agg.setField("temporaldata");

			queryDTO.addAgg(agg);
			queryDTO.setSize(0);
		}

		queryDTO.setDataType(DataPrefixType.getPrefixTypeFromClass(TimeSeriesDTO.class));

		return serviceES.findTemporalDataStatistics(queryDTO);
	}

	@PostMapping(value = "${controller.mapping.TIMESERIES_ACTIVITY}/${controller.mapping.SERIES_WINDROSE}/_search")
	@ResponseBody
	public SuperDTO getRosewindData(@PathVariable(name = "activityId", required = false) String activityId,
			@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		return serviceES.getWindRoseData(queryDTO, activityId);
	}
}
