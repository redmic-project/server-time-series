package es.redmic.test.timeseriesview.integration.controller;

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.common.query.dto.DateLimitsDTO;
import es.redmic.timeseriesview.TimeSeriesViewApplication;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.timeseriesview.repository.TimeSeriesESRepository;

@SpringBootTest(classes = { TimeSeriesViewApplication.class })
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class TimeSeriesControllerTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	private String timeSeries = "/data/timeseries/timeSeriesData.json";

	private String activityId = "1286";

	@Value("${controller.mapping.TIMESERIES}")
	private String TIMESERIES_PATH;

	@Value("${controller.mapping.SERIES_TEMPORALDATA}")
	private String TEMPORALDATA_PATH;

	@Autowired
	TimeSeriesESRepository repository;

	private static DataQueryDTO dataQuery;

	@BeforeClass
	public static void beforeClass() {

		// Se forma la query
		dataQuery = new DataQueryDTO();

		DateLimitsDTO dateLimits = new DateLimitsDTO();
		dateLimits.setStartDate(new DateTime(2019, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dateLimits.setEndDate(new DateTime(2019, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dataQuery.setDateLimits(dateLimits);

		Map<String, Object> terms = new HashMap<String, Object>();

		// @formatter:off

		List<Integer> dataDefinition = new ArrayList<Integer>();
		dataDefinition.add(18);
		dataDefinition.add(19);
		dataDefinition.add(20);
		dataDefinition.add(21);

		// @formatter:on

		terms.put("dataDefinition", dataDefinition);

		dataQuery.setTerms(terms);
	}

	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain)
				.build();

		TIMESERIES_PATH = TIMESERIES_PATH.replace("{activityId}", activityId);

		// Guardar timeseries de prueba

		TypeReference<List<TimeSeries>> type = new TypeReference<List<TimeSeries>>() {
		};

		List<TimeSeries> timeSeriesList = mapper.readValue(getClass().getResource(timeSeries).openStream(), type);

		for (TimeSeries item : timeSeriesList) {
			repository.save(item);
		}
	}

	@After
	public void restore() {

		dataQuery.getDateLimits().setStartDate(new DateTime(2019, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dataQuery.getDateLimits().setEndDate(new DateTime(2019, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC));
	}


	@Test
	public void timeSeries_Return200AndAggDataResult_WhenDataFoundAndTimeIntervalQuery() throws Exception {

		// @formatter:off

		String timeSeriesRawResult = "/data/timeseries/timeSeriesAggResult.json";

		dataQuery.setInterval("1h");

		this.mockMvc
				.perform(post(TIMESERIES_PATH + TEMPORALDATA_PATH + "/_search").content(getQueryAsString(dataQuery))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body", is(mapper.readValue(getClass().getResource(timeSeriesRawResult).openStream(), Map.class))));
		// @formatter:on
	}

	@Test
	public void timeSeries_Return200AndRawDataResult_WhenDataFoundAndNotTimeIntervalQuery() throws Exception {

		// @formatter:off

		this.mockMvc
				.perform(post(TIMESERIES_PATH + TEMPORALDATA_PATH + "/_search").content(getQueryAsString(dataQuery))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body.data.length()", is(2422)))
				.andExpect(jsonPath("$.body.data[0].value", is(16.1)))
				.andExpect(jsonPath("$.body.data[0].date", is("2019-01-01T01:55:00.000Z")));
		// @formatter:on
	}

	@Test
	public void timeSeries_Return200AndEmptyData_WhenNoDataFound() throws Exception {

		// @formatter:off

		dataQuery.getDateLimits().setStartDate(new DateTime(2018, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dataQuery.getDateLimits().setEndDate(new DateTime(2018, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC));

		this.mockMvc
				.perform(post(TIMESERIES_PATH + TEMPORALDATA_PATH + "/_search").content(getQueryAsString(dataQuery))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body.data.length()", is(0)));

		// @formatter:on
	}

	@Test
	public void timeSeries_Return200_WhenSearchSchema() throws Exception {

		// @formatter:off

		this.mockMvc
				.perform(get(TIMESERIES_PATH + TEMPORALDATA_PATH + "/_search/_schema")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.schema", notNullValue()))
				.andExpect(jsonPath("$.body.schema.title", is("Data Query DTO")));

		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private String getQueryAsString(DataQueryDTO dataQuery) throws JsonProcessingException {

		// Se elimina accessibilityIds ya que no está permitido para usuarios
		// no registrados
		HashMap<String, Object> query = mapper.convertValue(dataQuery, HashMap.class);
		query.remove("accessibilityIds");
		return mapper.writeValueAsString(query);
	}
}
