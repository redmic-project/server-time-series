package es.redmic.timeseriesview.dto.windrose;

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

public class WindRoseSectorDTO extends ArrayList<WindRoseItemDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WindRoseSectorDTO(Integer total, Integer partitionNumber) {
		super();
		initialize(total, partitionNumber);
	}

	private void initialize(Integer total, Integer partitionNumber) {

		for (int i = 0; i < partitionNumber; i++) {
			add(new WindRoseItemDTO(total));
		}
	}

	public void calculate() {
		for (int i = 0; i < size(); i++)
			get(i).calculate();
	}
}
