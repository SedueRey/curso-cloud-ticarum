package es.um.atica.vuelos.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")

@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/cucumber-reports/report.html"
)
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "es.um.atica.vuelos.cucumber,es.um.atica.vuelos.cucumber.steps" 
)
@ConfigurationParameter(
	    key = Constants.FEATURES_PROPERTY_NAME,
	    value = "classpath:features" )

public class CucumberTestSuite {}