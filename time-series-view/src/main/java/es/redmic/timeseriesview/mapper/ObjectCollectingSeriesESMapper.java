package es.redmic.timeseriesview.mapper;

/*-
 * #%L
 * Time series view
 * %%
 * Copyright (C) 2019 - 2021 REDMIC Project / Server
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.redmic.elasticsearchlib.common.utils.ElasticSearchUtils;
import es.redmic.models.es.common.dto.JSONCollectionDTO;
import es.redmic.models.es.common.model.DomainES;
import es.redmic.models.es.geojson.common.domain.dto.ConfidenceDTO;
import es.redmic.models.es.geojson.common.model.Aggregations;
import es.redmic.models.es.maintenance.objects.dto.ObjectClassificationDTO;
import es.redmic.models.es.maintenance.objects.model.ObjectClassification;
import es.redmic.models.es.series.common.model.SeriesHitWrapper;
import es.redmic.models.es.series.common.model.SeriesHitsWrapper;
import es.redmic.models.es.series.common.model.SeriesSearchWrapper;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationForListDTO;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationForPieChartDTO;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationsForListDTO;
import es.redmic.models.es.series.objectcollecting.dto.ClassificationsForPieChartDTO;
import es.redmic.models.es.series.objectcollecting.dto.IntervalAggregationDTO;
import es.redmic.models.es.series.objectcollecting.dto.ObjectClassificationForListDTO;
import es.redmic.timeserieslib.dto.objectcollecting.ObjectCollectingSeriesDTO;
import es.redmic.timeseriesview.common.mapper.SeriesESMapper;
import es.redmic.timeseriesview.model.objectcollectingseries.ObjectCollectingSeries;

@Mapper
public abstract class ObjectCollectingSeriesESMapper extends SeriesESMapper<ObjectCollectingSeriesDTO, ObjectCollectingSeries> {

	protected final Logger LOGGER = LoggerFactory.getLogger(ObjectCollectingSeriesESMapper.class);

	public abstract ConfidenceDTO map(DomainES confidence);

	public abstract List<ObjectClassificationDTO> map(List<ObjectClassification> objectClassifications);

	public abstract ObjectCollectingSeriesDTO mapBase(ObjectCollectingSeries model);

	public ObjectCollectingSeriesDTO map(ObjectCollectingSeries model) {

		ObjectCollectingSeriesDTO dto = mapBase(model);

		if (model.getLocalityConfidence() != null)
			dto.setConfidence(map(model.getLocalityConfidence()));

		return dto;
	}

	public ObjectCollectingSeriesDTO map(SeriesHitWrapper<ObjectCollectingSeries> viewResult) {
		return map(viewResult.get_source());
	}

	public JSONCollectionDTO map(SeriesSearchWrapper<ObjectCollectingSeries> viewResult) {

		JSONCollectionDTO result = map(viewResult.getHits());
		result.set_aggs(getAggs(viewResult.getAggregations()));
		return result;
	}

	public JSONCollectionDTO map(SeriesHitsWrapper<ObjectCollectingSeries> seriesHitsWrapper) {

		JSONCollectionDTO result = new JSONCollectionDTO();
		result.setData(mapList(seriesHitsWrapper.getHits()));
		result.get_meta().setMax_score(seriesHitsWrapper.getMax_score());
		result.setTotal(seriesHitsWrapper.getTotal());
		return result;
	}

	public List<ObjectCollectingSeriesDTO> mapList(List<SeriesHitWrapper<ObjectCollectingSeries>> dataHitWrapper) {

		List<ObjectCollectingSeriesDTO> list = new ArrayList<>();
		for (SeriesHitWrapper<ObjectCollectingSeries> entity : dataHitWrapper) {
			list.add(map(entity));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public ClassificationsForListDTO convertToList(Aggregations source) {
		ClassificationsForListDTO classificationList = new ClassificationsForListDTO();

		Map<String, Object> aggregations = source.getAttributes();
		if (aggregations == null || aggregations.size() == 0)
			return classificationList;

		List<Map<String, Object>> classificationType = ElasticSearchUtils.getBucketsFromAggregations(aggregations);

		List<ClassificationForListDTO> classifications = new ArrayList<>();
		for (int i = 0; i < classificationType.size(); i++) {
			/** Nuevo tipo de clasificación **/
			ClassificationForListDTO classification = new ClassificationForListDTO();
			classification.setName(classificationType.get(i).get("key").toString());
			/** Obtiene las estadisticas a nivel de tipo de classificación **/
			List<Map<String, Object>> timeIntervals = ElasticSearchUtils
					.getBucketsFromAggregations((Map<String, Object>) classificationType.get(i).get("reverse_nested#timeIntervals"));
			for (int timeIntervalsIt = 0; timeIntervalsIt < timeIntervals.size(); timeIntervalsIt++) {
				classification.addHeader(timeIntervals.get(timeIntervalsIt).get("key_as_string").toString());
				classification.addV(getValue((Map<String, Object>) timeIntervals.get(timeIntervalsIt).get("stats#value")));
			}
			/** Obtiene todas las clasificaciones del tipo actual **/
			List<Map<String, Object>> levels = ElasticSearchUtils.getBucketsFromAggregations(
					(Map<String, Object>) classificationType.get(i).get("nested#objectClassification"));
			List<ObjectClassificationForListDTO> data = new ArrayList<>();

			for (int levelIt = 0; levelIt < levels.size(); levelIt++) { // niveles de clasificación

				/** Obtiene cada uno de los elementos de la clasificación **/
				List<Map<String, Object>> objects = ElasticSearchUtils.getBucketsFromAggregations(
						(Map<String, Object>) levels.get(levelIt).get("sterms#objectClassificationPath"));
				boolean isLeave = (levelIt == levels.size() - 1);

				for (int objectIt = 0; objectIt < objects.size(); objectIt++) { // objectos
					ObjectClassificationForListDTO object = new ObjectClassificationForListDTO();
					object.initV(classification.getHeaderSize());
					object.setPath(objects.get(objectIt).get("key").toString());
					if (!isLeave)
						object.setLeaves(1);

					List<Map<String, Object>> type = ElasticSearchUtils.getBucketsFromAggregations(
							(Map<String, Object>) objects.get(objectIt).get("sterms#objectClassificationName"));
					object.setCategory(type.get(0).get("key").toString());

					List<Map<String, Object>> timeIntervalsObject = ElasticSearchUtils
							.getBucketsFromAggregations((Map<String, Object>) type.get(0).get("reverse_nested#timeIntervals"));

					for (int timeIntervalsIt = 0; timeIntervalsIt < timeIntervalsObject.size(); timeIntervalsIt++) {
						// posición donde se debe insertar el dato y que depende del intervalo actual
						int pos = classification
								.getHeaderPos(timeIntervalsObject.get(timeIntervalsIt).get("key_as_string").toString());

						Map<String, Object> stats = (Map<String, Object>) timeIntervalsObject.get(timeIntervalsIt)
								.get("stats#value");
						object.setV(pos, getValue(stats));
						// Setea el número de hijos si no es una hoja
						Integer count = (Integer) stats.get("count");
						if (!isLeave && (count > object.getLeaves()))
							object.setLeaves(count);
					}
					data.add(object);
				}
				classification.setData(data);
			}
			classifications.add(classification);
		}
		classificationList.setClassification(classifications);
		return classificationList;
	}

	@SuppressWarnings("unchecked")
	public ClassificationsForPieChartDTO convertToPieChart(Aggregations source) {
		ClassificationsForPieChartDTO classificationList = new ClassificationsForPieChartDTO();

		Map<String, Object> aggregations = source.getAttributes();
		if (aggregations == null || aggregations.size() == 0)
			return classificationList;

		List<Map<String, Object>> classificationIntervals = ElasticSearchUtils.getBucketsFromAggregations(aggregations);

		List<ClassificationForPieChartDTO> classifications = new ArrayList<>();
		for (int i = 0; i < classificationIntervals.size(); i++) {

			String timeInterval = classificationIntervals.get(i).get("key_as_string").toString();

			List<Map<String, Object>> types = ElasticSearchUtils
					.getBucketsFromAggregations((Map<String, Object>) classificationIntervals.get(i).get("nested#object"));

			for (int typesIt = 0; typesIt < types.size(); typesIt++) {

				/** Nuevo tipo de clasificación para el interval **/

				IntervalAggregationDTO interval = new IntervalAggregationDTO();
				interval.setTimeInterval(timeInterval);

				String classificationName = types.get(typesIt).get("key").toString();

				ClassificationForPieChartDTO classification = getClassificationForName(classifications,
						classificationName);

				List<IntervalAggregationDTO> data = classification.getData();

				/** Obtiene todas las clasificaciones para recorrerlas por niveles **/
				List<Map<String, Object>> levels = ElasticSearchUtils.getBucketsFromAggregations(
						(Map<String, Object>) types.get(typesIt).get("nested#objectClassification"));

				for (int levelIt = 0; levelIt < levels.size(); levelIt++) {

					/**
					 * Obtiene cada uno de los elementos de la clasificación
					 **/
					List<Map<String, Object>> objects = ElasticSearchUtils.getBucketsFromAggregations(
							(Map<String, Object>) levels.get(levelIt).get("sterms#objectClassificationPath"));

					for (int objectIt = 0; objectIt < objects.size(); objectIt++) { // objectos

						List<Map<String, Object>> type = ElasticSearchUtils.getBucketsFromAggregations(
								(Map<String, Object>) objects.get(objectIt).get("sterms#objectClassificationName"));
						/**
						 * Obtiene las estadisticas a nivel de tipo de classificación
						 **/
						Map<String, Object> stats = (Map<String, Object>) type.get(0).get("reverse_nested#stats");

						interval.addCategory(objects.get(objectIt).get("key").toString(),
								type.get(0).get("key").toString(), getValue((Map<String, Object>) stats.get("stats#value")));
					}
				}
				data.add(interval);
				classification.setData(data);

				addToClassifications(classifications, classification);
			}
		}
		classificationList.setClassification(classifications);
		return classificationList;
	}

	private ClassificationForPieChartDTO getClassificationForName(List<ClassificationForPieChartDTO> classifications,
			String classificationName) {

		for (int i = 0; i < classifications.size(); i++) {
			if (classifications.get(i).getName().equals(classificationName))
				return classifications.get(i);
		}

		ClassificationForPieChartDTO classification = new ClassificationForPieChartDTO();
		classification.setName(classificationName);
		classification.setData(new ArrayList<>());
		return classification;
	}

	private void addToClassifications(List<ClassificationForPieChartDTO> classifications,
			ClassificationForPieChartDTO classification) {

		if (!classifications.contains(classification))
			classifications.add(classification);
	}

	private int getValue(Map<String,Object> stats) {

		Object sum = stats.get("sum");
		int value = 0;
		if (sum != null)
			value = (int) (double) stats.get("sum");

		return value;
	}
}
