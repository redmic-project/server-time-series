package es.redmic.timeseriesview.dto.windrose;

import com.fasterxml.jackson.annotation.JsonIgnore;

import es.redmic.models.es.utils.DecimalUtils;

public class WindRoseItemDTO {

	private Integer total;

	public WindRoseItemDTO(Integer total) {
		this.total = total;
		this.count = 0;
	}

	private Double value;

	@JsonIgnore
	private Integer count;

	public Double getValue() {
		return DecimalUtils.roundDecimals(value);
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void addCount() {
		this.count++;
	}

	public void calculate() {
		double aux = (double) count / total;
		setValue(aux * 100);
	}
}