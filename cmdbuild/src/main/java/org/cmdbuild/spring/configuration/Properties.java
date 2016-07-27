package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.SOAP;

import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.config.EmailProperties;
import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.config.GisProperties;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.services.soap.security.SoapConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {

	@Bean
	public AuthProperties authConf() {
		return AuthProperties.getInstance();
	}

	@Bean
	public CmdbuildConfiguration cmdbuildProperties() {
		return CmdbuildProperties.getInstance();
	}

	@Bean
	public DatabaseConfiguration databaseProperties() {
		return DatabaseProperties.getInstance();
	}

	@Bean
	public DmsProperties dmsProperties() {
		return DmsProperties.getInstance();
	}

	@Bean
	public EmailConfiguration emailProperties() {
		return EmailProperties.getInstance();
	}

	@Bean
	public GisConfiguration gisProperties() {
		return GisProperties.getInstance();
	}

	@Bean
	public GraphProperties graphProperties() {
		return GraphProperties.getInstance();
	}

	@Bean
	@Qualifier(SOAP)
	public SoapConfiguration soapConfiguration() {
		return new SoapConfiguration(authConf());
	}

	@Bean
	public WorkflowConfiguration workflowProperties() {
		return WorkflowProperties.getInstance();
	}

}
