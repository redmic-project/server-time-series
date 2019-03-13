package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;
import java.util.List;

public class WindroseDataDTO extends RangesOfSplitsDTO {

	private List<WindroseSectorDTO> data = new ArrayList<WindroseSectorDTO>();

	public WindroseDataDTO() {
		super();
	}

	public WindroseDataDTO(Double min, Double max, Integer partitionNumber) {
		super();
		this.setLimits(min, max, partitionNumber);
	}

	public List<WindroseSectorDTO> getData() {
		return data;
	}

	public void setData(List<WindroseSectorDTO> data) {
		this.data = data;
	}

	public void addSectorData(WindroseSectorDTO sectorData) {

		if (data == null)
			data = new ArrayList<WindroseSectorDTO>();
		data.add(sectorData);
	}

	private void setLimits(Double min, Double max, Integer partitionNumber) {

		List<LimitsDTO> limits = new ArrayList<LimitsDTO>();

		double partitionLength = (max - min) / partitionNumber;

		double limit = min;
		for (int i = 0; i < partitionNumber; i++) {
			limits.add(new LimitsDTO(limit, limit + partitionLength));
			limit += partitionLength;
		}

		this.setLimits(limits);
	}
}
