package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;
import java.util.List;

import es.redmic.exception.elasticsearch.ESTermQueryException;

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

		if (min == null || max == null || (min > max) || min == max || partitionNumber == null || partitionNumber == 0)
			throw new ESTermQueryException("partitionNumber", partitionNumber.toString());

		double partitionLength = (max - min) / partitionNumber;

		List<LimitsDTO> limits = new ArrayList<LimitsDTO>();

		double limit = min;
		for (int i = 0; i < partitionNumber; i++) {
			limits.add(new LimitsDTO(limit, limit + partitionLength));
			limit += partitionLength;
		}

		this.setLimits(limits);
	}
}
