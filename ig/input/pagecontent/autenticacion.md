### URL base

| Ambiente | URL base FHIR |
|---|---|
| Pruebas (QA) | `https://openhim-qa-router.hro.gob.gt/fhir` |
| Producción | Pendiente |

Solo se acepta HTTPS. Las peticiones HTTP se redirigen a HTTPS.

### Credenciales

Cada sistema cliente recibe del HRO un **ID de cliente** y una **contraseña**, que se registran como cliente en OpenHIM. Se envían en cada petición con **HTTP Basic**:

```
Authorization: Basic base64(<id-cliente>:<contraseña>)
```

Con `curl`:

```bash
curl -u "<id-cliente>:<contraseña>" \
  -H "Accept: application/fhir+json" \
  "https://openhim-qa-router.hro.gob.gt/fhir/Patient/123456"
```

- Para solicitar credenciales, contacta al equipo de integración del HRO e indica el nombre del sistema y la institución.
- Las credenciales son por sistema, no por persona. No las compartas ni las incluyas en código del lado del navegador.
- Si las credenciales son incorrectas o faltan, OpenHIM responde **401** y la petición no llega al servidor FHIR.

### Encabezados

| Encabezado | Valor | Notas |
|---|---|---|
| `Accept` | `application/fhir+json` | Recomendado. Sin él, el servidor puede responder en XML, o en HTML si la petición viene de un navegador. |
| `Authorization` | `Basic ...` | Obligatorio. |

Si no puedes enviar `Accept`, agrega `_format=json` a la URL.

### URLs en las respuestas

Los enlaces de las respuestas (`Bundle.link`, `Bundle.entry.fullUrl`) usan siempre la URL pública de OpenHIM. Se pueden seguir directamente con las mismas credenciales.
