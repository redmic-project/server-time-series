package es.redmic.timeseriesview.common.query;

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
import java.util.HashSet;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import es.redmic.elasticsearchlib.common.query.DataQueryUtils;
import es.redmic.exception.elasticsearch.ESHistogramIntervalQueryException;
import es.redmic.models.es.common.query.dto.DataQueryDTO;

public abstract class SeriesQueryUtils extends DataQueryUtils {

	// @FORMATTER:OFF

	public final static BoolQueryBuilder INTERNAL_QUERY = null;

	public final static String DEFAULT_FIELD = "value", DATETIME_FIELD = "date", PARENT = "geodata",
			GRANDPARENT = "activity";

	// @FORMATTER:ON

	public static BoolQueryBuilder getQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery,
			QueryBuilder partialQuery) {
		return getSeriesQuery(queryDTO, internalQuery, partialQuery);
	}

	protected static BoolQueryBuilder getSeriesQuery(DataQueryDTO queryDTO, QueryBuilder internalQuery,
			QueryBuilder partialQuery) {

		BoolQueryBuilder query = getOrInitializeBaseQuery(getBaseQuery(queryDTO, internalQuery, partialQuery));

		addMustTermIfExist(query, getActivityIdQuery(queryDTO.getActivityId()));

		addMustTermIfExist(query, getFlagQuery(queryDTO.getQFlags(), QFLAG_PROPERTY));
		addMustTermIfExist(query, getFlagQuery(queryDTO.getVFlags(), VFLAG_PROPERTY));
		addMustTermIfExist(query, getDateLimitsQuery(queryDTO.getDateLimits(), DATETIME_FIELD));
		addMustTermIfExist(query, getValueQuery(queryDTO.getValue(), VALUE_PROPERTY));
		addMustTermIfExist(query, getZQuery(Z_PROPERTY, SEARCH_BY_Z_RANGE_SCRIPT, queryDTO.getZ()));

		return getResultQuery(query);
	}

	@SuppressWarnings("serial")
	public static BoolQueryBuilder getItemsQuery(String id, String parentId, String grandparentId,
			List<Long> accessibilityIds) {

		ArrayList<String> ids = new ArrayList<String>() {
			{
				add(id);
			}
		};
		return getItemsQuery(ids, parentId, grandparentId, accessibilityIds);
	}

	public static BoolQueryBuilder getItemsQuery(List<String> ids, String parentId, String grandparentId,
			List<Long> accessibilityIds) {

		BoolQueryBuilder query = QueryBuilders.boolQuery();

		if (accessibilityIds != null && accessibilityIds.size() > 0 && parentId != null && grandparentId != null)
			query.must(getQueryOnParentAndGrandparent(parentId, grandparentId, accessibilityIds));

		else if (accessibilityIds != null && accessibilityIds.size() > 0 && parentId == null && grandparentId == null)
			query.must(getAccessibilityQueryOnGrandparent(accessibilityIds));

		else if (accessibilityIds == null && parentId != null && grandparentId != null)
			query.must(getQueryByParentAndGrandparent(parentId, grandparentId));

		query.must(QueryBuilders.idsQuery().addIds(ids.toArray(new String[ids.size()])));

		return query;
	}

	public static DateHistogramInterval getInterval(String interval) {

		if (!interval.matches("^\\d+[smhdwMqy]$"))
			throw new ESHistogramIntervalQueryException(interval);
		return new DateHistogramInterval(interval);
	}

	public static QueryBuilder getHierarchicalQuery(DataQueryDTO queryDTO, String parentId, String grandparentId) {

		List<Long> accessibilityIds = queryDTO.getAccessibilityIds();

		if (parentId == null && grandparentId == null && (accessibilityIds == null || accessibilityIds.size() == 0))
			return null;

		if (parentId != null && grandparentId != null && (accessibilityIds == null || accessibilityIds.size() == 0))
			return getQueryByParentAndGrandparent(parentId, grandparentId);

		if (parentId == null || grandparentId == null && (accessibilityIds != null && accessibilityIds.size() > 0))
			return getAccessibilityQueryOnGrandparent(accessibilityIds);

		return getQueryOnParentAndGrandparent(parentId, grandparentId, accessibilityIds);
	}

	public static QueryBuilder getQueryByParentAndGrandparent(String parentId, String grandparentId) {
		return JoinQueryBuilders.hasParentQuery(PARENT,
				QueryBuilders.boolQuery()
						.must(JoinQueryBuilders.hasParentQuery(GRANDPARENT,
								QueryBuilders.boolQuery().must(QueryBuilders.termQuery("id", grandparentId)), true))
						.must(QueryBuilders.termQuery("_id", parentId)),
				true);
	}

	public static QueryBuilder getQueryOnParentAndGrandparent(String parentId, String grandparentId,
			List<Long> accessibilityIds) {

		return JoinQueryBuilders.hasParentQuery(PARENT,
				QueryBuilders.boolQuery()
						.must(JoinQueryBuilders.hasParentQuery(GRANDPARENT,
								QueryBuilders.boolQuery().must(QueryBuilders.termQuery("id", grandparentId))
										.must(getAccessibilityQuery(accessibilityIds)),
								true))
						.must(QueryBuilders.termQuery("_id", parentId)),
				true);
	}

	public static QueryBuilder getAccessibilityQueryOnGrandparent(List<Long> accessibilityIds) {

		return JoinQueryBuilders.hasParentQuery(PARENT,
				JoinQueryBuilders.hasParentQuery(GRANDPARENT, getAccessibilityQuery(accessibilityIds), true), true);
	}

	@SuppressWarnings("serial")
	public static HashSet<String> getFieldsExcludedOnQuery() {

		return new HashSet<String>() {
			{
				add(ZRANGE_QUERY_FIELD);
			}
		};
	}
}
