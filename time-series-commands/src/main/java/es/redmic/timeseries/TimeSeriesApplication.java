package es.redmic.timeseries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "es.redmic.timeseries" })
public class TimeSeriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeSeriesApplication.class, args);
	}
}
