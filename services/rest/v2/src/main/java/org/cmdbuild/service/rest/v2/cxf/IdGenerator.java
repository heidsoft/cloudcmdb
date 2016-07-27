package org.cmdbuild.service.rest.v2.cxf;

public interface IdGenerator {

	Long generate();

	boolean isGenerated(Long id);

}
