package gob.mspas.fhir.legacy.Exception;

/**
 * Excepción para errores 500 - Error interno del servidor
 */
public class ServerErrorException extends MSPASApiException {

    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
