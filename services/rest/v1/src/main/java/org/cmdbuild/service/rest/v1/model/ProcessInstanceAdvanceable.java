package org.cmdbuild.service.rest.v1.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.service.rest.v1.model.adapter.ProcessInstanceAdvanceableAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(ProcessInstanceAdvanceableAdapter.class)
public class ProcessInstanceAdvanceable extends ProcessInstance {

	private String activity;
	private boolean advance;

	ProcessInstanceAdvanceable() {
		// package visibility
	}

	public String getActivity() {
		return activity;
	}

	void setActivity(final String activityId) {
		this.activity = activityId;
	}

	public boolean isAdvance() {
		return advance;
	}

	void setAdvance(final boolean advance) {
		this.advance = advance;
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
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(activity) //
				.append(advance) //
				.toHashCode();
	}

}
