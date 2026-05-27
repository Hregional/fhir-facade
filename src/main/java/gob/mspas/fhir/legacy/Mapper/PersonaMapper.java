package gob.mspas.fhir.legacy.Mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gob.mspas.fhir.legacy.DTO.Persona;
import gob.mspas.fhir.legacy.DTO.PersonaResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PersonaMapper {
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

   private final ObjectMapper objectMapper;

   public PersonaMapper() {
      this.objectMapper = new ObjectMapper();
   }

   /**
    * Mapea una respuesta JSON completa a PersonaResponse
    */
   public PersonaResponse mapearRespuesta(String jsonResponse) throws Exception {
      JsonNode rootNode = objectMapper.readTree(jsonResponse);
      return mapearRespuesta(rootNode);
   }

   /**
    * Mapea un JsonNode a PersonaResponse
    */
   public PersonaResponse mapearRespuesta(JsonNode jsonNode) {
      PersonaResponse response = new PersonaResponse();

      // Mapear campos básicos
      response.setError(getBooleanValue(jsonNode, "error", true));
      response.setMensaje(getStringValue(jsonNode, "mensaje", "Sin mensaje"));

      // Mapear solicitudes restantes si existe
      if (jsonNode.has("solicitudes_restantes")) {
         response.setSolicitudesRestantes(getIntegerValue(jsonNode, "solicitudes_restantes", null));
      }

      // Mapear array de resultados si existe y no hay error
      if (!response.isError() && jsonNode.has("resultado") && jsonNode.get("resultado").isArray()) {
         List<Persona> personas = mapearListaPersonas(jsonNode.get("resultado"));
         response.setResultado(personas);

         System.out.println("✅ Mapeadas " + personas.size() + " persona(s)");
      } else {
         System.out.println("ℹ️  Sin resultados para mapear o respuesta con error");
      }

      return response;
   }

   /**
    * Mapea un array JSON de personas a una lista de objetos Persona
    */
   private List<Persona> mapearListaPersonas(JsonNode arrayNode) {
      List<Persona> personas = new ArrayList<>();

      for (JsonNode personaNode : arrayNode) {
         try {
            Persona persona = mapearPersona(personaNode);
            personas.add(persona);
         } catch (Exception e) {
            System.err.println("⚠️  Error mapeando persona: " + e.getMessage());
            // Continúar con las demás personas
         }
      }

      return personas;
   }

   /**
    * Mapea un JsonNode individual a un objeto Persona
    */
   public Persona mapearPersona(JsonNode personaNode) throws Exception {
      Persona persona = new Persona();

      // Mapear campos básicos
      persona.setCui(getStringValue(personaNode, "CUI", null));
      persona.setPrimerNombre(getStringValue(personaNode, "PRIMER_NOMBRE", null));
      persona.setSegundoNombre(getStringValue(personaNode, "SEGUNDO_NOMBRE", null));
      persona.setTercerNombre(getStringValue(personaNode, "TERCER_NOMBRE", null));
      persona.setPrimerApellido(getStringValue(personaNode, "PRIMER_APELLIDO", null));
      persona.setSegundoApellido(getStringValue(personaNode, "SEGUNDO_APELLIDO", null));
      persona.setApellidoCasada(getStringValue(personaNode, "APELLIDO_CASADA", null));
      persona.setSexo(getStringValue(personaNode, "SEXO", null));
      persona.setEstadoCivil(getStringValue(personaNode, "ESTADO_CIVIL", null));

      // Mapear fecha de nacimiento
      String fechaStr = getStringValue(personaNode, "FECHA_NACIMIENTO", null);
      if (fechaStr != null && !fechaStr.trim().isEmpty()) {
         try {
            LocalDate fecha = LocalDate.parse(fechaStr, DATE_FORMAT);
            persona.setFechaNacimiento(fecha);
         } catch (DateTimeParseException e) {
            System.err.println("⚠️  Error parseando fecha: " + fechaStr + " - " + e.getMessage());
            // La fecha queda como null
         }
      }

      // Validar que al menos tenga datos mínimos
      if (persona.getCui() == null && persona.getPrimerNombre() == null) {
         throw new Exception("Persona sin datos mínimos (CUI o primer nombre)");
      }

      return persona;
   }

   // Métodos de utilidad para extraer valores del JSON

   private String getStringValue(JsonNode node, String field, String defaultValue) {
      if (!node.has(field)) {
         return defaultValue;
      }

      JsonNode fieldNode = node.get(field);
      if (fieldNode.isNull() || fieldNode.asText().trim().isEmpty()) {
         return defaultValue;
      }

      return fieldNode.asText().trim();
   }

   private Integer getIntegerValue(JsonNode node, String field, Integer defaultValue) {
      if (!node.has(field)) {
         return defaultValue;
      }

      JsonNode fieldNode = node.get(field);
      if (fieldNode.isNull()) {
         return defaultValue;
      }

      try {
         return fieldNode.asInt();
      } catch (Exception e) {
         System.err.println("⚠️  Error parseando entero para campo " + field + ": " + e.getMessage());
         return defaultValue;
      }
   }

   private Boolean getBooleanValue(JsonNode node, String field, Boolean defaultValue) {
      if (!node.has(field)) {
         return defaultValue;
      }

      JsonNode fieldNode = node.get(field);
      if (fieldNode.isNull()) {
         return defaultValue;
      }

      return fieldNode.asBoolean(defaultValue);
   }

   /**
    * Método de utilidad para formatear fecha desde LocalDate a String
    */
   public String formatearFecha(LocalDate fecha) {
      if (fecha == null) {
         return null;
      }

      return fecha.format(DATE_FORMAT);
   }

   /**
    * Método de utilidad para parsear fecha desde String a LocalDate
    */
   public LocalDate parsearFecha(String fechaStr) throws DateTimeParseException {
      if (fechaStr == null || fechaStr.trim().isEmpty()) {
         return null;
      }

      return LocalDate.parse(fechaStr.trim(), DATE_FORMAT);
   }

   /**
    * Método de utilidad para debugging - imprime el JSON de manera legible
    */
   public void imprimirJsonPretty(String json) {
      try {
         JsonNode node = objectMapper.readTree(json);
         String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
         System.out.println("📋 JSON Response:");
         System.out.println(prettyJson);
      } catch (Exception e) {
         System.err.println("⚠️  Error imprimiendo JSON: " + e.getMessage());
         System.out.println("📋 JSON Raw: " + json);
      }
   }
}
