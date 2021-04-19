package es.redmic.timeseriesview.common.mapper;

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

import es.redmic.models.es.common.dto.AggregationsDTO;
import es.redmic.models.es.geojson.common.model.Aggregations;
import es.redmic.timeserieslib.dto.series.SeriesCommonDTO;
import es.redmic.timeseriesview.model.common.SeriesCommon;
import es.redmic.viewlib.common.mapper.es2dto.BaseESMapper;

public abstract class SeriesESMapper<TDTO extends SeriesCommonDTO, TModel extends SeriesCommon>
	extends BaseESMapper<TDTO, TModel> {

	public abstract AggregationsDTO map(Aggregations aggregations);
}
