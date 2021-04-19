package es.redmic.timeseriesview.model.timeseries;

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

import es.redmic.timeseriesview.model.common.SeriesCommon;

public class TimeSeries extends SeriesCommon {

    private Double z;

	private Double deviation;

	private double value;

	private Long dataDefinition;

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public Double getDeviation() {
		return deviation;
	}

	public void setDeviation(Double deviation) {
		this.deviation = deviation;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Long getDataDefinition() {
		return dataDefinition;
	}

	public void setDataDefinition(Long dataDefinition) {
		this.dataDefinition = dataDefinition;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		return super.equals(obj);
	}
}
