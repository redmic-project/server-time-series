package es.redmic.test.timeseriesview.unit.utils;

import org.junit.Test;

import es.redmic.exception.elasticsearch.ESTermQueryException;
import es.redmic.timeseriesview.utils.WindRoseUtils;

public class WindRoseUtilsTest {

	@Test(expected = ESTermQueryException.class)
	public void checkValidNumSectors_ThrowException_IfNumSectorsIsGreatThan36() {

		WindRoseUtils.checkValidNumSectors(40);
	}

	@Test(expected = ESTermQueryException.class)
	public void checkValidNumSectors_ThrowException_IfNumSectorsIsLessThan4() {

		WindRoseUtils.checkValidNumSectors(2);
	}

	@Test
	public void checkValidNumSectors_NoThrowException_IfNumSectorsIsCorrect() {

		WindRoseUtils.checkValidNumSectors(4);
		WindRoseUtils.checkValidNumSectors(8);
		WindRoseUtils.checkValidNumSectors(12);
		WindRoseUtils.checkValidNumSectors(16);
		WindRoseUtils.checkValidNumSectors(20);
		WindRoseUtils.checkValidNumSectors(24);
		WindRoseUtils.checkValidNumSectors(28);
		WindRoseUtils.checkValidNumSectors(32);
		WindRoseUtils.checkValidNumSectors(36);
	}

	@Test(expected = ESTermQueryException.class)
	public void checkValidPartitionNumber_ThrowException_IfPartitionNumberIsGreatThan10() {

		WindRoseUtils.checkValidPartitionNumber(11);
	}

	@Test(expected = ESTermQueryException.class)
	public void checkValidPartitionNumber_ThrowException_IfPartitionNumberIsLessThan1() {

		WindRoseUtils.checkValidPartitionNumber(0);
	}

	@Test
	public void checkValidPartitionNumber_NoThrowException_IfPartitionNumberIsCorrect() {

		WindRoseUtils.checkValidPartitionNumber(1);
		WindRoseUtils.checkValidPartitionNumber(2);
		WindRoseUtils.checkValidPartitionNumber(3);
		WindRoseUtils.checkValidPartitionNumber(4);
		WindRoseUtils.checkValidPartitionNumber(5);
		WindRoseUtils.checkValidPartitionNumber(6);
		WindRoseUtils.checkValidPartitionNumber(7);
		WindRoseUtils.checkValidPartitionNumber(8);
		WindRoseUtils.checkValidPartitionNumber(9);
		WindRoseUtils.checkValidPartitionNumber(10);
	}
}
