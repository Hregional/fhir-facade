Esta guía describe cómo consultar datos de pacientes del HRO usando **FHIR R4**. Está dirigida a los equipos de sistemas que se integran con el HRO.

> **Estado:** borrador para el ambiente de **pruebas (QA)**. El contenido puede cambiar antes de pasar a producción.

### Arquitectura

```
Sistema cliente
   │  HTTPS + credenciales de cliente
   ▼
OpenHIM (https://openhim-qa-router.hro.gob.gt)   ← autentica, enruta y registra cada transacción
   │
   ▼
Fachada FHIR R4 (hapi-fhirs-hro)                  ← traduce a recursos FHIR
   │
   ▼
API legacy del HRO                                 ← fuente de los datos
```

El sistema cliente **siempre** se conecta a OpenHIM; nunca directamente a la fachada FHIR. OpenHIM valida las credenciales y deja registro de cada consulta.

### Alcance

| Recurso | Operaciones | Perfil |
|---|---|---|
| Patient | `read` (por historia clínica) y `search` (por CUI, historia clínica, fecha de nacimiento o nombre) | [Paciente HRO](StructureDefinition-hro-patient.html) |

La API es de **solo lectura**: no se pueden crear, modificar ni eliminar pacientes.

### Contenido de esta guía

- [Acceso y autenticación](autenticacion.html): URL base, credenciales y encabezados.
- [Consultas de pacientes](busquedas.html): parámetros, reglas y ejemplos.
- [Respuestas y errores](errores.html): códigos HTTP y `OperationOutcome`.
- [Artefactos](artifacts.html):
  - perfil [Paciente HRO](StructureDefinition-hro-patient.html);
  - extensiones [nombre-madre](StructureDefinition-nombre-madre.html) y [nombre-padre](StructureDefinition-nombre-padre.html);
  - sistemas de identificadores [CUI](NamingSystem-cui-renap.html) e [historia clínica](NamingSystem-historia-clinica-hro.html);
  - [CapabilityStatement](CapabilityStatement-HROServer.html) y ejemplos.

### Documentación generada por el servidor

| Recurso | URL (QA) |
|---|---|
| Swagger UI (probar la API desde el navegador) | `https://openhim-qa-router.hro.gob.gt/fhir/swagger-ui/` |
| Especificación OpenAPI | `https://openhim-qa-router.hro.gob.gt/fhir/api-docs` |
| CapabilityStatement del servidor | `https://openhim-qa-router.hro.gob.gt/fhir/metadata` |
