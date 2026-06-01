package gob.mspas.fhir.legacy.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class TokenManager {
    private static final String CACHE_KEY = "access_token";
    private static final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(4, TimeUnit.MINUTES) // Ajustado a 4 min, la media de Keycloak es 5 min
            .build();

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String tokenUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TokenManager() {
        this.clientId = config.get("CLIENT_ID") != null ? config.get("CLIENT_ID").trim() : null;
        this.clientSecret = config.get("CLIENT_SECRET") != null ? config.get("CLIENT_SECRET").trim() : null;
        this.username = config.get("USERNAME") != null ? config.get("USERNAME").trim() : null;
        this.password = config.get("PASSWORD") != null ? config.get("PASSWORD").trim() : null;
        this.tokenUrl = config.get("ACCESS_TOKEN_URL") != null ? config.get("ACCESS_TOKEN_URL").trim() : null;
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();

        // Validación de configuración
        validarConfig("CLIENT_ID", clientId);
        validarConfig("CLIENT_SECRET", clientSecret);
        validarConfig("USERNAME", username);
        validarConfig("PASSWORD", password);
        validarConfig("ACCESS_TOKEN_URL", tokenUrl);
    }

    private void validarConfig(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            System.err.println("⚠️ ADVERTENCIA: La variable de entorno '" + key + "' no está definida o está vacía.");
        } else {
            System.out.println("✅ Configuración cargada: " + key + " (longitud: " + value.length() + ")");
        }
    }

    public synchronized String getAccessToken() throws Exception {
        String token = cache.getIfPresent(CACHE_KEY);
        if (token == null) {
            token = refreshToken();
            cache.put(CACHE_KEY, token);
        }
        return token;
    }

    public void invalidateToken() {
        System.out.println("🗑️ Invalidando token del cache por error de autenticación");
        cache.invalidate(CACHE_KEY);
    }

    private String refreshToken() throws Exception {
        System.out.println("🔑 Solicitando nuevo token de acceso a: " + tokenUrl);

        // Algunos servidores prefieren las credenciales del cliente en el body en lugar de Basic Auth
       /* String body = "grant_type=password" +
                "&client_id=" + java.net.URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&client_secret=" + java.net.URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                "&username=" + java.net.URLEncoder.encode(username, StandardCharsets.UTF_8) +
                "&password=" + java.net.URLEncoder.encode(password, StandardCharsets.UTF_8) +
                "&scope=" + java.net.URLEncoder.encode("openid profile", StandardCharsets.UTF_8);

        */
         String body = "grant_type=client_credentials" +
                       "&client_id=" + java.net.URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                       "&client_secret=" + java.net.URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                       "&scope=" + java.net.URLEncoder.encode("openid profile", StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error obteniendo token: " + response.statusCode() + " - " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        String accessToken = node.get("access_token").asText();
        
        System.out.println("✅ Token obtenido exitosamente");
        return accessToken;
    }
}
