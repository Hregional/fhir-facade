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
      private static final boolean DEVELOPMENT_MODE = true; // Cambiar a false en producción

      protected final HttpClient httpClient;
      protected final String apiKey;
      protected final String baseUrl;

      public Api() throws Exception {
         this.apiKey = getRequiredConfig("API_KEY");
         this.baseUrl = getRequiredConfig("API_URL");

         // Configurar SSL Context para mTLS
         String p12Path = getRequiredConfig("P12_PATH");
         String p12Password = getRequiredConfig("P12_PASSWORD");

         KeyStore keyStore = loadP12Certificate(p12Path, p12Password);
         SSLContext sslContext = createSSLContext(keyStore, p12Password);

         this.httpClient = HttpClient.newBuilder()
            .sslContext(sslContext)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

         System.out.println("✅ Cliente API inicializado correctamente");
      }

      /**
       * Construye una petición HTTP GET estándar
       */
      protected HttpRequest buildGetRequest(String url) {
         return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey)
            .header("User-Agent", "MSPAS-FHIR-Facade/1.0")
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

      // Métodos privados de configuración

      private String getRequiredConfig(String key) {
         String value = get(key);
         if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable de entorno requerida no encontrada: " + key);
         }
         return value.trim();
      }

      private KeyStore loadP12Certificate(String p12Path, String p12Password)
         throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

         System.out.println("🔐 Cargando certificado: " + p12Path);

         KeyStore keyStore = KeyStore.getInstance("PKCS12");
         try (FileInputStream fis = new FileInputStream(p12Path)) {
            keyStore.load(fis, p12Password.toCharArray());
         }

         System.out.println("✅ Certificado cargado exitosamente");
         return keyStore;
      }

      private SSLContext createSSLContext(KeyStore keyStore, String password)
         throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {

         // 🔑 Configurar KeyManager (certificado cliente para mTLS)
         KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         kmf.init(keyStore, password.toCharArray());

         TrustManager[] trustManagers;

         if (DEVELOPMENT_MODE) {
            // ⚠️ MODO DESARROLLO: Aceptar todos los certificados
            trustManagers = new TrustManager[] {
               new X509TrustManager() {
                  public X509Certificate[] getAcceptedIssuers() {
                     return null;
                  }

                  public void checkClientTrusted(X509Certificate[] certs, String authType) {
                     // No validar certificados del cliente
                  }

                  public void checkServerTrusted(X509Certificate[] certs, String authType) {
                     System.out.println("🔓 Certificado del servidor aceptado (modo desarrollo)");
                  }
               }
            };
         } else {
            // Modo producción: validar certificados normalmente
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // usar certificados del sistema
            trustManagers = tmf.getTrustManagers();
         }

         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(kmf.getKeyManagers(), trustManagers, new SecureRandom());

         System.out.println("🔒 SSL Context configurado para mTLS");
         return sslContext;
      }
   }
}
