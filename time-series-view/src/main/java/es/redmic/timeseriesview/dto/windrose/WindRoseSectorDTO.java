package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;

public class WindRoseSectorDTO extends ArrayList<WindRoseItemDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WindRoseSectorDTO(Integer total, Integer partitionNumber) {
		super();
		initialize(total, partitionNumber);
	}

	private void initialize(Integer total, Integer partitionNumber) {

		for (int i = 0; i < partitionNumber; i++) {
			add(new WindRoseItemDTO(total));
		}
	}

	public void calculate() {
		for (int i = 0; i < size(); i++)
			get(i).calculate();
	}
}
