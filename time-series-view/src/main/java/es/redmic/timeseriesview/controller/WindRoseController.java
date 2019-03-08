package es.redmic.timeseriesview.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.redmic.exception.databinding.DTONotValidException;
import es.redmic.models.es.common.dto.SuperDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.service.WindRoseESService;

@RestController
@RequestMapping(value = "${controller.mapping.TIMESERIES}")
public class WindRoseController {

	WindRoseESService service;

	@Autowired
	public WindRoseController(WindRoseESService service) {
		this.service = service;
	}

	@RequestMapping(value = "${controller.mapping.SERIES_WINDROSE}/_search", method = RequestMethod.POST)
	@ResponseBody
	public SuperDTO getRosewindData(@PathVariable(name = "activityId", required = false) String activityId,
			@Valid @RequestBody DataQueryDTO queryDTO, BindingResult bindingResult) {

		if (bindingResult != null && bindingResult.hasErrors())
			throw new DTONotValidException(bindingResult);

		return service.getWindRoseData(queryDTO, activityId);
	}
}