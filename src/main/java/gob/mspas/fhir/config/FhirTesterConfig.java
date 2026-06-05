package gob.mspas.fhir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;

//@formatter:off
/**
 * This spring config file configures the web testing module. It serves two
 * purposes:
 * 1. It imports FhirTesterMvcConfig, which is the spring config for the
 *    tester itself
 * 2. It tells the tester which server(s) to talk to, via the testerConfig()
 *    method below
 */
@Configuration
@Import(FhirTesterMvcConfig.class)
public class FhirTesterConfig {

	/**
	 * This bean tells the testing webpage which servers it should configure itself
	 * to communicate with. In this example we configure it to talk to the local
	 * server, as well as one public server. If you are creating a project to 
	 * deploy somewhere else, you might choose to only put your own server's 
	 * address here.
	 */
	@Bean
	public TesterConfig testerConfig() {
		TesterConfig retVal = new TesterConfig();

		// Leer variable de entorno para decidir si mostrar la UI
		String showUi = gob.mspas.fhir.legacy.config.config.get("SHOW_UI");
		
		if ("true".equalsIgnoreCase(showUi)) {
			retVal
				.addServer()
					.withId("home")
					.withFhirVersion(FhirVersionEnum.R4)
					// Ajustado a la URL de Docker por defecto o localhost:8080
					.withBaseUrl(gob.mspas.fhir.legacy.config.config.get("FHIR_BASE_URL") != null ? 
								 gob.mspas.fhir.legacy.config.config.get("FHIR_BASE_URL") : 
								 "http://localhost:8080/fhir")
					.withName("HRO FHIR Facade");
		}
		
		return retVal;
	}
	
}
//@formatter:on
