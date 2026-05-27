package gob.mspas.fhir.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import gob.mspas.fhir.legacy.DTO.BusquedaNombres;
import gob.mspas.fhir.legacy.DTO.Persona;
import gob.mspas.fhir.legacy.DTO.PersonaResponse;
import gob.mspas.fhir.legacy.Exception.MSPASApiException;
import gob.mspas.fhir.legacy.data.patient.PersonaService;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PatientResourceProvider que integra con la API del MSPAS/RENAP
 */
public class PatientResourceProvider implements IResourceProvider {

   private static final Logger logger = LoggerFactory.getLogger(PatientResourceProvider.class);
   private final PersonaService personaService;

   public PatientResourceProvider() {
      try {
         this.personaService = new PersonaService();
      } catch (Exception e) {
         logger.error("Error inicializando PersonaService", e);
         throw new InternalErrorException("Error inicializando PersonaService: " + e.getMessage());
      }
   }

   @Override
   public Class<Patient> getResourceType() {
      return Patient.class;
   }

   /**
    * Búsqueda por ID (CUI)
    * GET /Patient/{cui}
    */
   @Read()
   public Patient getResourceById(@IdParam IdType theId) {
      try {
         String cui = theId.getIdPart();
         logger.info("Búsqueda por CUI: {}", cui);

         // Validar formato básico del CUI (13 dígitos)
         if (!cui.matches("\\d{13}")) {
            logger.warn("CUI inválido: {}", cui);
            throw new InvalidRequestException("CUI debe tener 13 dígitos numéricos");
         }

         PersonaResponse response = personaService.buscarPorCUI(cui);

         if (response.isError()) {
            logger.warn("Error en búsqueda por CUI {}: {}", cui, response.getMensaje());
            throw new ResourceNotFoundException("No se encontró paciente con CUI: " + cui +
               ". Mensaje: " + response.getMensaje());
         }

         if (!response.tieneResultados()) {
            logger.info("No se encontraron resultados para CUI: {}", cui);
            throw new ResourceNotFoundException("No se encontró paciente con CUI: " + cui);
         }

         // Tomar el primer resultado
         Persona persona = response.getResultado().get(0);
         return convertirPersonaAPatient(persona);

      } catch (MSPASApiException e) {
         logger.error("Error consultando API MSPAS", e);
         throw new InternalErrorException("Error consultando API MSPAS: " + e.getMessage());
      } catch (Exception e) {
         logger.error("Error interno en búsqueda por ID", e);
         throw new InternalErrorException("Error interno: " + e.getMessage());
      }
   }

   /**
    * Búsqueda por parámetros múltiples
    * GET /Patient?family={apellido}&given={nombre}&birthdate={fecha}&identifier={cui}&_count={count}&_getpagesoffset={offset}
    */
   @Search()
   public List<Patient> searchPatients(
      @OptionalParam(name = Patient.SP_FAMILY) StringParam family,
      @OptionalParam(name = Patient.SP_GIVEN) StringParam given,
      @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthdate,
      @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
      @OptionalParam(name = "_count") NumberParam count,
      @OptionalParam(name = "_getpagesoffset") NumberParam offset) {

      try {
         logger.info("Búsqueda de pacientes - family: {}, given: {}, birthdate: {}, identifier: {}, count: {}, offset: {}",
            family != null ? family.getValue() : null,
            given != null ? given.getValue() : null,
            birthdate != null ? birthdate.getValueAsString() : null,
            identifier != null ? identifier.getValue() : null,
            count != null ? count.getValue() : null,
            offset != null ? offset.getValue() : null);

         List<Patient> resultados;

         // Si se proporciona identifier (CUI), hacer búsqueda directa
         if (identifier != null && identifier.getValue() != null) {
            logger.info("Realizando búsqueda por CUI: {}", identifier.getValue());
            resultados = buscarPorCUI(identifier.getValue());
         }
         // Si se proporcionan nombres, hacer búsqueda por nombres
         else if (family != null || given != null) {
            logger.info("Realizando búsqueda por nombres");
            resultados = buscarPorNombres(family, given, birthdate);
         } else {
            logger.warn("Parámetros de búsqueda insuficientes");
            throw new InvalidRequestException("Debe proporcionar al menos 'identifier' (CUI) o 'family'/'given' (nombres)");
         }

         logger.info("Se encontraron {} resultados", resultados.size());

         // Aplicar paginación si se especifica
         if (count != null || offset != null) {
            resultados = aplicarPaginacion(resultados, count, offset);
         }

         return resultados;

      } catch (MSPASApiException e) {
         logger.error("Error consultando API MSPAS", e);
         throw new InternalErrorException("Error consultando API MSPAS: " + e.getMessage());
      } catch (Exception e) {
         logger.error("Error interno en búsqueda", e);
         throw new InternalErrorException("Error interno: " + e.getMessage());
      }
   }

   private List<Patient> aplicarPaginacion(List<Patient> resultados, NumberParam count, NumberParam offset) {
      int offsetValue = offset != null ? offset.getValue().intValue() : 0;
      int countValue = count != null ? count.getValue().intValue() : resultados.size();

      logger.info("Aplicando paginación - offset: {}, count: {}", offsetValue, countValue);

      if (offsetValue >= resultados.size()) {
         return Collections.emptyList();
      }

      int endIndex = Math.min(offsetValue + countValue, resultados.size());
      return resultados.subList(offsetValue, endIndex);
   }

   private List<Patient> buscarPorCUI(String cui) throws MSPASApiException {
      if (!cui.matches("\\d{13}")) {
         logger.warn("CUI inválido en búsqueda: {}", cui);
         throw new InvalidRequestException("CUI debe tener 13 dígitos numéricos");
      }

      PersonaResponse response = personaService.buscarPorCUI(cui);

      if (response.isError() || !response.tieneResultados()) {
         logger.info("No se encontraron resultados para CUI: {}", cui);
         return Collections.emptyList();
      }

      return response.getResultado().stream()
         .map(this::convertirPersonaAPatient)
         .collect(Collectors.toList());
   }

   private List<Patient> buscarPorNombres(StringParam family, StringParam given, DateParam birthdate)
      throws MSPASApiException {

      // Mejorar el manejo de nombres - no dividir automáticamente
      String nombreCompleto = given != null ? given.getValue().trim() : null;
      String apellidoCompleto = family != null ? family.getValue().trim() : null;

      logger.info("Búsqueda por nombres - nombre: '{}', apellido: '{}'", nombreCompleto, apellidoCompleto);

      // Separar nombres y apellidos solo si es necesario para la API
      String[] nombres = nombreCompleto != null ? separarNombres(nombreCompleto) : new String[]{null, null};
      String[] apellidos = apellidoCompleto != null ? separarApellidos(apellidoCompleto) : new String[]{null, null};

      String primerNombre = nombres[0];
      String segundoNombre = nombres[1];
      String primerApellido = apellidos[0];
      String segundoApellido = apellidos[1];

      logger.debug("Nombres separados - primerNombre: '{}', segundoNombre: '{}', primerApellido: '{}', segundoApellido: '{}'",
         primerNombre, segundoNombre, primerApellido, segundoApellido);

      PersonaResponse response = null;

      try {
         // Intentar diferentes estrategias de búsqueda

         // 1. Si tenemos nombre y apellido completos, probar primero sin separar
         if (nombreCompleto != null && apellidoCompleto != null && birthdate != null) {
            logger.info("Intentando búsqueda con nombre completo");
            String fechaStr = formatearFecha(birthdate);
            // Probar con el nombre completo como primer nombre
            response = personaService.buscarNombreApellido(nombreCompleto, apellidoCompleto, fechaStr);
         }

         // 2. Si no hay resultados o no hay fecha, probar con nombres separados
         if ((response == null || !response.tieneResultados()) &&
            primerNombre != null && primerApellido != null) {

            if (segundoNombre != null) {
               logger.info("Intentando búsqueda con segundo nombre");
               response = personaService.buscarSegundoNombre(primerNombre, primerApellido, segundoNombre);
            } else if (birthdate != null) {
               logger.info("Intentando búsqueda con nombres separados y fecha");
               String fechaStr = formatearFecha(birthdate);
               response = personaService.buscarNombreApellido(primerNombre, primerApellido, fechaStr);
            }
         }

         // 3. Como último recurso, usar búsqueda completa
         if (response == null || !response.tieneResultados()) {
            logger.info("Usando búsqueda completa por nombres");
            BusquedaNombres busqueda = new BusquedaNombres();
            busqueda.setPrimerNombre(primerNombre);
            busqueda.setSegundoNombre(segundoNombre);
            busqueda.setPrimerApellido(primerApellido);
            busqueda.setSegundoApellido(segundoApellido);

            if (birthdate != null) {
               busqueda.setFechaNacimiento(parsearFecha(birthdate));
            }

            response = personaService.buscarPorNombres(busqueda);
         }

      } catch (Exception e) {
         logger.error("Error en búsqueda por nombres", e);
         throw e;
      }

      if (response == null || response.isError() || !response.tieneResultados()) {
         logger.info("No se encontraron resultados para la búsqueda por nombres");
         return Collections.emptyList();
      }

      List<Patient> resultados = response.getResultado().stream()
         .map(this::convertirPersonaAPatient)
         .collect(Collectors.toList());

      logger.info("Búsqueda por nombres completada, {} resultados encontrados", resultados.size());
      return resultados;
   }

   /**
    * Convierte una Persona del MSPAS/RENAP a un Patient FHIR
    */
   private Patient convertirPersonaAPatient(Persona persona) {
      Patient patient = new Patient();

      // ID usando el CUI
      patient.setId(persona.getCui());

      // Identifier - CUI como identificador principal
      patient.addIdentifier()
         .setSystem("urn:oid:2.16.840.1.113883.4.292") // OID para CUI Guatemala
         .setValue(persona.getCui())
         .setUse(Identifier.IdentifierUse.OFFICIAL)
         .setType(new CodeableConcept()
            .addCoding(new Coding()
               .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
               .setCode("NI")
               .setDisplay("National identifier")))
         .addExtension()
         .setUrl("http://mspas.gob.gt/fhir/StructureDefinition/cui-guatemala")
         .setValue(new StringType(persona.getCui()));

      // Nombre completo
      HumanName name = patient.addName()
         .setUse(HumanName.NameUse.OFFICIAL)
         .setFamily(persona.getApellidoCompleto());

      // Separar nombres
      String nombreCompleto = persona.getNombreCompleto();
      if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
         String[] nombres = nombreCompleto.trim().split("\\s+");
         for (String nombre : nombres) {
            name.addGiven(nombre);
         }
      }

      // Género
      if (persona.getSexo() != null) {
         switch (persona.getSexo().toUpperCase()) {
            case "M":
            case "MASCULINO":
            case "HOMBRE":
               patient.setGender(AdministrativeGender.MALE);
               break;
            case "F":
            case "FEMENINO":
            case "MUJER":
               patient.setGender(AdministrativeGender.FEMALE);
               break;
            default:
               patient.setGender(AdministrativeGender.UNKNOWN);
         }
      }

      // Fecha de nacimiento
      if (persona.getFechaNacimiento() != null) {
         try {
            // Verificar si getFechaNacimiento() devuelve LocalDate o String
            Object fechaNacimiento = persona.getFechaNacimiento();
            LocalDate fecha;

            if (fechaNacimiento instanceof LocalDate) {
               // Si ya es LocalDate, usar directamente
               fecha = (LocalDate) fechaNacimiento;
            } else if (fechaNacimiento instanceof String) {
               // Si es String, parsear con formato dd/MM/yyyy
               String fechaStr = (String) fechaNacimiento;
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
               try {
                  fecha = LocalDate.parse(fechaStr, formatter);
               } catch (Exception e) {
                  // Intentar formato ISO (yyyy-MM-dd)
                  fecha = LocalDate.parse(fechaStr);
               }
            } else {
               throw new IllegalArgumentException("Tipo de fecha no soportado: " + fechaNacimiento.getClass());
            }

            patient.setBirthDate(java.sql.Date.valueOf(fecha));

         } catch (Exception e) {
            // Log del error pero no fallar la conversión
            logger.warn("No se pudo parsear fecha de nacimiento: {} - Error: {}",
               persona.getFechaNacimiento(), e.getMessage());
         }
      }

      // Estado civil como extensión
      if (persona.getEstadoCivil() != null && !persona.getEstadoCivil().trim().isEmpty()) {
         patient.addExtension()
            .setUrl("http://mspas.gob.gt/fhir/StructureDefinition/estado-civil")
            .setValue(new StringType(persona.getEstadoCivil()));
      }

      // Marcar como activo
      patient.setActive(true);

      return patient;
   }

   private String[] separarNombres(String nombresCompletos) {
      if (nombresCompletos == null || nombresCompletos.trim().isEmpty()) {
         return new String[]{null, null};
      }

      String[] partes = nombresCompletos.trim().split("\\s+", 2);
      return new String[]{
         partes[0],
         partes.length > 1 ? partes[1] : null
      };
   }

   private String[] separarApellidos(String apellidosCompletos) {
      if (apellidosCompletos == null || apellidosCompletos.trim().isEmpty()) {
         return new String[]{null, null};
      }

      String[] partes = apellidosCompletos.trim().split("\\s+", 2);
      return new String[]{
         partes[0],
         partes.length > 1 ? partes[1] : null
      };
   }

   private String formatearFecha(DateParam dateParam) {
      if (dateParam == null || dateParam.getValue() == null) {
         return null;
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(dateParam.getValue());

      return String.format("%02d/%02d/%04d",
         cal.get(Calendar.DAY_OF_MONTH),
         cal.get(Calendar.MONTH) + 1,
         cal.get(Calendar.YEAR));
   }

   private LocalDate parsearFecha(DateParam dateParam) {
      if (dateParam == null || dateParam.getValue() == null) {
         return null;
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(dateParam.getValue());

      return LocalDate.of(
         cal.get(Calendar.YEAR),
         cal.get(Calendar.MONTH) + 1,
         cal.get(Calendar.DAY_OF_MONTH));
   }
}
