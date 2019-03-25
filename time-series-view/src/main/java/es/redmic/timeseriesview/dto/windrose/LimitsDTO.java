package es.redmic.timeseriesview.dto.windrose;

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
