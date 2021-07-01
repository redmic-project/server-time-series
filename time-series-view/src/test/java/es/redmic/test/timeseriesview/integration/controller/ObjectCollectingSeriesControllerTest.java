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
import org.springframework.test.web.servlet.MvcResult;
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
public class ObjectCollectingSeriesControllerTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Autowired
	protected FilterChainProxy springSecurityFilterChain;

	protected MockMvc mockMvc;

	@Autowired
	ObjectMapper mapper;


	@Value("${controller.mapping.OBJECTCOLLECTING}")
	private String OBJECTCOLLECTINGSERIES_BASE_PATH;

	@Value("${controller.mapping.OBJECT_CLASSIFICATION_LIST_SCHEMA}")
	private String OBJECT_CLASSIFICATION_LIST_SCHEMA;

	@BeforeClass
	public static void beforeClass() {
	}

	@Before
	public void setUp() throws JsonParseException, JsonMappingException, IOException {

		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain)
				.build();
	}

	@After
	public void restore() {
	}


	@Test
	public void getObjectClassificationListSchema_Return200_WhenSchemaIsFound() throws Exception {

		String searchSchema = "/data/objectcollectingseries/schema/searchSchema.json";

		// @formatter:off

		this.mockMvc
			.perform(get(OBJECTCOLLECTINGSERIES_BASE_PATH + OBJECT_CLASSIFICATION_LIST_SCHEMA)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.success", is(true)))
			.andExpect(jsonPath("$.body", is(mapper.readValue(getClass().getResource(searchSchema).openStream(), Map.class))));

		// @formatter:on
	}
}
