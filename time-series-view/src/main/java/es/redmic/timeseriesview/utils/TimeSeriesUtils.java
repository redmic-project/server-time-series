package es.redmic.timeseriesview.utils;

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
