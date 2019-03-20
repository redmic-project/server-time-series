package es.redmic.timeseriesview.utils;

import es.redmic.exception.elasticsearch.ESTermQueryException;

public abstract class WindRoseUtils {

	private final static Integer PARTITION_NUMBER_LIMIT = 10;

	public static void checkValidNumSectors(Integer numSectors) {
		// Comprueba que numSectors sea múltiplo de 4 y menor o igual a 36

		if (numSectors > 36 || (numSectors % 4) != 0)
			throw new ESTermQueryException("numSectors", numSectors.toString());
	}

	public static void checkValidPartitionNumber(Integer partitionNumber) {

		if (partitionNumber > PARTITION_NUMBER_LIMIT || partitionNumber < 1)
			throw new ESTermQueryException("partitionNumber", partitionNumber.toString());
	}
}
