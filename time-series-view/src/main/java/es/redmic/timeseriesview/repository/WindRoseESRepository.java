package es.redmic.timeseriesview.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BaseAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import es.redmic.elasticsearchlib.timeseries.repository.RTimeSeriesESRepository;
import es.redmic.models.es.common.query.dto.AggsPropertiesDTO;
import es.redmic.models.es.common.query.dto.DataQueryDTO;
import es.redmic.models.es.data.common.model.DataSearchWrapper;
import es.redmic.timeseriesview.common.query.SeriesQueryUtils;
import es.redmic.timeseriesview.dto.windrose.WindRoseDataDTO;
import es.redmic.timeseriesview.model.timeseries.TimeSeries;
import es.redmic.viewlib.config.MapperScanBeanItfc;
import ma.glasnost.orika.MappingContext;

@Repository
public class WindRoseESRepository extends RTimeSeriesESRepository<TimeSeries, DataQueryDTO> {

	public WindRoseESRepository() {
		super();
	}

	@Autowired
	protected MapperScanBeanItfc mapper;

	/**
	 * Método que realiza la consulta y manda a convertir el resultado al dto
	 * esperado
	 */

	@SuppressWarnings("unchecked")
	public WindRoseDataDTO getWindRoseData(DataQueryDTO query, Integer numSectors, Integer partitionNumber) {

		Map<Object, Object> globalProperties = new HashMap<Object, Object>();
		globalProperties.put("numSectors", numSectors);
		globalProperties.put("partitionNumber", partitionNumber);

		return mapper.getMapperFacade().convert(((DataSearchWrapper<TimeSeries>) find(query)).getAggregations(),
				WindRoseDataDTO.class, null, new MappingContext(globalProperties));
	}

	/**
	 * Sobrescribe método original para evitar que cree las agregaciones comunes, ya
	 * que se usarán las específicas para windrose
	 */
	@Override
	protected List<BaseAggregationBuilder> getAggs(List<AggsPropertiesDTO> aggs) {

		String listSplitRegex = "\\s*,\\s*";

		List<BaseAggregationBuilder> aggBuilders = new ArrayList<BaseAggregationBuilder>();

		List<String> speedDataDefinitions = Arrays.asList(aggs.get(0).getTerm().split(listSplitRegex)),
				directionDataDefinitions = Arrays.asList(aggs.get(1).getTerm().split(listSplitRegex));

		TermsQueryBuilder speedTermQuery = QueryBuilders.termsQuery("dataDefinition", speedDataDefinitions);

		aggBuilders.add(PipelineAggregatorBuilders.statsBucket("stats-buckets",
				"avg_values_by_interval>speedDataDefinitionFilter>avg_speed"));

		// Se consulta el número de elementos total para las estadísticas
		aggBuilders.add(AggregationBuilders.filter("dataDefinitionFilter", speedTermQuery)
				.subAggregation(AggregationBuilders.count("speed_count").field(defaultField)));

		// Desde el cliente se envía en ms y aquí se pasa a seg
		int timeInterval = (int) (Long.parseLong(aggs.get(2).getTerm()) / 1000);

		// Se consultan direcciones y velocidades agregadas por el timeInterval definido

		AggregationBuilder directionValues = AggregationBuilders
				.filter("directionDataDefinitionFilter",
						QueryBuilders.termsQuery("dataDefinition", directionDataDefinitions))
				.subAggregation(AggregationBuilders.avg("avg_direction").field(defaultField));

		AggregationBuilder speedValues = AggregationBuilders.filter("speedDataDefinitionFilter", speedTermQuery)
				.subAggregation(AggregationBuilders.avg("avg_speed").field(defaultField));

		aggBuilders.add(AggregationBuilders.dateHistogram("avg_values_by_interval").field(dateTimeField).minDocCount(1)
				.dateHistogramInterval(DateHistogramInterval.seconds(timeInterval)).subAggregation(directionValues)
				.subAggregation(speedValues));

		return aggBuilders;
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryBuilder getTermQuery(Map<String, Object> terms, BoolQueryBuilder query) {

		if (terms.containsKey("dataDefinition")) {
			List<Integer> ids = (List<Integer>) terms.get("dataDefinition");
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("dataDefinition", ids)));
		}

		if (terms.containsKey("dates")) {
			List<String> dates = (List<String>) terms.get("dates");
			query.must(QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery("date", dates)));
		}
		return super.getTermQuery(terms, query);
	}

	@Override
	protected BoolQueryBuilder getQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery, QueryBuilder partialQuery) {
		return SeriesQueryUtils.getQuery(queryDTO, getInternalQuery(), partialQuery);
	}
}
