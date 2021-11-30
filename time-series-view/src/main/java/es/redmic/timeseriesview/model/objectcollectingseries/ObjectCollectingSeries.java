package es.redmic.timeseriesview.model.objectcollectingseries;

/*-
 * #%L
 * Models
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

import java.util.List;

import es.redmic.models.es.common.model.DomainES;
import es.redmic.models.es.geodata.ancillarydata.model.Analytic;
import es.redmic.models.es.geodata.ancillarydata.model.Metric;
import es.redmic.models.es.geodata.genomics.model.Molecular;
import es.redmic.models.es.geodata.misc.model.Element;
import es.redmic.models.es.geodata.qualifiers.model.Attribute;
import es.redmic.models.es.geodata.samples.model.Sample;
import es.redmic.models.es.maintenance.objects.model.ObjectClassification;
import es.redmic.timeseriesview.model.common.SeriesCommon;

public class ObjectCollectingSeries extends SeriesCommon {

	private Double radius;

	private String collectorName;

	private String collectionRegNo;

	private DomainES confidence;
	private DomainES localityConfidence;

	private List<ObjectClassification> object;

	private List<Analytic> analytics;

	private List<Attribute> attributes;

	private List<Metric> metrics;

	private List<Molecular> molecular;

	private List<Element> elements;

	private Sample sample;

	public Double getRadius() {
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
	}

	public String getCollectorName() {
		return collectorName;
	}

	public void setCollectorName(String collectorName) {
		this.collectorName = collectorName;
	}

	public String getCollectionRegNo() {
		return collectionRegNo;
	}

	public void setCollectionRegNo(String collectionRegNo) {
		this.collectionRegNo = collectionRegNo;
	}

	public DomainES getConfidence() {
		return confidence;
	}

	public void setConfidence(DomainES confidence) {
		this.confidence = confidence;
	}

	public DomainES getLocalityConfidence() {
		return localityConfidence;
	}

	public void setLocalityConfidence(DomainES localityConfidence) {
		this.localityConfidence = localityConfidence;
	}

	public List<ObjectClassification> getObject() {
		return object;
	}

	public void setObject(List<ObjectClassification> object) {
		this.object = object;
	}

	public List<Analytic> getAnalytics() {
		return analytics;
	}

	public void setAnalytics(List<Analytic> analytics) {
		this.analytics = analytics;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public List<Molecular> getMolecular() {
		return molecular;
	}

	public void setMolecular(List<Molecular> molecular) {
		this.molecular = molecular;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
}
