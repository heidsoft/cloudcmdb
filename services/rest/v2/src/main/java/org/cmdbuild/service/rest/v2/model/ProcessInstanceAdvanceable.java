package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVITY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ADVANCE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.WIDGETS;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v2.model.adapter.ProcessInstanceAdvanceableAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(ProcessInstanceAdvanceableAdapter.class)
public class ProcessInstanceAdvanceable extends ProcessInstance {

	private String activity;
	private boolean advance;
	private Collection<Widget> widgets;

	ProcessInstanceAdvanceable() {
		// package visibility
	}

	@XmlAttribute(name = ACTIVITY)
	public String getActivity() {
		return activity;
	}

	void setActivity(final String activityId) {
		this.activity = activityId;
	}

	@XmlAttribute(name = ADVANCE)
	public boolean isAdvance() {
		return advance;
	}

	void setAdvance(final boolean advance) {
		this.advance = advance;
	}

	@XmlElement(name = WIDGETS)
	public Collection<Widget> getWidgets() {
		return widgets;
	}

	void setWidgets(final Collection<Widget> widgets) {
		this.widgets = widgets;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessInstanceAdvanceable)) {
			return false;
		}

		final ProcessInstanceAdvanceable other = ProcessInstanceAdvanceable.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.append(this.activity, other.activity) //
				.append(this.advance, other.advance) //
				.append(this.widgets, other.widgets) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(activity) //
				.append(advance) //
				.append(widgets) //
				.toHashCode();
	}

}
