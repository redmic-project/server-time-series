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
