package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import javax.sql.DataSource;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.logic.DefaultGISLogic;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.services.gis.GeoFeatureStore;
import org.cmdbuild.services.gis.GisDatabaseService;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Gis {

	@Autowired
	private Data data;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private GisConfiguration gisConfiguration;

	@Bean
	protected GeoServerService geoServerService() {
		return new GeoServerService(gisConfiguration);
	}

	@Bean
	protected GeoFeatureStore geoFeatureStore() {
		return new GeoFeatureStore(dataSource, gisDatabaseService());
	}

	@Bean
	protected GisDatabaseService gisDatabaseService() {
		return new GisDatabaseService(dataSource);
	}

	@Bean
	@Scope(PROTOTYPE)
	public GISLogic gisLogic() {
		return new DefaultGISLogic( //
				data.systemDataView(), //
				geoFeatureStore(), //
				gisConfiguration, //
				geoServerService());
	}

}
