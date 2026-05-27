package gob.mspas.fhir.legacy.Exception;

/**
 * Excepción para errores 429 - Límite de solicitudes alcanzado
 */
public class RateLimitException extends MSPASApiException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
