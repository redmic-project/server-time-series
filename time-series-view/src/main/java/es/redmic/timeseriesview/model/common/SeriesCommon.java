package es.redmic.timeseriesview.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.redmic.models.es.common.model.BaseTimeDataAbstractES;

public abstract class SeriesCommon extends BaseTimeDataAbstractES {

	private String activityId;

	private Character qFlag = '0';

	private Character vFlag = 'U';

	private String remark;

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	@JsonProperty(value = "qFlag")
	public Character getQFlag() {
		return qFlag;
	}

	public void setQFlag(Character qFlag) {
		this.qFlag = qFlag;
	}

	@JsonProperty(value = "vFlag")
	public Character getVFlag() {
		return vFlag;
	}

	public void setVFlag(Character vFlag) {
		this.vFlag = vFlag;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
