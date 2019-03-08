package es.redmic.timeseriesview.dto.windrose;

import java.util.ArrayList;
import java.util.List;

public class DatesByDirectionDTO {

	private List<String> dates;

	public DatesByDirectionDTO() {
		dates = new ArrayList<String>();
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}
}
