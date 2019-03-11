package es.redmic.timeserieslib.unit;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import es.redmic.brokerlib.alert.AlertType;
import es.redmic.brokerlib.alert.Message;

public class LibraryConfigTest {

	@Test
	public void requireBrokerComponent_IsSuccess_IfConfigIsCorrect() {

		Message message = new Message("info@redmic.es", "test", "mensaje de test", AlertType.ERROR.name());

		assertNotNull(message);
	}
}
