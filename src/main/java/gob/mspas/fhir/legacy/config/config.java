package gob.mspas.fhir.legacy.config;

import io.github.cdimascio.dotenv.Dotenv;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class config {
      private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

      public static String get(String key) {
         return dotenv.get(key);
      }

   public static class Api {
      protected final HttpClient httpClient;
      protected final String baseUrl;
      protected final TokenManager tokenManager;

      public Api() throws Exception {
         this.baseUrl = get("API_URL");
         this.tokenManager = new TokenManager();

         this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

         System.out.println("✅ Cliente API inicializado (Base URL: " + baseUrl + ")");
      }

      /**
       * Construye una petición HTTP GET con Bearer Token
       */
      protected HttpRequest buildGetRequest(String url) throws Exception {
         String token = tokenManager.getAccessToken();
         
         return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .header("User-Agent", "HRO-FHIR-Facade/1.0")
            .timeout(Duration.ofSeconds(60))
            .GET()
            .build();
      }

      /**
       * Ejecuta una petición HTTP y devuelve la respuesta
       */
      protected HttpResponse<String> executeRequest(HttpRequest request) throws IOException, InterruptedException {
         System.out.println("📤 Enviando petición a: " + request.uri());

         HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

         System.out.println("📊 Código de estado: " + response.statusCode());

         return response;
      }

      /**
       * Construye URL con parámetros de consulta
       */
      protected String buildUrl(String endpoint, String queryParams) {
         return baseUrl + endpoint + (queryParams != null && !queryParams.isEmpty() ? "?" + queryParams : "");
      }
   }
}
