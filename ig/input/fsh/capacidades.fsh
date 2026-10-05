Instance: HROServer
InstanceOf: CapabilityStatement
Title: "Servidor FHIR HRO"
Description: "Operaciones que ofrece la API FHIR del HRO a través de OpenHIM."
Usage: #definition
* name = "HROServer"
* status = #draft
* date = "2026-10-01"
* publisher = "HRO"
* kind = #requirements
* fhirVersion = #4.0.1
* format[0] = #json
* format[1] = #xml
* rest.mode = #server
* rest.documentation = "URL base QA: https://openhim-qa-router.hro.gob.gt/fhir"
* rest.security.description = "Acceso a través de OpenHIM con credenciales de cliente (HTTP Basic) entregadas por el HRO. Ver la página Acceso y autenticación."
* rest.resource.type = #Patient
* rest.resource.supportedProfile = Canonical(HROPatient)
* rest.resource.documentation = "Solo lectura. Se aplica un único criterio de búsqueda por petición, con prioridad identifier > birthdate > family/given."
* rest.resource.interaction[0].code = #read
* rest.resource.interaction[0].documentation = "GET [base]/Patient/[historia clínica]"
* rest.resource.interaction[1].code = #search-type
* rest.resource.interaction[1].documentation = "GET [base]/Patient?[parámetro]=[valor]. Sin parámetros la búsqueda es rechazada."
* rest.resource.searchParam[0].name = "identifier"
* rest.resource.searchParam[0].definition = "http://hl7.org/fhir/SearchParameter/Patient-identifier"
* rest.resource.searchParam[0].type = #token
* rest.resource.searchParam[0].documentation = "CUI (system http://renap.gob.gt/cui o valor de 13 dígitos) o número de historia clínica. Tiene prioridad sobre los demás parámetros."
* rest.resource.searchParam[1].name = "birthdate"
* rest.resource.searchParam[1].definition = "http://hl7.org/fhir/SearchParameter/individual-birthdate"
* rest.resource.searchParam[1].type = #date
* rest.resource.searchParam[1].documentation = "Fecha exacta YYYY-MM-DD. Se usa solo si no se envía identifier."
* rest.resource.searchParam[2].name = "family"
* rest.resource.searchParam[2].definition = "http://hl7.org/fhir/SearchParameter/individual-family"
* rest.resource.searchParam[2].type = #string
* rest.resource.searchParam[2].documentation = "Apellidos separados por espacio (se usan los dos primeros). Se usa solo si no se envía identifier ni birthdate."
* rest.resource.searchParam[3].name = "given"
* rest.resource.searchParam[3].definition = "http://hl7.org/fhir/SearchParameter/individual-given"
* rest.resource.searchParam[3].type = #string
* rest.resource.searchParam[3].documentation = "Nombres separados por espacio (se usan los dos primeros). Se combina con family."
