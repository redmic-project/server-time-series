package es.redmic.timeseriesview.model.common;

public class SeriesBase extends SeriesCommon {

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
}
