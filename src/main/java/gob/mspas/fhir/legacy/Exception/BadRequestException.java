package gob.mspas.fhir.legacy.Exception;

/**
 * Excepción para errores 400 - Solicitud incorrecta
 */
public class BadRequestException extends MSPASApiException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
