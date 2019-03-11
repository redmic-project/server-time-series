package es.redmic.timeseriesview.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator.KeyedFilter;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.timeseries.repository.RTimeSeriesESRepository;
import es.redmic.exception.common.ExceptionType;
import es.redmic.exception.common.InternalException;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.data.common.model.DataSearchWrapper;
import es.redmic.timeseriesview.common.query.SeriesQueryUtils;
import es.redmic.timeseriesview.dto.windrose.DatesByDirectionListDTO;
import es.redmic.timeseriesview.dto.windrose.LimitsDTO;
import es.redmic.timeseriesview.dto.windrose.WindroseDataDTO;
import es.redmic.timeseriesview.dto.windrose.WindroseSectorDTO;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.viewlib.config.MapperScanBeanItfc;

@Repository
public class WindRoseESRepository extends RTimeSeriesESRepository<TimeSeries, DataQueryDTO> {

	public WindRoseESRepository() {
		super();
	}

	@Autowired
	protected MapperScanBeanItfc mapper;

	@SuppressWarnings({ "serial", "unchecked" })
	public Map<String, Object> getStatAggs(DataQueryDTO query, Integer speedDataDefinition) {

		// Crea query para obtener max, min y count de velocidades
		query.setSize(0);
		query.getTerms().put("dataDefinition", new ArrayList<Integer>() {
			{
				add(speedDataDefinition);
			}
		});
		// añade identificador para que se cree la agregación correspondiente
		query.addAgg(new AggsPropertiesDTO("stats", "speed"));

		DataSearchWrapper<TimeSeries> responseStats = (DataSearchWrapper<TimeSeries>) find(query);

		if (responseStats.getAggregations() == null || responseStats.getAggregations().getAttributes() == null) {
			LOGGER.debug("No es posible realizar los cálculos");
			throw new InternalException(ExceptionType.INTERNAL_EXCEPTION);
		}

		return (Map<String, Object>) responseStats.getAggregations().getAttributes().get("stats#value");
	}

	@SuppressWarnings({ "serial", "unchecked" })
	public DatesByDirectionListDTO getDatesByDirectionAggs(DataQueryDTO query, Integer numSectors,
			Integer directionDataDefinition) {

		// añade identificador para que se cree la agregación de los rangos de
		// los sectores (Los sectores se calculan en grados a partir del número de
		// sectores enviado por el cliente)
		query.getAggs().clear();
		query.addAgg(new AggsPropertiesDTO("sectors", "sectors"));
		query.getTerms().put("numSectors", numSectors);

		query.getTerms().put("dataDefinition", new ArrayList<Integer>() {
			{
				add(directionDataDefinition);
			}
		});

		DataSearchWrapper<TimeSeries> responseSectors = (DataSearchWrapper<TimeSeries>) find(query);

		if (responseSectors == null || responseSectors.getAggregations() == null) {
			LOGGER.debug("No es posible realizar los cálculos");
			throw new InternalException(ExceptionType.INTERNAL_EXCEPTION);
		}

		return mapper.getMapperFacade().convert(responseSectors.getAggregations(), DatesByDirectionListDTO.class, null,
				null);
	}

	@SuppressWarnings({ "serial" })
	public WindroseDataDTO getWindroseData(DataQueryDTO query, DatesByDirectionListDTO datesByDirectionListDTO,
			Integer speedDataDefinition, Map<String, Object> stats, Integer partitionNumber) {

		// @formatter:off
		
		Double min = (Double) stats.get("min"),
				max = (Double) stats.get("max");
		
		// @formatter:on

		Integer count = (Integer) stats.get("count");

		WindroseDataDTO windroseDataDTO = new WindroseDataDTO(min, max, partitionNumber);

		query.getAggs().clear();
		query.addAgg(new AggsPropertiesDTO("windrose"));

		query.getTerms().put("limits", windroseDataDTO.getLimits());

		query.getTerms().put("dataDefinition", new ArrayList<Integer>() {
			{
				add(speedDataDefinition);
			}
		});

		List<SearchSourceBuilder> searchs = new ArrayList<SearchSourceBuilder>();

		// para cada uno de los sectores obtener queryBuilder enviando en terms
		// las fechas.
		for (int i = 0; i < datesByDirectionListDTO.size(); i++) {
			// TODO: optimizar evitando hacer query cuando
			// datesByDirectionListDTO.get(i).getDates() == 0
			query.getTerms().put("dates", datesByDirectionListDTO.get(i).getDates());
			searchs.add(searchRequestBuilder(query));
		}
		List<DataSearchWrapper<?>> results = multiFind(searchs);

		// para cada respuesta obtenemos los resultados de la agregación
		for (int i = 0; i < results.size(); i++) {
			WindroseSectorDTO dataSector = mapper.getMapperFacade().convert(results.get(i).getAggregations(),
					WindroseSectorDTO.class, null, null);
			dataSector.calculateValue(count);
			windroseDataDTO.addSectorData(dataSector);
		}
		return windroseDataDTO;
	}

	/**
	 * Sobrescribe el método original para añadir las agregaciones específicas para
	 * windrose
	 */
	@Override
	protected SearchSourceBuilder searchRequestBuilder(DataQueryDTO queryDTO, QueryBuilder serviceQuery) {
		SearchSourceBuilder searchSourceBuilder = super.searchRequestBuilder(queryDTO, serviceQuery);

		List<BaseAggregationBuilder> aggs = getAggs(queryDTO);

		if (aggs != null) {
			for (BaseAggregationBuilder term : aggs) {
				searchSourceBuilder.aggregation((AggregationBuilder) term);
			}
		}

		return searchSourceBuilder;
	}

	/**
	 * Método que sustituye a getAggs original ya que necesita el objeto query
	 * completo para fabricar las agregaciones
	 * 
	 */
	protected List<BaseAggregationBuilder> getAggs(DataQueryDTO queryDTO) {

		List<AggsPropertiesDTO> aggs = queryDTO.getAggs();

		if (queryDTO.getInterval() == null && (aggs == null || aggs.size() == 0))
			return null;

		List<BaseAggregationBuilder> aggsBuilder = new ArrayList<BaseAggregationBuilder>();

		switch (aggs.get(0).getField()) {
		case "stats":
			// Primera agregación: estadísticas para obtener el min, max y count de la
			// velocidad
			aggsBuilder.add(AggregationBuilders.stats(defaultField).field(defaultField));
			break;
		case "sectors":
			// Segunda agregación: Fechas agregadas por sectores que corresponden con la
			// dirección.
			aggsBuilder.add(getSectorAggregationBuilder(queryDTO));
			break;
		case "windrose":
			// Tercera agregación: Velocidad agregada por las fechas que corresponden con
			// cada sector
			aggsBuilder.add(getWindroseAggregationBuilder(queryDTO));
			break;
		default:
			break;
		}

		return aggsBuilder;
	}

	/**
	 * Sobrescribe método original para evitar que cree las agregaciones comunes, ya
	 * que se usarán las específicas para windrose
	 */
	@Override
	protected List<BaseAggregationBuilder> getAggs(List<AggsPropertiesDTO> aggs) {
		return null;
	}

	/**
	 * Obtener fechas de datos agrupadas por sectores (Query 2 de windrose)
	 */
	private FiltersAggregationBuilder getSectorAggregationBuilder(DataQueryDTO elasticQueryDTO) {

		Integer numSectors = (Integer) elasticQueryDTO.getTerms().get("numSectors");

		// @formatter:off

		Double sectorLength = 360.0 / numSectors,
				rotationOffset = sectorLength / 2;

		// @formatter:on

		KeyedFilter[] filters = new KeyedFilter[numSectors];

		double limit = 0;
		for (int i = 0; i < numSectors; i++) {

			if (limit == 0) {
				BoolQueryBuilder filter = QueryBuilders.boolQuery();
				filter.should().add(0, QueryBuilders.rangeQuery(defaultField).gte(360 - rotationOffset).lte(360));
				filter.should().add(1, QueryBuilders.rangeQuery(defaultField).gte(0).lt(sectorLength - rotationOffset));
				filters[i] = new KeyedFilter(i + "", filter); // se guarda como índice el orden en la representación
			} else {
				filters[i] = new KeyedFilter(i + "",
						QueryBuilders.rangeQuery(defaultField).gte(limit - rotationOffset).lt(limit + rotationOffset));
			}
			limit += sectorLength;
		}

		return AggregationBuilders.filters("direction_ranges", filters)
				.subAggregation(AggregationBuilders.terms("dates").field(dateTimeField).size(MAX_SIZE));
	}

	/**
	 * Obtener count de velocidad agrupadas por rangos de precisión (Query final por
	 * cada sector)
	 * 
	 */
	@SuppressWarnings("unchecked")
	private RangeAggregationBuilder getWindroseAggregationBuilder(DataQueryDTO elasticQueryDTO) {

		List<LimitsDTO> limits = (List<LimitsDTO>) elasticQueryDTO.getTerms().get("limits");

		RangeAggregationBuilder range = AggregationBuilders.range("value_ranges").field(defaultField);

		for (int i = 0; i < limits.size(); i++) {
			if (i == (limits.size() - 1)) // Rango abierto para el último
				range.addUnboundedFrom(limits.get(i).getMin());
			else
				range.addRange(limits.get(i).getMin(), limits.get(i).getMax());
		}

		return range.subAggregation(AggregationBuilders.count("count").field(defaultField));
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryBuilder getTermQuery(Map<String, Object> terms, BoolQueryBuilder query) {

		if (terms.containsKey("dataDefinition")) {
			List<Integer> ids = (List<Integer>) (List<?>) terms.get("dataDefinition");
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("dataDefinition", ids)));
		}

		if (terms.containsKey("dates")) {
			List<String> dates = (List<String>) (List<?>) terms.get("dates");
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("date", dates)));
		}
		return super.getTermQuery(terms, query);
	}

	@Override
	protected BoolQueryBuilder getQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery, QueryBuilder partialQuery) {
		return SeriesQueryUtils.getQuery(queryDTO, getInternalQuery(), partialQuery);
	}
}
