package es.redmic.timeserieslib.unit.dto.series;

import org.junit.Before;
import org.junit.Test;

import es.redmic.testutils.dto.DTOBaseTest;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;

public class TimSeriesDTOTest extends DTOBaseTest<TimeSeriesDTO> {

	private static TimeSeriesDTO dto = new TimeSeriesDTO();

	@Before
	public void reset() {

	}

	@Test
	public void validationDTO_NoReturnError_IfDTOIsCorrect() {

		checkDTOHasNoError(dto);
	}
}
