package es.redmic.timeseriesview.dto.timeseries;

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

public class StatsDTO {

	private Double min;

	private Double max;

	private Integer count;

	private Double sum;

	private Double avg;

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

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Double getSum() {
		return DecimalUtils.roundDecimals(sum);
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	public Double getAvg() {
		return DecimalUtils.roundDecimals(avg);
	}

	public void setAvg(Double avg) {
		this.avg = avg;
	}
}
