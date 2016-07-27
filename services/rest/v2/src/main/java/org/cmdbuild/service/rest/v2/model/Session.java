package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.AVAILABLE_ROLES;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PASSWORD;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ROLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.USERNAME;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class Session extends ModelWithStringId {

	private String username;
	private String password;
	private String role;
	private Collection<String> availableRoles;

	Session() {
		// package visibility
	}

	@XmlElement(name = USERNAME)
	public String getUsername() {
		return username;
	}

	void setUsername(final String username) {
		this.username = username;
	}

	@XmlElement(name = PASSWORD)
	public String getPassword() {
		return password;
	}

	void setPassword(final String password) {
		this.password = password;
	}

	@XmlElement(name = ROLE)
	public String getRole() {
		return role;
	}

	void setRole(final String role) {
		this.role = role;
	}

	@XmlElement(name = AVAILABLE_ROLES)
	public Collection<String> getAvailableRoles() {
		return availableRoles;
	}

	void setAvailableRoles(final Collection<String> availableRoles) {
		this.availableRoles = availableRoles;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Session)) {
			return false;
		}
		final Session other = Session.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.username, other.username) //
				.append(this.password, other.password) //
				.append(this.role, other.role) //
				.append(this.availableRoles, other.availableRoles) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.username) //
				.append(this.password) //
				.append(this.role) //
				.append(this.availableRoles) //
				.toHashCode();
	}

}
