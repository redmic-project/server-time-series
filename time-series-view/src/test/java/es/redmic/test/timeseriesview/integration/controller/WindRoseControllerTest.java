package es.redmic.test.timeseriesview.integration.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
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
public class WindRoseControllerTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;

	String windRoseSeries = "/data/windrose/windRoseSeries.json", activityId = "1286";

	@Value("${controller.mapping.TIMESERIES}")
	private String TIMESERIES_PATH;

	@Value("${controller.mapping.SERIES_WINDROSE}")
	private String WINDROSE_PATH;

	@Autowired
	TimeSeriesESRepository repository;

	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {

		TIMESERIES_PATH = TIMESERIES_PATH.replace("{activityId}", activityId);

		// Guardar timeseries de prueba

		TypeReference<List<TimeSeries>> type = new TypeReference<List<TimeSeries>>() {
		};

		List<TimeSeries> timeSeriesList = mapper.readValue(getClass().getResource(windRoseSeries).openStream(), type);

		for (TimeSeries item : timeSeriesList) {
			repository.save(item);
		}

		// @formatter:off

		mockMvc = MockMvcBuilders
				.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();

		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void searchVesselsPost_Return200_WhenSearchIsCorrect() throws Exception {

		DataQueryDTO dataQuery = new DataQueryDTO();

		DateLimitsDTO dateLimits = new DateLimitsDTO();
		dateLimits.setStartDate(new DateTime(2019, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC));
		dateLimits.setEndDate(new DateTime(2019, 1, 2, 0, 0, 0, 0, DateTimeZone.UTC));
		dataQuery.setDateLimits(dateLimits);

		Map<String, Object> terms = new HashMap<String, Object>();

		// @formatter:off

		Integer speedDataDefinition = 20,
				directionDataDefinition = 19,
				numSectors = 16,
				numSplits = 6;
		
		// @formatter:on

		Map<String, Object> dataDefinition = new HashMap<>();
		dataDefinition.put("speed", speedDataDefinition);
		dataDefinition.put("direction", directionDataDefinition);
		terms.put("dataDefinition", dataDefinition);

		terms.put("numSectors", numSectors);
		terms.put("numSplits", numSplits);

		dataQuery.setTerms(terms);

		// Se elimina accessibilityIds ya que no está permitido para usuarios
		// no registrados
		HashMap<String, Object> query = mapper.convertValue(dataQuery, HashMap.class);
		query.remove("accessibilityIds");

		// @formatter:off
		
		ResultActions result = this.mockMvc
				.perform(post(TIMESERIES_PATH + WINDROSE_PATH + "/_search").content(mapper.writeValueAsString(query))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.body.data", notNullValue()))
				.andExpect(jsonPath("$.body.limits.length()", is(numSplits)))
				.andExpect(jsonPath("$.body.data.length()", is(numSectors)));
		
		result.andDo(MockMvcResultHandlers.print());
		
		// @formatter:on
	}
}
