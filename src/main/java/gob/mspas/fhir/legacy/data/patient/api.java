package gob.mspas.fhir.legacy.data.patient;

import gob.mspas.fhir.legacy.config.config;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;


public class api {

   public static String getPatientByCui(String cui) throws Exception {
      // Variables de entorno
      String apiUrl = config.get("API_URL"); // ejemplo: https://salud-digital.mspas.gob.gt:8085/personas
      String apiKey = config.get("API_KEY");
      String p12Path = config.get("P12_PATH");
      String p12Password = config.get("P12_PASSWORD");

      // 🔹 Cargar el .p12 en un KeyStore
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      try (FileInputStream fis = new FileInputStream(p12Path)) {
         keyStore.load(fis, p12Password.toCharArray());
      }

      // 🔹 Inicializar KeyManager con el certificado cliente
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(keyStore, p12Password.toCharArray());

      // 🔹 Inicializar TrustManager (con los certificados de confianza del mismo p12)
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(keyStore);

      // 🔹 Crear SSLContext con mTLS
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

      // 🔹 Construir el HttpClient con el SSLContext
      HttpClient client = HttpClient.newBuilder()
         .sslContext(sslContext)
         .build();

      // 🔹 Construir la request con API Key
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(apiUrl))
         .header("accept", "application/json")
         .header("x-api-key", apiKey)
         .GET()
         .build();

      // 🔹 Ejecutar request
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      //System.out.println("Status code: " + response.statusCode());
      //System.out.println("Response: " + response.body());

      return  response.body();

   }
}
