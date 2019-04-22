package es.redmic.timeseriesview.utils;

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

import es.redmic.models.es.common.query.dto.DateLimitsDTO;

public abstract class TimeSeriesUtils {

	// @formatter:off

	public static final Long TIME_INTERVAL_DEFAULT = 3600000L,
			MAX_BUCKETS = 10000L;

	// @formatter:on

	public static Long getTimeInterval(Long timeIntervalDefault, DateLimitsDTO dateLimits) {

		if (timeIntervalDefault == null || dateLimits.getEndDate() == null || dateLimits.getStartDate() == null) {
			return TIME_INTERVAL_DEFAULT;
		}

		Long dateRangeMs = dateLimits.getEndDate().getMillis() - dateLimits.getStartDate().getMillis();

		if ((dateRangeMs / TIME_INTERVAL_DEFAULT) < MAX_BUCKETS) {
			return TIME_INTERVAL_DEFAULT;
		}

		// TimeInterval para maximizar el número de buckets devueltos
		return dateRangeMs / MAX_BUCKETS;
	}
}
