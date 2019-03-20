package es.redmic.timeseriesview.dto.windrose;

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
