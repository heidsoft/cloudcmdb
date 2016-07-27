package org.cmdbuild.service.rest.v1.model;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ForwardingMap;

@XmlRootElement
public class Values extends ForwardingMap<String, Object> implements Model {

	private final Map<String, Object> delegate;

	Values() {
		this.delegate = newHashMap();
	}

	@Override
	protected Map<String, Object> delegate() {
		return delegate;
	}

}
