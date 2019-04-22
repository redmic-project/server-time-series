package es.redmic.timeseriesview.service;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import es.redmic.timeseriesview.repository.WindRoseESRepository;
import es.redmic.timeseriesview.utils.TimeSeriesUtils;
import es.redmic.timeseriesview.utils.WindRoseUtils;
import es.redmic.viewlib.config.MapperScanBeanItfc;

@Service
public class WindRoseESService {

	private WindRoseESRepository repository;

	@Autowired
	protected MapperScanBeanItfc mapper;

	private static final String GOOD_QFLAG = "1";

	@Autowired
	public WindRoseESService(WindRoseESRepository repository) {
		this.repository = repository;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public ElasticSearchDTO getWindRoseData(DataQueryDTO query, String activityId) {

		// Obtiene datos de la query

		Map<String, Object> dataDefinitionMap = (Map<String, Object>) query.getTerms().get("dataDefinition");

		Integer numSectors = (Integer) query.getTerms().get("numSectors"),
				partitionNumber = (Integer) query.getTerms().get("numSplits");

		List<Integer> speedDataDefinition = (List<Integer>) dataDefinitionMap.get("speed"),
				directionDataDefinition = (List<Integer>) dataDefinitionMap.get("direction");

		Long timeIntervalDefault = new Long(query.getTerms().get("timeInterval").toString());

		WindRoseUtils.checkValidNumSectors(numSectors);
		WindRoseUtils.checkValidPartitionNumber(partitionNumber);

		// Añade a query para comprobar que la actividad corresponde con la buscada
		query.setActivityId(activityId);

		// Se obliga a que los datos sean buenos
		if (query.getQFlags() != null)
			query.getQFlags().clear();

		query.setQFlags(Arrays.asList(GOOD_QFLAG));

		query.setSize(0);

		query.getTerms().put("dataDefinition", new ArrayList<Integer>() {
			{
				addAll(speedDataDefinition);
				addAll(directionDataDefinition);
			}
		});

		query.addAgg(new AggsPropertiesDTO("dataDefinition", StringUtils.join(speedDataDefinition, ",")));
		query.addAgg(new AggsPropertiesDTO("dataDefinition", StringUtils.join(directionDataDefinition, ",")));
		query.addAgg(new AggsPropertiesDTO("interval",
				TimeSeriesUtils.getTimeInterval(timeIntervalDefault, query.getDateLimits()).toString()));

		WindRoseDataDTO windroseDataDTO = repository.getWindRoseData(query, numSectors, partitionNumber);

		return new ElasticSearchDTO(windroseDataDTO, windroseDataDTO.getData().size());
	}
}
