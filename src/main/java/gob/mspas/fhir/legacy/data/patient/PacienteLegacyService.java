package gob.mspas.fhir.legacy.data.patient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gob.mspas.fhir.legacy.DTO.PacienteLegacyDTO;
import gob.mspas.fhir.legacy.config.config;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class PacienteLegacyService extends config.Api {
    private final ObjectMapper objectMapper;

    public PacienteLegacyService() throws Exception {
        super();
        this.objectMapper = new ObjectMapper();
        System.out.println("🔍 PacienteLegacyService inicializado con soporte OAuth2");
    }

    public List<PacienteLegacyDTO> buscarPorHistoriaClinica(String noHistoriaClinica) throws Exception {
        System.out.println("🔍 Búsqueda por Historia Clínica: " + noHistoriaClinica);
        String url = baseUrl + "/api/Busqueda/paciente/" + noHistoriaClinica;
        return ejecutarYListar(url);
    }

    public List<PacienteLegacyDTO> buscarPorCUI(String cui) throws Exception {
        System.out.println("🔍 Búsqueda por CUI: " + cui);
        String url = baseUrl + "/api/Busqueda/paciente/dpi/" + cui;
        return ejecutarYListar(url);
    }

    public List<PacienteLegacyDTO> buscarPorFechaNacimiento(String fechaNacimiento) throws Exception {
        System.out.println("🔍 Búsqueda por Fecha Nacimiento: " + fechaNacimiento);
        // El endpoint es /api/Busqueda/paciente/avanzado/{FechaNacimiento}
        String url = baseUrl + "/api/Busqueda/paciente/avanzado/" + fechaNacimiento;
        return ejecutarYListar(url);
    }

    public List<PacienteLegacyDTO> buscarPorNombre(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido, String tercerApellido) throws Exception {
        StringBuilder query = new StringBuilder();
        appendParam(query, "PrimerNombre", primerNombre);
        appendParam(query, "SegundoNombre", segundoNombre);
        appendParam(query, "PrimerApellido", primerApellido);
        appendParam(query, "SegundoApellido", segundoApellido);
        appendParam(query, "TercerApellido", tercerApellido);

        String url = buildUrl("/api/Busqueda/paciente/nombre", query.toString());
        return ejecutarYListar(url);
    }

    private void appendParam(StringBuilder query, String name, String value) {
        if (value != null && !value.trim().isEmpty()) {
            if (query.length() > 0) query.append("&");
            query.append(name).append("=").append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private List<PacienteLegacyDTO> ejecutarYListar(String url) throws Exception {
        HttpRequest request = buildGetRequest(url);
        HttpResponse<String> response = executeRequest(request);

        if (response.statusCode() == 404) {
            return Collections.emptyList();
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error en API Legacy: " + response.statusCode() + " - " + response.body());
        }

        String body = response.body().trim();
        if (body.startsWith("{")) {
            PacienteLegacyDTO dto = objectMapper.readValue(body, PacienteLegacyDTO.class);
            return Collections.singletonList(dto);
        } else {
            return objectMapper.readValue(body, new TypeReference<List<PacienteLegacyDTO>>() {});
        }
    }
}
