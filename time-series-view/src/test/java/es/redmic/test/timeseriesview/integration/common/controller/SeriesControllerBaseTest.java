package es.redmic.test.timeseriesview.integration.common.controller;

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

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.redmic.models.es.common.query.dto.DataQueryDTO;

public abstract class SeriesControllerBaseTest {

	@Autowired
    protected
	ObjectMapper mapper;

	@SuppressWarnings("unchecked")
	protected String getQueryAsString(DataQueryDTO dataQuery) throws JsonProcessingException {

		// Se elimina accessibilityIds ya que no está permitido para usuarios
		// no registrados
		HashMap<String, Object> query = mapper.convertValue(dataQuery, HashMap.class);
		query.remove("accessibilityIds");
		return mapper.writeValueAsString(query);
	}
}
