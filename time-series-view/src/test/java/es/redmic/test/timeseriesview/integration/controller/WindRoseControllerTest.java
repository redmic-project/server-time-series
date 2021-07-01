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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.common.query.dto.DateLimitsDTO;
import es.redmic.test.timeseriesview.integration.common.controller.SeriesControllerBaseTest;
import es.redmic.timeseriesview.TimeSeriesViewApplication;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.timeseriesview.repository.TimeSeriesESRepository;

@SpringBootTest(classes = { TimeSeriesViewApplication.class })
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class WindRoseControllerTest extends SeriesControllerBaseTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

	private String windRoseSeries = "/data/windrose/windRoseData.json";

	private String activityId = "1286";

	@Value("${controller.mapping.TIMESERIES_ACTIVITY}")
	private String TIMESERIES_ACTIVITY_PATH;

	@Value("${controller.mapping.SERIES_WINDROSE}")
	private String WINDROSE_PATH;

	@Autowired
	TimeSeriesESRepository repository;

	private static DataQueryDTO dataQuery;

	private static HashMap<String, Object> query;

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

		List<Integer> speedDataDefinition = new ArrayList<Integer>() {{
			add(20);
			add(21);
		}},
		directionDataDefinition = new ArrayList<Integer>() {{
			add(18);
			add(19);
		}};

		// @formatter:on

		Map<String, Object> dataDefinition = new HashMap<>();
		dataDefinition.put("speed", speedDataDefinition);
		dataDefinition.put("direction", directionDataDefinition);
		terms.put("dataDefinition", dataDefinition);

		terms.put("timeInterval", 3600000);

		dataQuery.setTerms(terms);
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain)
				.build();

		TIMESERIES_ACTIVITY_PATH = TIMESERIES_ACTIVITY_PATH.replace("{activityId}", activityId);

		// Guardar timeseries de prueba

		TypeReference<List<TimeSeries>> type = new TypeReference<List<TimeSeries>>() {
		};

		List<TimeSeries> timeSeriesList = mapper.readValue(getClass().getResource(windRoseSeries).openStream(), type);

		for (TimeSeries item : timeSeriesList) {
			repository.save(item);
		}

		// Se elimina accessibilityIds ya que no está permitido para usuarios
		// no registrados
		query = mapper.convertValue(dataQuery, HashMap.class);
		query.remove("accessibilityIds");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windRose16Sectors6Splits_Return200_WhenSearchIsCorrect() throws Exception {

		// @formatter:off

		Integer numSectors = 16,
				numSplits = 6;

		String windRoseResult = "/data/windrose/windRoseResult16Sectors6Splits.json";


		((Map<String, Object>) query.get("terms")).put("numSectors", numSectors);
		((Map<String, Object>) query.get("terms")).put("numSplits", numSplits);

		this.mockMvc
				.perform(post(TIMESERIES_ACTIVITY_PATH + WINDROSE_PATH + "/_search").content(mapper.writeValueAsString(query))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body.limits.length()", is(numSplits)))
				.andExpect(jsonPath("$.body.data.length()", is(numSectors)))
				.andExpect(jsonPath("$.body", is(mapper.readValue(getClass().getResource(windRoseResult).openStream(), Map.class))));

		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windRose36Sectors10Splits_Return200_WhenSearchIsCorrect() throws Exception {

		// @formatter:off

		Integer numSectors = 36,
				numSplits = 10;

		String windRoseResult = "/data/windrose/windRoseResult36Sectors10Splits.json";

		((Map<String, Object>) query.get("terms")).put("numSectors", numSectors);
		((Map<String, Object>) query.get("terms")).put("numSplits", numSplits);

		this.mockMvc
				.perform(post(TIMESERIES_ACTIVITY_PATH + WINDROSE_PATH + "/_search").content(mapper.writeValueAsString(query))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body.limits.length()", is(numSplits)))
				.andExpect(jsonPath("$.body.data.length()", is(numSectors)))
				.andExpect(jsonPath("$.body", is(mapper.readValue(getClass().getResource(windRoseResult).openStream(), Map.class))));

		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void windRose_Return204_WhenNoDataFound() throws Exception {

		// @formatter:off

		Integer numSectors = 16,
				numSplits = 6;

		((Map<String, Object>) query.get("terms")).put("numSectors", numSectors);
		((Map<String, Object>) query.get("terms")).put("numSplits", numSplits);

		((Map<String, Object>) query.get("dateLimits")).put("startDate", new DateTime(2018, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		((Map<String, Object>) query.get("dateLimits")).put("endDate", new DateTime(2018, 2, 1, 0, 0, 0, 0, DateTimeZone.UTC));

		this.mockMvc
				.perform(post(TIMESERIES_ACTIVITY_PATH + WINDROSE_PATH + "/_search").content(mapper.writeValueAsString(query))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(204));

		// @formatter:on
	}
}
