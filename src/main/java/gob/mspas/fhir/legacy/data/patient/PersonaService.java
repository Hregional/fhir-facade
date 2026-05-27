package gob.mspas.fhir.legacy.data.patient;

import gob.mspas.fhir.legacy.DTO.BusquedaNombres;
import gob.mspas.fhir.legacy.DTO.PersonaResponse;
import gob.mspas.fhir.legacy.Exception.*;
import gob.mspas.fhir.legacy.Mapper.PersonaMapper;
import gob.mspas.fhir.legacy.config.config;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
/*** Servicio para búsqueda de personas en la API MSPAS/RENAP
 */
public class PersonaService extends config.Api {

   private static final Pattern CUI_PATTERN = Pattern.compile("\\d{13}");
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

   private final PersonaMapper mapper;

   public PersonaService() throws Exception {
      super();
      this.mapper = new PersonaMapper();
      System.out.println("🔍 Servicio de búsqueda de personas inicializado");
   }

   /**
    * Búsqueda por CUI (Código Único de Identificación)
    *
    * @param cui Código único de 13 dígitos
    * @return PersonaResponse con los resultados de la búsqueda
    */
   public PersonaResponse buscarPorCUI(String cui) throws MSPASApiException {
      System.out.println("🔍 Iniciando búsqueda por CUI: " + cui);

      // Validar CUI
      if (!validarCUI(cui)) {
         throw new IllegalArgumentException("CUI inválido, debe contener exactamente 13 dígitos numéricos");
      }

      // Construir URL
      String queryParams = "cui=" + cui;
      String url = buildUrl("/personas", queryParams);

      // Ejecutar búsqueda
      return ejecutarBusqueda(url, "CUI: " + cui);
   }

   /**
    * Búsqueda por nombres y apellidos
    * Requiere exactamente 3 parámetros: primer_nombre, primer_apellido y uno más
    *
    * @param busqueda Objeto con los parámetros de búsqueda
    * @return PersonaResponse con los resultados de la búsqueda
    */
   public PersonaResponse buscarPorNombres(BusquedaNombres busqueda) throws MSPASApiException {
      System.out.println("🔍 Iniciando búsqueda por nombres: " + busqueda.toString());

      // Validar parámetros de búsqueda
      validarBusquedaNombres(busqueda);

      // Construir parámetros de consulta
      String queryParams = construirQueryParams(busqueda);
      String url = buildUrl("/personas", queryParams);

      // Ejecutar búsqueda
      return ejecutarBusqueda(url, "Nombres: " + busqueda.toString());
   }

   /**
    * Búsqueda simplificada por nombre y apellido con fecha opcional
    */
   public PersonaResponse buscarNombreApellido(String primerNombre, String primerApellido, String fechaNacimiento) throws MSPASApiException {
      BusquedaNombres busqueda = new BusquedaNombres(primerNombre, primerApellido);

      if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
         try {
            busqueda.setFechaNacimiento(mapper.parsearFecha(fechaNacimiento));
         } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use DD/MM/YYYY");
         }
      }

      return buscarPorNombres(busqueda);
   }

   /**
    * Búsqueda simplificada con segundo nombre
    */
   public PersonaResponse buscarSegundoNombre(String primerNombre, String segundoNombre, String primerApellido) throws MSPASApiException {
      BusquedaNombres busqueda = new BusquedaNombres(primerNombre, primerApellido);
      busqueda.setSegundoNombre(segundoNombre);

      return buscarPorNombres(busqueda);
   }

   // Métodos privados de ejecución y validación

   private PersonaResponse ejecutarBusqueda(String url, String descripcion) throws MSPASApiException {
      try {
         System.out.println("📡 Ejecutando búsqueda: " + descripcion);
         System.out.println("🔗 URL: " + url);

         // Construir y ejecutar petición
         HttpRequest request = buildGetRequest(url);
         HttpResponse<String> response = executeRequest(request);

         // Procesar respuesta según código de estado
         return procesarRespuesta(response);

      } catch (Exception e) {
         System.err.println("❌ Error en búsqueda: " + e.getMessage());
         throw new MSPASApiException("Error ejecutando búsqueda: " + e.getMessage(), e);
      }
   }

   private PersonaResponse procesarRespuesta(HttpResponse<String> response) throws MSPASApiException {
      String responseBody = response.body();

      // Debug: imprimir respuesta si está habilitado
      if (System.getProperty("mspas.debug", "false").equals("true")) {
         mapper.imprimirJsonPretty(responseBody);
      }

      switch (response.statusCode()) {
         case 200:
            try {
               PersonaResponse personaResponse = mapper.mapearRespuesta(responseBody);
               System.out.println("✅ Búsqueda exitosa: " + personaResponse.cantidadResultados() + " resultado(s)");
               return personaResponse;
            } catch (Exception e) {
               throw new MSPASApiException("Error mapeando respuesta exitosa: " + e.getMessage(), e);
            }

         case 400:
            try {
               PersonaResponse errorResponse = mapper.mapearRespuesta(responseBody);
               System.out.println("⚠️  Solicitud incorrecta: " + errorResponse.getMensaje());
               throw new BadRequestException("Solicitud incorrecta: " + errorResponse.getMensaje());
            } catch (BadRequestException e) {
               throw e;
            } catch (Exception e) {
               throw new BadRequestException("Solicitud incorrecta: " + responseBody);
            }

         case 401:
            System.out.println("🔒 Acceso no autorizado");
            throw new UnauthorizedException("Acceso no autorizado - Verificar API Key");

         case 429:
            try {
               PersonaResponse rateLimitResponse = mapper.mapearRespuesta(responseBody);
               System.out.println("🚦 Límite de solicitudes alcanzado. Restantes: " + rateLimitResponse.getSolicitudesRestantes());
               throw new RateLimitException("Límite de solicitudes alcanzado");
            } catch (RateLimitException e) {
               throw e;
            } catch (Exception e) {
               throw new RateLimitException("Límite de solicitudes alcanzado");
            }

         case 500:
            System.out.println("🔥 Error interno del servidor");
            throw new ServerErrorException("Error interno del servidor");

         default:
            System.out.println("❓ Código de estado inesperado: " + response.statusCode());
            throw new MSPASApiException("Error inesperado: " + response.statusCode() + " - " + responseBody);
      }
   }

   private String construirQueryParams(BusquedaNombres busqueda) {
      StringBuilder params = new StringBuilder();

      // Parámetros requeridos
      params.append("primer_nombre=").append(encode(busqueda.getPrimerNombre()));
      params.append("&primer_apellido=").append(encode(busqueda.getPrimerApellido()));

      // Parámetros opcionales
      if (busqueda.getSegundoNombre() != null && !busqueda.getSegundoNombre().trim().isEmpty()) {
         params.append("&segundo_nombre=").append(encode(busqueda.getSegundoNombre()));
      }

      if (busqueda.getSegundoApellido() != null && !busqueda.getSegundoApellido().trim().isEmpty()) {
         params.append("&segundo_apellido=").append(encode(busqueda.getSegundoApellido()));
      }

      if (busqueda.getFechaNacimiento() != null) {
         String fechaFormateada = busqueda.getFechaNacimiento().format(DATE_FORMAT);
         params.append("&fecha_nacimiento=").append(encode(fechaFormateada));
      }

      return params.toString();
   }

   // Métodos de validación

   private boolean validarCUI(String cui) {
      return cui != null && CUI_PATTERN.matcher(cui.trim()).matches();
   }

   private void validarBusquedaNombres(BusquedaNombres busqueda) {
      if (busqueda == null) {
         throw new IllegalArgumentException("Los parámetros de búsqueda no pueden ser nulos");
      }

      if (!busqueda.esValida()) {
         throw new IllegalArgumentException(
            "Búsqueda inválida. Se requieren: primer_nombre, primer_apellido y al menos un parámetro adicional " +
               "(segundo_nombre, segundo_apellido o fecha_nacimiento)"
         );
      }

      // Verificar que tenga exactamente 3 o más parámetros (según documentación)
      if (busqueda.cantidadParametros() < 3) {
         throw new IllegalArgumentException("Se requieren exactamente 3 parámetros para la búsqueda");
      }
   }

   // Método de utilidad
   private String encode(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
   }

}
