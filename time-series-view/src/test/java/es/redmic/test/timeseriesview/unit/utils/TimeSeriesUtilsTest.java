package es.redmic.test.timeseriesview.unit.utils;

/*-
 * #%L
 * Time series view
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import es.redmic.models.es.common.query.dto.DateLimitsDTO;
import es.redmic.timeseriesview.utils.TimeSeriesUtils;

public class TimeSeriesUtilsTest {

	@Test
	public void getTimeInterval_ReturnDefaultTimeInterval_IfTimeIntervalIsNull() {

		assertEquals(TimeSeriesUtils.TIME_INTERVAL_DEFAULT, TimeSeriesUtils.getTimeInterval(null, new DateLimitsDTO()));
	}

	@Test
	public void getTimeInterval_ReturnDefaultTimeInterval_IfDateLimitHasNullValue() {

		assertEquals(TimeSeriesUtils.TIME_INTERVAL_DEFAULT,
				TimeSeriesUtils.getTimeInterval(3600L, new DateLimitsDTO()));
	}

	@Test
	public void getTimeInterval_ReturnOriginalTimeInterval_IfResultsIsLessThanMaxBuckets() {

		Long originalTimeInterval = 3600000L;

		DateLimitsDTO dateLimits = new DateLimitsDTO();
		dateLimits.setStartDate(new DateTime(2019, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dateLimits.setEndDate(new DateTime(2019, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		assertEquals(originalTimeInterval, TimeSeriesUtils.getTimeInterval(originalTimeInterval, dateLimits));
	}

	@Test
	public void getTimeInterval_ReturnCalculateTimeInterval_IfResultsIsGreatThanMaxBuckets() {

		Long originalTimeInterval = 3600000L;

		DateLimitsDTO dateLimits = new DateLimitsDTO();
		dateLimits.setStartDate(new DateTime(2018, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dateLimits.setEndDate(new DateTime(2019, 4, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		assertNotEquals(originalTimeInterval, TimeSeriesUtils.getTimeInterval(originalTimeInterval, dateLimits));
	}
}
