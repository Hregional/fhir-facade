package gob.mspas.fhir.legacy.Exception;


/**
 * Excepción para errores 401 - Acceso no autorizado
 */
public class UnauthorizedException extends MSPASApiException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
