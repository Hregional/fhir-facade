package gob.mspas.fhir.legacy.DTO;

import java.time.LocalDate;

public class BusquedaNombres {
   private String primerNombre;
   private String segundoNombre;
   private String primerApellido;
   private String segundoApellido;
   private LocalDate fechaNacimiento;

   // Constructor vacío
   public BusquedaNombres() {
   }

   // Constructor con parámetros básicos
   public BusquedaNombres(String primerNombre, String primerApellido) {
      this.primerNombre = primerNombre;
      this.primerApellido = primerApellido;
   }

   // Getters y setters
   public String getPrimerNombre() {
      return primerNombre;
   }

   public void setPrimerNombre(String primerNombre) {
      this.primerNombre = primerNombre;
   }

   public String getSegundoNombre() {
      return segundoNombre;
   }

   public void setSegundoNombre(String segundoNombre) {
      this.segundoNombre = segundoNombre;
   }

   public String getPrimerApellido() {
      return primerApellido;
   }

   public void setPrimerApellido(String primerApellido) {
      this.primerApellido = primerApellido;
   }

   public String getSegundoApellido() {
      return segundoApellido;
   }

   public void setSegundoApellido(String segundoApellido) {
      this.segundoApellido = segundoApellido;
   }

   public LocalDate getFechaNacimiento() {
      return fechaNacimiento;
   }

   public void setFechaNacimiento(LocalDate fechaNacimiento) {
      this.fechaNacimiento = fechaNacimiento;
   }

   // Métodos de utilidad
   public boolean esValida() {
      // Validar parámetros requeridos
      if (primerNombre == null || primerNombre.trim().isEmpty()) {
         return false;
      }

      if (primerApellido == null || primerApellido.trim().isEmpty()) {
         return false;
      }

      // Verificar que al menos un parámetro adicional esté presente
      boolean tieneSegundoNombre = segundoNombre != null && !segundoNombre.trim().isEmpty();
      boolean tieneSegundoApellido = segundoApellido != null && !segundoApellido.trim().isEmpty();
      boolean tieneFechaNacimiento = fechaNacimiento != null;

      return tieneSegundoNombre || tieneSegundoApellido || tieneFechaNacimiento;
   }

   public int cantidadParametros() {
      int count = 0;

      if (primerNombre != null && !primerNombre.trim().isEmpty()) count++;
      if (segundoNombre != null && !segundoNombre.trim().isEmpty()) count++;
      if (primerApellido != null && !primerApellido.trim().isEmpty()) count++;
      if (segundoApellido != null && !segundoApellido.trim().isEmpty()) count++;
      if (fechaNacimiento != null) count++;

      return count;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();

      if (primerNombre != null) sb.append(primerNombre);
      if (segundoNombre != null && !segundoNombre.trim().isEmpty()) {
         if (!sb.isEmpty()) sb.append(" ");
         sb.append(segundoNombre);
      }
      if (primerApellido != null) {
         if (!sb.isEmpty()) sb.append(" ");
         sb.append(primerApellido);
      }
      if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
         if (!sb.isEmpty()) sb.append(" ");
         sb.append(segundoApellido);
      }
      if (fechaNacimiento != null) {
         if (!sb.isEmpty()) sb.append(" ");
         sb.append("(").append(fechaNacimiento).append(")");
      }

      return sb.toString().trim();
   }
}
