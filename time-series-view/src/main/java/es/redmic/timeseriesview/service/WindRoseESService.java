package es.redmic.timeseriesview.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.redmic.exception.elasticsearch.ESTermQueryException;
import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import es.redmic.timeseriesview.repository.WindRoseESRepository;
import es.redmic.timeseriesview.utils.TimeSeriesUtils;
import es.redmic.viewlib.config.MapperScanBeanItfc;

@Service
public class WindRoseESService {

	private final static Integer PARTITION_NUMBER_LIMIT = 10;

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

		checkValidNumSectors(numSectors);
		checkValidPartitionNumber(partitionNumber);

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

	private void checkValidNumSectors(Integer numSectors) {
		// Comprueba que numSectors sea un número válido. An = 2^(n-1)

		double x = (Math.log(numSectors) / Math.log(2));
		if ((numSectors < 2 || x > 5 || (x != (int) x)) && numSectors != 36)
			throw new ESTermQueryException("numSectors", numSectors.toString());
	}

	private void checkValidPartitionNumber(Integer partitionNumber) {

		if (partitionNumber > PARTITION_NUMBER_LIMIT || partitionNumber < 1)
			throw new ESTermQueryException("partitionNumber", partitionNumber.toString());
	}
}