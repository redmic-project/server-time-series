package es.redmic.timeserieslib.unit.dto.series;

/*-
 * #%L
 * Time series library
 * %%
 * Copyright (C) 2019 REDMIC Project / Server
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;

import org.junit.Before;
import org.junit.Test;

import es.redmic.testutils.dto.DTOBaseTest;
import es.redmic.timeserieslib.dto.series.TimeSeriesDTO;


public class TimSeriesDTOTest extends DTOBaseTest<TimeSeriesDTO> {

	private static TimeSeriesDTO dto = new TimeSeriesDTO();

	@Before
	public void reset() {

		dto.setValue(1.0);
		dto.setDate(new DateTime());
		dto.setRemark("remark");
	}

	@Test
	public void validationDTO_NoReturnError_IfDTOIsCorrect() {

		checkDTOHasNoError(dto);
	}

	@Test
	public void validationDTO_ReturnNotNullError_IfValueIsNull() {

		dto.setValue(null);

		checkDTOHasError(dto, NOT_NULL_MESSAGE_TEMPLATE);
	}

	@Test
	public void validationDTO_ReturnNotNullError_IfDateIsNull() {

		dto.setDate(null);

		checkDTOHasError(dto, NOT_NULL_MESSAGE_TEMPLATE);
	}

	@Test
	public void validationDTO_ReturnSizeError_IfNoteExceedsSize() {

		dto.setRemark(generateString(1500));

		checkDTOHasError(dto, SIZE_MESSAGE_TEMPLATE);
	}
}
