package es.redmic.test.timeseriescommands.integration.translation;

import java.text.MessageFormat;
import java.util.Locale;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.redmic.commandslib.exceptions.HistoryNotFoundException;
import es.redmic.exception.common.PatternUtils;
import es.redmic.timeseriescommands.TimeSeriesCommandsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { TimeSeriesCommandsApplication.class })
@ActiveProfiles("test")
@TestPropertySource(properties = { "schema.registry.port=0" })
public class ExceptionsTranslationTest {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1);

	@Autowired
	MessageSource messageSource;

	protected static final String resourcePathSpanish = "classpath*:i18n/messages_es_ES.properties",
			resourcePathEnglish = "classpath*:i18n/messages_en_EN.properties";

	@Test
	public void getEnglishMessage_returnI18nMessageInDefaultLocale_WhenCodePropertyExist() {

		try {
			throw new HistoryNotFoundException("UpdateTimeSeriesCommand", "1");
		} catch (HistoryNotFoundException e) {

			String code = e.getCode().toString();

			String[] fields = (String[]) e.getFieldErrors().toArray();

			String mess = messageSource.getMessage(code, fields, new Locale("en", "EN"));

			Assert.assertEquals(getMessage(code, fields, resourcePathEnglish), mess);
		}
	}

	@Test
	public void getSpanishMessage_returnI18nMessage_WhenCodePropertyExist() {

		try {
			throw new HistoryNotFoundException("UpdateTimeSeriesCommand", "1");
		} catch (HistoryNotFoundException e) {

			String code = e.getCode().toString();

			String[] fields = (String[]) e.getFieldErrors().toArray();

			String mess = messageSource.getMessage(code, fields, new Locale("es", "ES"));

			Assert.assertEquals(getMessage(code, fields, resourcePathSpanish), mess);
		}
	}

	@Test
	public void getRussianLanguageMessage_returnDefaultI18nMessage_WhenI18nFileNotExist() {

		try {
			throw new HistoryNotFoundException("UpdateTimeSeriesCommand", "1");
		} catch (HistoryNotFoundException e) {

			String code = e.getCode().toString();

			String[] fields = (String[]) e.getFieldErrors().toArray();

			String mess = messageSource.getMessage(code, fields, new Locale("ru", "RU"));

			Assert.assertEquals(getMessage(code, fields, resourcePathSpanish), mess);
		}
	}

	@Test
	public void getMessage_returnCode_WhenCodeNotInI18nFile() {

		String code = "CodeNotFound";

		String mess = messageSource.getMessage(code, null, new Locale("es", "ES"));

		Assert.assertEquals(code, mess);
	}

	private String getMessage(String code, String[] fields, String resourcePath) {

		String message = PatternUtils.getPattern(code, resourcePath);
		MessageFormat format = new MessageFormat(message);

		return format.format(fields);
	}

}
