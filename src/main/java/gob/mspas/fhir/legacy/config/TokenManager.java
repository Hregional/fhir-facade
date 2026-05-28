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
            .expireAfterWrite(50, TimeUnit.MINUTES) // El token suele durar 60 min
            .build();

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String tokenUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TokenManager() {
        this.clientId = config.get("CLIENT_ID");
        this.clientSecret = config.get("CLIENT_SECRET");
        this.username = config.get("USERNAME");
        this.password = config.get("PASSWORD");
        this.tokenUrl = config.get("ACCESS_TOKEN_URL");
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    public synchronized String getAccessToken() throws Exception {
        String token = cache.getIfPresent(CACHE_KEY);
        if (token == null) {
            token = refreshToken();
            cache.put(CACHE_KEY, token);
        }
        return token;
    }

    private String refreshToken() throws Exception {
        System.out.println("🔑 Solicitando nuevo token de acceso a: " + tokenUrl);

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String body = "grant_type=password" +
                "&username=" + username +
                "&password=" + password +
                "&scope=openid profile";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encodedAuth)
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
