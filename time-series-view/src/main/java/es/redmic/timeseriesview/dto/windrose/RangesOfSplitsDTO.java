package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;
import java.util.List;

public class RangesOfSplitsDTO {

	private List<LimitsDTO> limits = new ArrayList<LimitsDTO>();

	public List<LimitsDTO> getLimits() {
		return limits;
	}

	public void setLimits(List<LimitsDTO> limits) {
		this.limits = limits;
	}

	public void addRange(Double min, Double max) {
		limits.add(new LimitsDTO(min, max));
	}
}
