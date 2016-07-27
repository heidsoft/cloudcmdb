package org.cmdbuild.service.rest.v1.cxf.configuration;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.FirstPresent.firstPresent;

import org.cmdbuild.service.rest.v1.cxf.security.FirstPresent;
import org.cmdbuild.service.rest.v1.cxf.security.HeaderTokenExtractor;
import org.cmdbuild.service.rest.v1.cxf.security.QueryStringTokenExtractor;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityV1 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV1 helper;

	@Bean
	public TokenHandler v1_tokenHandler() {
		return new TokenHandler(v1_tokenExtractor(), helper.sessionLogic());
	}

	@Bean
	protected FirstPresent v1_tokenExtractor() {
		return firstPresent(asList(v1_header(), v1_queryString()));
	}

	@Bean
	protected HeaderTokenExtractor v1_header() {
		return new HeaderTokenExtractor();
	}

	@Bean
	protected QueryStringTokenExtractor v1_queryString() {
		return new QueryStringTokenExtractor();
	}

}
