package gob.mspas.fhir.legacy.DTO;

import java.time.LocalDate;

public class Persona {
   private String cui;
   private String primerNombre;
   private String segundoNombre;
   private String tercerNombre;
   private String primerApellido;
   private String segundoApellido;
   private String apellidoCasada;
   private String sexo;
   private String estadoCivil;
   private LocalDate fechaNacimiento;

   // Constructor vacío
   public Persona() {
   }

   // Getters y setters
   public String getCui() {
      return cui;
   }

   public void setCui(String cui) {
      this.cui = cui;
   }

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

   public String getTercerNombre() {
      return tercerNombre;
   }

   public void setTercerNombre(String tercerNombre) {
      this.tercerNombre = tercerNombre;
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

   public String getApellidoCasada() {
      return apellidoCasada;
   }

   public void setApellidoCasada(String apellidoCasada) {
      this.apellidoCasada = apellidoCasada;
   }

   public String getSexo() {
      return sexo;
   }

   public void setSexo(String sexo) {
      this.sexo = sexo;
   }

   public String getEstadoCivil() {
      return estadoCivil;
   }

   public void setEstadoCivil(String estadoCivil) {
      this.estadoCivil = estadoCivil;
   }

   public LocalDate getFechaNacimiento() {
      return fechaNacimiento;
   }

   public void setFechaNacimiento(LocalDate fechaNacimiento) {
      this.fechaNacimiento = fechaNacimiento;
   }

   // Métodos de utilidad
   public String getNombreCompleto() {
      StringBuilder nombre = new StringBuilder();

      if (primerNombre != null) nombre.append(primerNombre);
      if (segundoNombre != null && !segundoNombre.trim().isEmpty()) {
         if (!nombre.isEmpty()) nombre.append(" ");
         nombre.append(segundoNombre);
      }
      if (tercerNombre != null && !tercerNombre.trim().isEmpty()) {
         if (!nombre.isEmpty()) nombre.append(" ");
         nombre.append(tercerNombre);
      }

      return nombre.toString().trim();
   }

   public String  getApellidoCompleto() {
      StringBuilder apellido = new StringBuilder();
      if (primerApellido != null) apellido.append(primerApellido);
      if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
         if (!apellido.isEmpty()) apellido.append(" ");
         apellido.append(segundoApellido);
      }
      return apellido.toString().trim();
   }
}
