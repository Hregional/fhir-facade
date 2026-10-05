En los ejemplos, `$BASE` es `https://openhim-qa-router.hro.gob.gt/fhir` y `$CRED` es `<id-cliente>:<contraseña>`. Todos los datos de ejemplo son ficticios.

### Leer un paciente por historia clínica

El id lógico del `Patient` es su **número de historia clínica**.

```bash
curl -u "$CRED" -H "Accept: application/fhir+json" "$BASE/Patient/123456"
```

- Devuelve un `Patient` conforme a [Paciente HRO](StructureDefinition-hro-patient.html). Ver el [ejemplo](Patient-123456.html).
- Si no existe, responde **404**.

### Buscar pacientes

`GET $BASE/Patient?<parámetro>=<valor>` devuelve un `Bundle` de tipo `searchset`.

| Parámetro | Tipo | Ejemplo | Busca por |
|---|---|---|---|
| `identifier` | token | `http://renap.gob.gt/cui\|1234567890101` | CUI o historia clínica |
| `birthdate` | date | `1990-05-14` | Fecha de nacimiento exacta |
| `family` | string | `Pérez López` | Apellidos |
| `given` | string | `Juan Carlos` | Nombres |

#### Reglas importantes

1. **Se aplica un solo criterio por petición.** Si envías varios, se usa el primero de esta lista y los demás se ignoran:
   1. `identifier`
   2. `birthdate`
   3. `family` + `given` (juntos)

   Por ejemplo, `?identifier=123456&birthdate=1990-05-14` busca **solo** por identifier.
2. **CUI vs. historia clínica.** El valor de `identifier` se trata como **CUI** si:
   - el system es `http://renap.gob.gt/cui`, o
   - el valor tiene exactamente 13 dígitos.

   En cualquier otro caso se trata como **número de historia clínica**. Recomendamos enviar siempre el system para evitar ambigüedades:
   - `identifier=http://renap.gob.gt/cui|1234567890101`
   - `identifier=http://hro.gob.gt/historia-clinica|123456`
3. **Nombres y apellidos compuestos.** `family` y `given` se separan por espacios y se usan las dos primeras palabras de cada uno (primer y segundo apellido, primer y segundo nombre). Puedes enviar solo uno de los dos.
4. **Fecha.** `birthdate` debe ir en formato `YYYY-MM-DD`. No se admiten prefijos de rango (`ge`, `le`, etc.).
5. **Sin parámetros** la búsqueda es rechazada (ver [errores](errores.html)).
6. **Sin paginación.** Todos los resultados vienen en un solo `Bundle`. Prefiere los criterios más específicos (CUI o historia clínica).
7. **Codifica la URL.** El `|` va como `%7C`, los espacios como `%20` y las tildes en UTF-8. `curl --data-urlencode` con `-G` lo hace por ti.

#### Ejemplos

Por CUI:

```bash
curl -u "$CRED" -H "Accept: application/fhir+json" -G "$BASE/Patient" \
  --data-urlencode "identifier=http://renap.gob.gt/cui|1234567890101"
```

Por historia clínica:

```bash
curl -u "$CRED" -H "Accept: application/fhir+json" -G "$BASE/Patient" \
  --data-urlencode "identifier=http://hro.gob.gt/historia-clinica|123456"
```

Por fecha de nacimiento:

```bash
curl -u "$CRED" -H "Accept: application/fhir+json" "$BASE/Patient?birthdate=1990-05-14"
```

Por nombre:

```bash
curl -u "$CRED" -H "Accept: application/fhir+json" -G "$BASE/Patient" \
  --data-urlencode "family=Pérez López" --data-urlencode "given=Juan Carlos"
```

#### Respuesta (resumida)

```json
{
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 1,
  "link": [
    { "relation": "self",
      "url": "https://openhim-qa-router.hro.gob.gt/fhir/Patient?identifier=http%3A%2F%2Frenap.gob.gt%2Fcui%7C1234567890101" }
  ],
  "entry": [
    {
      "fullUrl": "https://openhim-qa-router.hro.gob.gt/fhir/Patient/123456",
      "resource": {
        "resourceType": "Patient",
        "id": "123456",
        "identifier": [
          { "use": "official", "system": "http://hro.gob.gt/historia-clinica", "value": "123456" },
          { "use": "official", "system": "http://renap.gob.gt/cui", "value": "1234567890101" }
        ],
        "name": [ { "use": "official", "family": "Pérez López", "given": [ "Juan", "Carlos" ] } ],
        "gender": "male",
        "birthDate": "1990-05-14"
      },
      "search": { "mode": "match" }
    }
  ]
}
```

- Si no hay coincidencias, se devuelve `total: 0` sin `entry`. No es un error.
- El recurso completo está en el [ejemplo de paciente](Patient-123456.html).

### Datos del paciente

| Elemento | Contenido |
|---|---|
| `id` | Número de historia clínica |
| `identifier` (system `http://hro.gob.gt/historia-clinica`, tipo `MR`) | Historia clínica. Siempre presente. |
| `identifier` (system `http://renap.gob.gt/cui`, tipo `NI`) | CUI, si está registrado. |
| `name.family` | Apellidos en un solo texto |
| `name.given` | Un elemento por cada nombre |
| `gender` | `male`, `female` o `unknown` |
| `birthDate` | Fecha de nacimiento |
| `address` | `line` = dirección, `city` = municipio, `district` = departamento, `country` = Guatemala |
| `contact` | Persona responsable: nombre, teléfono y dirección |
| `extension` [nombre-madre](StructureDefinition-nombre-madre.html) / [nombre-padre](StructureDefinition-nombre-padre.html) | Nombres de los padres |
| `active` | Siempre `true` |

Los elementos sin dato en el sistema de origen se omiten. No se envían vacíos.
