### Códigos HTTP

| Código | Quién responde | Cuándo |
|---|---|---|
| 200 | Servidor FHIR | Lectura exitosa, o búsqueda con o sin resultados. |
| 400 | Servidor FHIR | Parámetros mal formados (por ejemplo, una fecha inválida). |
| 401 | OpenHIM | Credenciales faltantes o incorrectas. |
| 404 | Servidor FHIR | `GET /Patient/{id}` con una historia clínica que no existe. |
| 404 | OpenHIM | Ruta no publicada (por ejemplo, un recurso distinto de `Patient`). |
| 500 | Servidor FHIR | Error interno o del sistema de origen. Puedes reintentar más tarde. |

> **Comportamiento actual:** una búsqueda **sin parámetros** responde **500** con el mensaje "Debe proporcionar al menos un parámetro de búsqueda...". Está previsto corregirlo para que responda **400**. Trata ambos códigos como error del cliente si el mensaje es ese.

### Formato del error

Los errores del servidor FHIR vienen como `OperationOutcome`:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "processing",
      "diagnostics": "No se encontró paciente con Historia Clínica: 999999"
    }
  ]
}
```

Los errores de OpenHIM (401, 404 de ruta) **no** son `OperationOutcome`: su cuerpo es texto plano.

### Recomendaciones

- Valida el código HTTP antes de interpretar el cuerpo.
- En un 500, reintenta con espera progresiva (por ejemplo, 1 s, 5 s, 30 s) y como máximo 3 veces.
- Cada consulta queda registrada en OpenHIM. Para reportar un problema, indica la fecha y hora, la URL consultada (sin credenciales) y el código recibido.
