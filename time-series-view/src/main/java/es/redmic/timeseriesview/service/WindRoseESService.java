package es.redmic.timeseriesview.service;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.redmic.exception.elasticsearch.ESTermQueryException;
import es.redmic.models.es.common.dto.ElasticSearchDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.timeseriesview.dto.windrose.DatesByDirectionListDTO;
import es.redmic.timeseriesview.dto.windrose.WindroseDataDTO;
import es.redmic.timeseriesview.repository.WindRoseESRepository;
import es.redmic.viewlib.config.MapperScanBeanItfc;

@Service
public class WindRoseESService {

	private final static Integer PARTITION_NUMBER_LIMIT = 20;

	private WindRoseESRepository repository;

	@Autowired
	protected MapperScanBeanItfc mapper;

	private static final String GOOD_QFLAG = "1";

	@Autowired
	public WindRoseESService(WindRoseESRepository repository) {
		this.repository = repository;
	}

	@SuppressWarnings({ "unchecked" })
	public ElasticSearchDTO getWindRoseData(DataQueryDTO query, String activityId) {

		// TODO: comprobar que la actividad corresponde con la buscada

		// Se obliga a que los datos sean buenos
		if (query.getQFlags() != null)
			query.getQFlags().clear();

		query.setQFlags(Arrays.asList(GOOD_QFLAG));

		// Obtiene datos de la query

		Map<String, Object> dataDefinitionMap = (Map<String, Object>) query.getTerms().get("dataDefinition");

		Integer numSectors = (Integer) query.getTerms().get("numSectors"),
				partitionNumber = (Integer) query.getTerms().get("numSplits"),
				speedDataDefinition = (Integer) dataDefinitionMap.get("speed"),
				directionDataDefinition = (Integer) dataDefinitionMap.get("direction");

		checkValidNumSectors(numSectors);
		checkValidPartitionNumber(partitionNumber);

		Map<String, Object> stats = repository.getStatAggs(query, speedDataDefinition);

		// Obtener las fechas de los registros de cada uno de los sectores.
		DatesByDirectionListDTO datesByDirectionListDTO = repository.getDatesByDirectionAggs(query, numSectors,
				directionDataDefinition);

		// Hacer query para obtener los datos en formato windrose
		WindroseDataDTO windroseDataDTO = repository.getWindroseData(query, datesByDirectionListDTO,
				speedDataDefinition, stats, partitionNumber);

		return new ElasticSearchDTO(windroseDataDTO, windroseDataDTO.getData().size());
	}

	private void checkValidNumSectors(Integer numSectors) {
		// Comprueba que numSectors sea un número válido. An = 2^(n-1)

		double x = (Math.log(numSectors) / Math.log(2));
		if (numSectors < 2 || x > 5 || (x != (int) x))
			throw new ESTermQueryException("numSectors", numSectors.toString());
	}

	private void checkValidPartitionNumber(Integer partitionNumber) {

		if (partitionNumber > PARTITION_NUMBER_LIMIT || partitionNumber < 1)
			throw new ESTermQueryException("partitionNumber", partitionNumber.toString());
	}
}