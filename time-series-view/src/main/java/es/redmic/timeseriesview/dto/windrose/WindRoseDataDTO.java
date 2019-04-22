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
import java.util.List;

import es.redmic.timeseriesview.dto.timeseries.StatsDTO;

public class WindRoseDataDTO extends RangesOfSplitsDTO {

	private List<WindRoseSectorDTO> data = new ArrayList<WindRoseSectorDTO>();

	private StatsDTO stats;

	private Integer numSectors;

	private Integer partitionNumber;

	public WindRoseDataDTO() {
		super();
	}

	public WindRoseDataDTO(Integer total, Double max, Integer partitionNumber, Integer numSectors) {
		super();
		this.numSectors = numSectors;
		this.partitionNumber = partitionNumber;
		initialize(total);
		setLimits(max);
	}

	private void initialize(Integer total) {

		for (int i = 0; i < numSectors; i++) {
			data.add(new WindRoseSectorDTO(total, partitionNumber));
		}
	}

	public List<WindRoseSectorDTO> getData() {
		return data;
	}

	public void setData(List<WindRoseSectorDTO> data) {
		this.data = data;
	}

	public StatsDTO getStats() {
		return stats;
	}

	public void setStats(StatsDTO stats) {
		this.stats = stats;
	}

	public void addSectorData(WindRoseSectorDTO sectorData) {

		if (data == null)
			data = new ArrayList<WindRoseSectorDTO>();
		data.add(sectorData);
	}

	private void setLimits(Double max) {

		List<LimitsDTO> limits = new ArrayList<LimitsDTO>();

		double partitionLength = (Math.ceil(max) - 0) / partitionNumber;

		double limit = 0;
		for (int i = 0; i < partitionNumber; i++) {
			limits.add(new LimitsDTO(limit, limit + partitionLength));
			limit += partitionLength;
		}

		this.setLimits(limits);
	}

	public void calculate() {

		for (int i = 0; i < numSectors; i++) {
			data.get(i).calculate();
		}
	}
}
