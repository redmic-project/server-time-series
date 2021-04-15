package es.redmic.timeserieslib.dto.series;

/*-
 * #%L
 * Time series library
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

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.redmic.brokerlib.avro.common.CommonDTO;

public abstract class SeriesCommonDTO extends CommonDTO {

	// Notas
	@Size(min = 0, max = 1500)
	private String remark;

	private Character qFlag = '0';

	private Character vFlag = 'U';

	protected SeriesCommonDTO() {
		super();
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@JsonProperty(value = "qFlag")
	public Character getQFlag() {
		return qFlag;
	}

	@JsonProperty(value = "qFlag")
	public void setQFlag(Character qFlag) {

		if (qFlag != null)
			this.qFlag = qFlag;
	}

	@JsonIgnore
	public void setqFlag(Character qFlag) {
		this.qFlag = qFlag;
	}

	@JsonProperty(value = "vFlag")
	public Character getVFlag() {
		return vFlag;
	}

	@JsonProperty(value = "vFlag")
	public void setVFlag(Character vFlag) {

		if (vFlag != null)
			this.vFlag = vFlag;
	}

	@JsonIgnore
	public void setvFlag(Character vFlag) {
		this.vFlag = vFlag;
	}

	// TODO: Añadir @JsonSchemaIgnore private MetaDataDTO _meta = new MetaDataDTO();

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		return super.equals(obj);
	}
}
