package org.cmdbuild.dms.cmis;

import org.cmdbuild.dms.DmsConfiguration;

public interface CmisDmsConfiguration extends DmsConfiguration {

	String getCmisUrl();

	String getCmisUser();

	String getCmisPassword();

	String getCmisPath();

	String getCmisModelType();

}
