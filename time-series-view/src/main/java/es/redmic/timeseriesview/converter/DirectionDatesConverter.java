package es.redmic.timeseriesview.converter;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.redmic.elasticsearchlib.common.utils.ElasticSearchUtils;
import es.redmic.models.es.geojson.common.model.Aggregations;
import es.redmic.timeseriesview.dto.windrose.DatesByDirectionDTO;
import es.redmic.timeseriesview.dto.windrose.DatesByDirectionListDTO;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * Convierte el resultado de la agregación de fechas por sectores, en un objeto
 * para usar en la query de windrose
 */

@Component
public class DirectionDatesConverter extends CustomConverter<Aggregations, DatesByDirectionListDTO> {

	@SuppressWarnings("unchecked")
	@Override
	public DatesByDirectionListDTO convert(Aggregations source,
			Type<? extends DatesByDirectionListDTO> destinationType) {

		DatesByDirectionListDTO result = new DatesByDirectionListDTO();

		Map<String, Object> aggregations = source.getAttributes();

		if (aggregations == null || aggregations.size() == 0)
			return result;

		// @formatter:off
		// Extrae los datos hasta el resultado de cada filtro 
		Map<String, Object> directions = (Map<String, Object>) aggregations.get("filters#direction_ranges"),
				sectors = (Map<String, Object>) directions.get("buckets");

		// @formatter:on

		// Para el resultado de cada filtro = sector
		for (int i = 0; i < sectors.size(); i++) {
			result.add(getDatesByDirection((Map<String, Object>) sectors.get(i + ""))); // se usa como índice el orden
																						// en la representación
		}
		return result;
	}

	/*
	 * Obtiene las fechas agregadas en el sector especificado
	 */
	private DatesByDirectionDTO getDatesByDirection(Map<String, Object> sector) {

		DatesByDirectionDTO datesByDirectionDTO = new DatesByDirectionDTO();

		// Extrae los buckets
		List<Map<String, Object>> dates = ElasticSearchUtils.getBucketsFromAggregations(sector);

		// Guarda cada una de las fechas obtenidas
		for (int j = 0; j < dates.size(); j++) {
			datesByDirectionDTO.getDates().add(dates.get(j).get("key_as_string").toString());
		}

		return datesByDirectionDTO;
	}
}
