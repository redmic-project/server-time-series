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

import es.redmic.models.es.utils.DecimalUtils;

public class LimitsDTO {

	private Double min;

	private Double max;

	public LimitsDTO(Double min, Double max) {
		setMin(min);
		setMax(max);
	}

	public Double getMin() {
		return DecimalUtils.roundDecimals(min);
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return DecimalUtils.roundDecimals(max);
	}

	public void setMax(Double max) {
		this.max = max;
	}
}
