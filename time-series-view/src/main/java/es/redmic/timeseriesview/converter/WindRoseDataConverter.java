package es.redmic.timeseriesview.converter;

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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import es.redmic.elasticsearchlib.common.utils.ElasticSearchUtils;
import es.redmic.exception.common.NoContentException;
import es.redmic.models.es.geojson.common.model.Aggregations;
import es.redmic.timeseriesview.dto.timeseries.StatsDTO;
import es.redmic.timeseriesview.dto.windrose.LimitsDTO;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

@Component
public class WindRoseDataConverter extends CustomConverter<Aggregations, WindRoseDataDTO> {

	private Integer numSectors;

	private Integer partitionNumber;

	protected final static Logger LOGGER = LoggerFactory.getLogger(WindRoseDataConverter.class);

	@SuppressWarnings("unchecked")
	@Override
	public WindRoseDataDTO convert(Aggregations source, Type<? extends WindRoseDataDTO> destinationType,
			MappingContext mappingContext) {

		// @formatter:off
		
		numSectors = (Integer) mappingContext.getProperty("numSectors");
		
		partitionNumber = (Integer) mappingContext.getProperty("partitionNumber");
		
		System.out.println(source.getAttributes().toString());

		Integer count = (Integer) ElasticSearchUtils.getMapValue(source.getAttributes(),
				"filter#dataDefinitionFilter").get("doc_count");
		
		StatsDTO stats = (StatsDTO) ElasticSearchUtils.getStatsFromAggregation(
				source.getAttributes(),
					"stats_bucket#stats-buckets", StatsDTO.class);
		
		// Sobrescribe count para tener en cuenta todos los datos
		stats.setCount(count);
		
		List<Map<String, Object>> values = (List<Map<String, Object>>) 
				(ElasticSearchUtils.getMapValue(source.getAttributes(), "date_histogram#avg_values_by_interval"))
					.get("buckets");
		
		if (values == null || values.size() == 0) {
			LOGGER.error("No es posible realizar los cálculos. No se ha obtenido resultados");
			throw new NoContentException();
		}
		
		Double sectorLength = 360.0 / numSectors;
		
		Double rotationOffset = sectorLength / 2;
		
		// @formatter:on

		Double max = stats.getMax();

		// Eliminar max
		WindRoseDataDTO windRoseDataDTO = new WindRoseDataDTO(values.size(), max, partitionNumber, numSectors);

		// Se guardan las estadísticas
		windRoseDataDTO.setStats(stats);

		List<LimitsDTO> limits = windRoseDataDTO.getLimits();

		for (int i = 0; i < values.size(); i++) {

			// @formatter:off
			
			Map<String, Object> directionValue = ElasticSearchUtils.getMapValue(
				ElasticSearchUtils.getMapValue(values.get(i), "filter#directionDataDefinitionFilter"),
					"avg#avg_direction"),
				speedData = ElasticSearchUtils.getMapValue(values.get(i), "filter#speedDataDefinitionFilter");
			
			Double direction = (Double) directionValue.get("value"),
					speed = (Double) ElasticSearchUtils.getMapValue(speedData, "avg#avg_speed").get("value");
			
			if (direction != null && speed != null) {
			
				Integer sectorIndex = getSectorIndex(direction, sectorLength, rotationOffset),
					splitIndex = getSplitIndex(speed, limits);
				
				windRoseDataDTO.getData().get(sectorIndex).get(splitIndex).addCount();
			}
			else {
				LOGGER.info("Dirección o velocidad con valores nulos en la agregación", values.get(i).toString());
			}
		}

		// @formatter:on

		// Se calculan los porcentajes con respecto al total de todos los splits de cada
		// uno de los sectores
		windRoseDataDTO.calculate();

		return windRoseDataDTO;
	}

	/**
	 * Devuelve el índice dentro del array de sectores que le corresponde al dato
	 */
	private Integer getSectorIndex(Double value, Double sectorLength, Double rotationOffset) {

		if (((value >= (360 - rotationOffset)) && (value <= 360))
				|| ((value >= 0) && (value < (sectorLength - rotationOffset)))) {
			return 0;
		}

		double limit = sectorLength;
		for (int i = 1; i < numSectors; i++) {

			if ((value >= (limit - rotationOffset)) && (value < (limit + rotationOffset))) {
				return i;
			}
			limit += sectorLength;
		}

		LOGGER.warn("No se ecuentra un sector donde clasificar el siguiente valor de dirección: ", value);
		return null;
	}

	/**
	 * Devuelve el índice dentro del array de divisiones que le corresponde al dato
	 */
	private Integer getSplitIndex(Double value, List<LimitsDTO> limits) {

		for (int i = 0; i < limits.size(); i++) {
			if ((value >= limits.get(i).getMin()) && ((value < limits.get(i).getMax()) || (i == (limits.size() - 1)))) {
				return i;
			}
		}
		LOGGER.warn("No se ecuentra un split donde clasificar el siguiente valor de velocidad: ", value);
		return null;
	}
}
