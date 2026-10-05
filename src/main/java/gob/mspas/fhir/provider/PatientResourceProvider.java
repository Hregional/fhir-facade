package gob.mspas.fhir.provider;

import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import gob.mspas.fhir.legacy.DTO.DireccionLegacyDTO;
import gob.mspas.fhir.legacy.DTO.PacienteLegacyDTO;
import gob.mspas.fhir.legacy.data.patient.PacienteLegacyService;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PatientResourceProvider que integra con la nueva API Legacy del HRO
 */
public class PatientResourceProvider implements IResourceProvider {

   private static final Logger logger = LoggerFactory.getLogger(PatientResourceProvider.class);
   private final PacienteLegacyService pacienteService;

   public PatientResourceProvider() {
      try {
         this.pacienteService = new PacienteLegacyService();
      } catch (Exception e) {
         logger.error("Error inicializando PacienteLegacyService", e);
         throw new InternalErrorException("Error inicializando PacienteLegacyService: " + e.getMessage());
      }
   }

   @Override
   public Class<Patient> getResourceType() {
      return Patient.class;
   }

   /**
    * Búsqueda por ID (se asocia a noHistoriaClinica)
    * GET /Patient/{id}
    */
   @Read()
   public Patient getResourceById(@IdParam IdType theId) {
      try {
         String id = theId.getIdPart();
         logger.info("Búsqueda por ID (Historia Clínica): {}", id);

         List<PacienteLegacyDTO> resultados = pacienteService.buscarPorHistoriaClinica(id);

         if (resultados.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró paciente con Historia Clínica: " + id);
         }

         return convertirAPatient(resultados.get(0));

      } catch (ResourceNotFoundException e) {
         throw e;
      } catch (Exception e) {
         logger.error("Error en búsqueda por ID", e);
         throw new InternalErrorException("Error interno: " + e.getMessage());
      }
   }

   /**
    * Búsqueda por parámetros múltiples
    * GET /Patient?identifier=[system]|[value]&birthdate=[date]&family=[apellido]&given=[nombre]
    */
   @Search()
   public List<Patient> searchPatients(
      @Description(shortDefinition = "CUI (13 digitos, system http://renap.gob.gt/cui) o No. de historia clinica. Tiene prioridad sobre los demas parametros")
      @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
      @Description(shortDefinition = "Fecha de nacimiento YYYY-MM-DD. Se usa si no se envia identifier")
      @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthdate,
      @Description(shortDefinition = "Apellidos separados por espacio (primer y segundo apellido). Se usa si no se envia identifier ni birthdate")
      @OptionalParam(name = Patient.SP_FAMILY) StringParam family,
      @Description(shortDefinition = "Nombres separados por espacio (primer y segundo nombre). Se combina con family")
      @OptionalParam(name = Patient.SP_GIVEN) StringParam given) {

      try {
         List<PacienteLegacyDTO> resultados = new ArrayList<>();

         // 1. Búsqueda por Identifier (CUI o Historia Clínica)
         if (identifier != null) {
            String value = identifier.getValue();
            String system = identifier.getSystem();

            if ("http://renap.gob.gt/cui".equals(system) || (value != null && value.matches("\\d{13}"))) {
               resultados = pacienteService.buscarPorCUI(value);
            } else {
               resultados = pacienteService.buscarPorHistoriaClinica(value);
            }
         }
         // 2. Búsqueda por Fecha de Nacimiento
         else if (birthdate != null) {
            // Formato esperado por API: YYYY-MM-DD (ISO)
            String fechaStr = birthdate.getValueAsString();
            resultados = pacienteService.buscarPorFechaNacimiento(fechaStr);
         }
         // 3. Búsqueda por Nombre
         else if (family != null || given != null) {
            String givenName = given != null ? given.getValue() : "";
            String familyName = family != null ? family.getValue() : "";

            // Dividir los nombres si vienen varios (ej: "Juan Carlos")
            String[] nombres = givenName.trim().split("\\s+");
            String primerNombre = nombres.length > 0 ? nombres[0] : null;
            String segundoNombre = nombres.length > 1 ? nombres[1] : null;

            // Dividir los apellidos si vienen varios (ej: "Lopez Perez")
            String[] apellidos = familyName.trim().split("\\s+");
            String primerApellido = apellidos.length > 0 ? apellidos[0] : null;
            String segundoApellido = apellidos.length > 1 ? apellidos[1] : null;

            resultados = pacienteService.buscarPorNombre(primerNombre, segundoNombre, primerApellido, segundoApellido, null);
         } else {
            throw new InvalidRequestException("Debe proporcionar al menos un parámetro de búsqueda (identifier, birthdate, family o given)");
         }

         return resultados.stream()
            .map(this::convertirAPatient)
            .collect(Collectors.toList());

      } catch (Exception e) {
         logger.error("Error en búsqueda de pacientes", e);
         throw new InternalErrorException("Error en la consulta: " + e.getMessage());
      }
   }

   /**
    * Convierte el DTO Legacy al recurso Patient de FHIR
    */
   private Patient convertirAPatient(PacienteLegacyDTO dto) {
      Patient patient = new Patient();

      // ID lógico
      patient.setId(dto.getNoHistoriaClinica());

      // Identificador: Historia Clínica
      patient.addIdentifier()
         .setSystem("http://hro.gob.gt/historia-clinica")
         .setValue(dto.getNoHistoriaClinica())
         .setUse(Identifier.IdentifierUse.OFFICIAL)
         .setType(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
            .setCode("MR")
            .setDisplay("Medical record number")));

      // Identificador: CUI (si existe)
      if (dto.getCodigoRenap() != null && !dto.getCodigoRenap().isEmpty()) {
         patient.addIdentifier()
            .setSystem("http://renap.gob.gt/cui")
            .setValue(dto.getCodigoRenap())
            .setUse(Identifier.IdentifierUse.OFFICIAL)
            .setType(new CodeableConcept().addCoding(new Coding()
               .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
               .setCode("NI")
               .setDisplay("National unique individual identifier")));
      }

      // Nombre
      HumanName name = patient.addName()
         .setUse(HumanName.NameUse.OFFICIAL)
         .setFamily(dto.getApellidos());
      
      if (dto.getNombres() != null) {
         for (String n : dto.getNombres().split("\\s+")) {
            name.addGiven(n);
         }
      }

      // Género
      if (dto.getSexo() != null) {
         String sexo = dto.getSexo().toUpperCase();
         if (sexo.startsWith("M")) patient.setGender(AdministrativeGender.MALE);
         else if (sexo.startsWith("F")) patient.setGender(AdministrativeGender.FEMALE);
         else patient.setGender(AdministrativeGender.UNKNOWN);
      }

      // Fecha de Nacimiento
      if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isEmpty()) {
         try {
            // Intentar parsear como OffsetDateTime primero (ISO 8601 completo)
            try {
               OffsetDateTime odt = OffsetDateTime.parse(dto.getFechaNacimiento());
               patient.setBirthDate(java.sql.Date.valueOf(odt.toLocalDate()));
            } catch (Exception e) {
               // Si falla, intentar parsear solo la fecha (YYYY-MM-DD)
               String fechaSolo = dto.getFechaNacimiento().split("T")[0];
               patient.setBirthDate(java.sql.Date.valueOf(fechaSolo));
            }
         } catch (Exception e) {
            logger.warn("No se pudo parsear fecha de nacimiento: {}", dto.getFechaNacimiento());
         }
      }

      // Dirección
      if (dto.getDireccionPaciente() != null) {
         DireccionLegacyDTO dirDto = dto.getDireccionPaciente();
         Address address = patient.addAddress()
            .addLine(dirDto.getDescripcion())
            .setCity(dirDto.getMunicipio())
            .setDistrict(dirDto.getDepartamento())
            .setCountry("Guatemala");
      }

      // Contactos (Responsable)
      if (dto.getNombre_Resposable() != null && !dto.getNombre_Resposable().isEmpty()) {
         Patient.ContactComponent contact = patient.addContact();
         contact.setName(new HumanName().setText(dto.getNombre_Resposable()));
         if (dto.getTelefono_Responsable() != null) {
            contact.addTelecom().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(dto.getTelefono_Responsable());
         }
         if (dto.getDireccion_Responsable() != null) {
            contact.setAddress(new Address().addLine(dto.getDireccion_Responsable()));
         }
      }

      // Extensiones para nombres de padres
      if (dto.getNombreMadre() != null) {
         patient.addExtension("http://hro.gob.gt/fhir/StructureDefinition/nombre-madre", new StringType(dto.getNombreMadre()));
      }
      if (dto.getNombrePadre() != null) {
         patient.addExtension("http://hro.gob.gt/fhir/StructureDefinition/nombre-padre", new StringType(dto.getNombrePadre()));
      }

      patient.setActive(true);
      return patient;
   }
}
