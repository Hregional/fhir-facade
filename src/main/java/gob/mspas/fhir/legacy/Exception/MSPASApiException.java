package gob.mspas.fhir.legacy.Exception;

/**
 * Excepción base para errores de la API MSPAS
 */
public class MSPASApiException extends Exception {

    public MSPASApiException(String message) {
        super(message);
    }

    public MSPASApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

