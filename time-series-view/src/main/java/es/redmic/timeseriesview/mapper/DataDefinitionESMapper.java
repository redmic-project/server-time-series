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

import org.mapstruct.Mapper;

import es.redmic.models.es.maintenance.parameter.dto.DataDefinitionDTO;

@Mapper
public abstract class DataDefinitionESMapper {

	public DataDefinitionDTO map(Long source) {
		DataDefinitionDTO dto = new DataDefinitionDTO();
		dto.setId(source);
		return dto;
	}

	public Long map(DataDefinitionDTO source) {
		return source.getId();
	}
}
