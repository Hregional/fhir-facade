package gob.mspas.fhir.servlet;

import java.util.ArrayList;
import java.util.List;

import gob.mspas.fhir.provider.OrganizationResourceProvider;
import gob.mspas.fhir.provider.PatientResourceProvider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.narrative.INarrativeGenerator;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import java.util.Arrays;
import org.springframework.web.cors.CorsConfiguration;

/**
 * This servlet is the actual FHIR server itself
 */
public class ExampleRestfulServlet extends RestfulServer {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public ExampleRestfulServlet() {
		super(FhirContext.forR4()); // This is an R4 server
	}
	
	/**
	 * This method is called automatically when the
	 * servlet is initializing.
	 */
	@Override
	public void initialize() {
      System.setProperty("jdk.tls.maxHandshakeMessageSize", "50000");
		/*
		 * One resource provider is defined. It handles Patient resources.
		 */
		List<IResourceProvider> providers = new ArrayList<>();
		providers.add(new PatientResourceProvider());
		providers.add(new OrganizationResourceProvider());
		setResourceProviders(providers);
		
		/*
		 * Use a narrative generator. This is a completely optional step, 
		 * but can be useful as it causes HAPI to generate narratives for
		 * resources which don't otherwise have one.
		 */
		INarrativeGenerator narrativeGen = new DefaultThymeleafNarrativeGenerator();
		getFhirContext().setNarrativeGenerator(narrativeGen);

		/*
		 * Use nice coloured HTML when a browser is used to request the content
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());
		registerInterceptor(new gob.mspas.fhir.interceptor.NewLoggingInterceptor());

		// Define your CORS configuration. This is an example
		// showing a typical setup. You should customize this
		// to be as restrictive as possible for your application.
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedHeader("x-fhir-starter");
		corsConfig.addAllowedHeader("Origin");
		corsConfig.addAllowedHeader("Accept");
		corsConfig.addAllowedHeader("X-Requested-With");
		corsConfig.addAllowedHeader("Content-Type");
		corsConfig.addAllowedHeader("Access-Control-Request-Method");
		corsConfig.addAllowedHeader("Access-Control-Request-Headers");
		corsConfig.addAllowedOrigin("*");
		corsConfig.addExposedHeader("Location");
		corsConfig.addExposedHeader("Content-Location");
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		// Create the interceptor and register it
		CorsInterceptor corsInterceptor = new CorsInterceptor(corsConfig);
		registerInterceptor(corsInterceptor);
	}

}
