package es.redmic.timeseriescommands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import es.redmic.commandslib.config.GenerateJsonSchemaScanBean;
import es.redmic.restlib.config.ResourceBundleMessageSource;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootApplication
@ComponentScan({ "es.redmic.timeseriescommand", "es.redmic.restlib", "es.redmic.commandslib",
		"es.redmic.brokerlib.alert" })
public class TimeSeriesCommandsApplication {

	@Value("${info.microservice.name}")
	String microserviceName;

	public static void main(String[] args) {
		SpringApplication.run(TimeSeriesCommandsApplication.class, args);
	}

	@Bean
	public MessageSource messageSource() {

		return new ResourceBundleMessageSource();
	}

	@Bean
	public GenerateJsonSchemaScanBean generateSchemaScanBean() {
		return new GenerateJsonSchemaScanBean();
	}

	@Bean
	MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config().commonTags("application", microserviceName);
	}
}
