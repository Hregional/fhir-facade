Instance: cui-renap
InstanceOf: NamingSystem
Title: "CUI - RENAP"
Usage: #definition
* name = "CUI_RENAP"
* status = #active
* kind = #identifier
* date = "2026-10-01"
* publisher = "HRO"
* responsible = "RENAP"
* description = "Código Único de Identificación (13 dígitos) emitido por el Registro Nacional de las Personas de Guatemala."
* jurisdiction = urn:iso:std:iso:3166#GT "Guatemala"
* type = http://terminology.hl7.org/CodeSystem/v2-0203#NI
* uniqueId.type = #uri
* uniqueId.value = "http://renap.gob.gt/cui"
* uniqueId.preferred = true

Instance: historia-clinica-hro
InstanceOf: NamingSystem
Title: "Historia clínica HRO"
Usage: #definition
* name = "HistoriaClinicaHRO"
* status = #active
* kind = #identifier
* date = "2026-10-01"
* publisher = "HRO"
* responsible = "HRO"
* description = "Número de historia clínica asignado por el HRO. Es también el id lógico del recurso Patient."
* jurisdiction = urn:iso:std:iso:3166#GT "Guatemala"
* type = http://terminology.hl7.org/CodeSystem/v2-0203#MR
* uniqueId.type = #uri
* uniqueId.value = "http://hro.gob.gt/historia-clinica"
* uniqueId.preferred = true
