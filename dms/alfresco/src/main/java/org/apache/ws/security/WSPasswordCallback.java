
package org.apache.ws.security;

import javax.security.auth.callback.Callback;

import org.w3c.dom.Element;

/**
 * Custom implementation of {@link Callback} needed for handle Alfresco
 * implementation.
 */
public class WSPasswordCallback implements Callback {

	private org.apache.wss4j.common.ext.WSPasswordCallback delegate;

	public WSPasswordCallback(org.apache.wss4j.common.ext.WSPasswordCallback delegate) {
		this.delegate = delegate;
	}

	public String getIdentifier() {
		return delegate.getIdentifier();
	}

	public void setIdentifier(String ident) {
		delegate.setIdentifier(ident);
	}

	public String getPassword() {
		return delegate.getPassword();
	}

	public void setPassword(String passwd) {
		delegate.setPassword(passwd);
	}

	public void setKey(byte[] key) {
		delegate.setKey(key);
	}

	public byte[] getKey() {
		return delegate.getKey();
	}

	public int getUsage() {
		return delegate.getUsage();
	}

	public String getType() {
		return delegate.getType();
	}

	public Element getCustomToken() {
		return delegate.getCustomToken();
	}

	public void setCustomToken(Element customToken) {
		delegate.setCustomToken(customToken);
	}

}
