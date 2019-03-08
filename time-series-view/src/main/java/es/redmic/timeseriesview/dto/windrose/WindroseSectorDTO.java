package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;

public class WindroseSectorDTO extends ArrayList<WindroseItemDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void calculateValue(Integer total) {
		for (int i = 0; i < size(); i++)
			get(i).calculateValue(total);
	}
}
