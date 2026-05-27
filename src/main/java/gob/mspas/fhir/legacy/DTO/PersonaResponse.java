package gob.mspas.fhir.legacy.DTO;

import java.util.List;

public class PersonaResponse {
   private boolean error;
   private String mensaje;
   private List<Persona> resultado;
   private Integer solicitudesRestantes;

   // Constructor vacío
   public PersonaResponse() {}

   // Getters y setters
   public boolean isError() {
      return error;
   }

   public void setError(boolean error) {
      this.error = error;
   }

   public String getMensaje() {
      return mensaje;
   }

   public void setMensaje(String mensaje) {
      this.mensaje = mensaje;
   }

   public List<Persona> getResultado() {
      return resultado;
   }

   public void setResultado(List<Persona> resultado) {
      this.resultado = resultado;
   }

   public Integer getSolicitudesRestantes() {
      return solicitudesRestantes;
   }

   public void setSolicitudesRestantes(Integer solicitudesRestantes) {
      this.solicitudesRestantes = solicitudesRestantes;
   }

   // Métodos de utilidad
   public boolean tieneResultados() {
      return resultado != null && !resultado.isEmpty();
   }

   public int cantidadResultados() {
      return resultado != null ? resultado.size() : 0;
   }

   @Override
   public String toString() {
      return String.format("PersonaResponse{error=%s, mensaje='%s', resultados=%d, solicitudesRestantes=%d}",
         error, mensaje, cantidadResultados(), solicitudesRestantes);
   }
}
