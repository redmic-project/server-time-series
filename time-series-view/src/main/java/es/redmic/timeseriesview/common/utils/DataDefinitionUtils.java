package es.redmic.timeseriesview.common.utils;

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

import java.util.ArrayList;
import java.util.List;

import es.redmic.models.es.common.query.dto.DataQueryDTO;

public abstract class DataDefinitionUtils {

	public static void addDataDefinitionFieldToReturn(DataQueryDTO query) {

		List<String> returnFields = query.getReturnFields();

		if (returnFields == null) {
			returnFields = new ArrayList<>();
			returnFields.add("date");
			returnFields.add("value");
			returnFields.add("dataDefinition");
		}
		else
			returnFields.add("dataDefinition");

		query.setReturnFields(returnFields);
	}
}
