package es.redmic.timeseriesview.dto.timeseries;

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
