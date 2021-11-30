package es.redmic.test.timeseriesview.unit.mapper;

/*-
 * #%L
 * ElasticSearch
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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import es.redmic.jts4jackson.module.JTSModule;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.timeseriesview.mapper.DataHistogramESMapper;
import es.redmic.timeseriesview.mapper.ObjectCollectingSeriesESMapper;
import es.redmic.timeseriesview.model.objectcollectingseries.ObjectCollectingSeries;

@RunWith(MockitoJUnitRunner.class)
public class ObjectCollectMappingTest {

	protected ObjectMapper jacksonMapper = new ObjectMapper()
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).registerModule(new JTSModule());

	@Test
	public void objectCollectingSeriesESMapper_ReturnObjectCollectingDTO_IfModelAndMapperIsOk() throws Exception {

		String modelPath = "/data/objectcollectingseries/model/searchWrapperObjectCollectingSeriesModel.json";
		String dtoPath = "/data/objectcollectingseries/dto/searchWrapperObjectCollectingSeriesDTO.json";

		SeriesSearchWrapper<ObjectCollectingSeries> model = (SeriesSearchWrapper<ObjectCollectingSeries>)
			getSeriesSearchWrapper(ObjectCollectingSeries.class, modelPath);

		Object dto = Mappers.getMapper(ObjectCollectingSeriesESMapper.class).map(model.getHits());

		String dtoExpected = getJsonString(dtoPath);

		JSONAssert.assertEquals(dtoExpected, jacksonMapper.writeValueAsString(dto), false);
	}

	@Test
	public void objectCollectingSeriesESMapper_ReturnClassificationList_IfModelAndMapperIsOk() throws Exception {

		String modelPath = "/data/objectcollectingseries/model/classificationForList.json";
		String dtoPath = "/data/objectcollectingseries/dto/classificationForList.json";

		SeriesSearchWrapper<ObjectCollectingSeries> model = (SeriesSearchWrapper<ObjectCollectingSeries>)
			getSeriesSearchWrapper(ObjectCollectingSeries.class, modelPath);

		Object dto = Mappers.getMapper(ObjectCollectingSeriesESMapper.class).convertToList(model.getAggregations());

		String dtoExpected = getJsonString(dtoPath);

		JSONAssert.assertEquals(dtoExpected, jacksonMapper.writeValueAsString(dto), false);
	}

	@Test
	public void objectCollectingSeriesESMapper_ReturnClassificationListIncomplete_IfModelAndMapperIsOk() throws Exception {

		String modelPath = "/data/objectcollectingseries/model/classificationForListIncomplete.json";
		String dtoPath = "/data/objectcollectingseries/dto/classificationForListIncomplete.json";

		SeriesSearchWrapper<ObjectCollectingSeries> model = (SeriesSearchWrapper<ObjectCollectingSeries>)
			getSeriesSearchWrapper(ObjectCollectingSeries.class, modelPath);

		Object dto = Mappers.getMapper(ObjectCollectingSeriesESMapper.class).convertToList(model.getAggregations());

		String dtoExpected = getJsonString(dtoPath);

		JSONAssert.assertEquals(dtoExpected, jacksonMapper.writeValueAsString(dto), false);
	}

	@Test
	public void objectCollectingSeriesESMapper_ReturnClassificationPieChart_IfModelAndMapperIsOk() throws Exception {

		String modelPath = "/data/objectcollectingseries/model/classificationForPieChart.json";
		String dtoPath = "/data/objectcollectingseries/dto/classificationForPieChart.json";

		SeriesSearchWrapper<ObjectCollectingSeries> model = (SeriesSearchWrapper<ObjectCollectingSeries>)
			getSeriesSearchWrapper(ObjectCollectingSeries.class, modelPath);

		Object dto = Mappers.getMapper(ObjectCollectingSeriesESMapper.class).convertToPieChart(model.getAggregations());

		String dtoExpected = getJsonString(dtoPath);

		JSONAssert.assertEquals(dtoExpected, jacksonMapper.writeValueAsString(dto), false);
	}

	@Test
	public void objectCollectingSeriesESMapper_ReturnClassificationLineChart_IfModelAndMapperIsOk() throws Exception {

		String modelPath = "/data/objectcollectingseries/model/classificationTotalForLineChart.json";
		String dtoPath = "/data/objectcollectingseries/dto/classificationTotalForLineChart.json";

		SeriesSearchWrapper<ObjectCollectingSeries> model = (SeriesSearchWrapper<ObjectCollectingSeries>)
			getSeriesSearchWrapper(ObjectCollectingSeries.class, modelPath);

		Object dto = Mappers.getMapper(DataHistogramESMapper.class).map(model.getAggregations());

		String dtoExpected = getJsonString(dtoPath);

		JSONAssert.assertEquals(dtoExpected, jacksonMapper.writeValueAsString(dto), false);
	}

	@SuppressWarnings("unchecked")
	private SeriesSearchWrapper<ObjectCollectingSeries> getSeriesSearchWrapper(Class<ObjectCollectingSeries> model,
		String modelPath) throws JsonParseException, JsonMappingException, IOException {

		JavaType modelWrapperType = jacksonMapper.getTypeFactory().constructParametricType(SeriesSearchWrapper.class,
				model);
		return (SeriesSearchWrapper<ObjectCollectingSeries>) jacksonMapper.readValue(
			getJsonString(modelPath), modelWrapperType);
	}

	protected String getJsonString(String filePath) throws IOException {

		return IOUtils.toString(getClass().getResource(filePath).openStream());
	}
}
