Alias: $v2-0203 = http://terminology.hl7.org/CodeSystem/v2-0203

// Las URLs de las extensiones coinciden con las que emite PatientResourceProvider
Extension: NombreMadre
Id: nombre-madre
Title: "Nombre de la madre"
Description: "Nombre completo de la madre del paciente, tal como está registrado en el sistema legacy del HRO."
Context: Patient
* value[x] only string

Extension: NombrePadre
Id: nombre-padre
Title: "Nombre del padre"
Description: "Nombre completo del padre del paciente, tal como está registrado en el sistema legacy del HRO."
Context: Patient
* value[x] only string

Invariant: cui-13-digitos
Description: "El CUI debe tener 13 dígitos."
Expression: "value.matches('^[0-9]{13}$')"
Severity: #warning

Profile: HROPatient
Parent: Patient
Id: hro-patient
Title: "Paciente HRO"
Description: "Paciente devuelto por la API FHIR del HRO. El id lógico del recurso es el número de historia clínica."
* id 1..1 MS
* id ^short = "Número de historia clínica"
* extension contains
    NombreMadre named nombreMadre 0..1 MS and
    NombrePadre named nombrePadre 0..1 MS
* identifier 1..* MS
* identifier ^slicing.discriminator.type = #value
* identifier ^slicing.discriminator.path = "system"
* identifier ^slicing.rules = #open
* identifier ^slicing.description = "Se distingue por el system"
* identifier contains
    historiaClinica 1..1 MS and
    cui 0..1 MS
* identifier[historiaClinica] ^short = "Número de historia clínica del HRO"
* identifier[historiaClinica].system 1..1
* identifier[historiaClinica].system = "http://hro.gob.gt/historia-clinica" (exactly)
* identifier[historiaClinica].value 1..1 MS
* identifier[historiaClinica].type = $v2-0203#MR
* identifier[cui] ^short = "CUI (Código Único de Identificación, RENAP)"
* identifier[cui] obeys cui-13-digitos
* identifier[cui].system 1..1
* identifier[cui].system = "http://renap.gob.gt/cui" (exactly)
* identifier[cui].value 1..1 MS
* identifier[cui].type = $v2-0203#NI
* active MS
* name 1..* MS
* name.family MS
* name.family ^short = "Apellidos (primer y segundo apellido en un solo texto)"
* name.given MS
* name.given ^short = "Un elemento por cada nombre"
* gender MS
* birthDate MS
* address MS
* address.line MS
* address.city MS
* address.city ^short = "Municipio"
* address.district MS
* address.district ^short = "Departamento"
* address.country = "Guatemala"
* contact MS
* contact ^short = "Persona responsable del paciente"
* contact.name MS
* contact.telecom MS
* contact.address MS

Instance: EjemploPaciente
InstanceOf: HROPatient
Title: "Ejemplo de paciente"
Description: "Paciente ficticio con CUI, responsable y nombres de padres."
Usage: #example
* id = "123456"
* active = true
* identifier[historiaClinica].use = #official
* identifier[historiaClinica].type = $v2-0203#MR "Medical record number"
* identifier[historiaClinica].system = "http://hro.gob.gt/historia-clinica"
* identifier[historiaClinica].value = "123456"
* identifier[cui].use = #official
* identifier[cui].type = $v2-0203#NI "National unique individual identifier"
* identifier[cui].system = "http://renap.gob.gt/cui"
* identifier[cui].value = "1234567890101"
* name.use = #official
* name.family = "Pérez López"
* name.given[0] = "Juan"
* name.given[1] = "Carlos"
* gender = #male
* birthDate = "1990-05-14"
* address.line = "4a. avenida 1-23 zona 1"
* address.city = "Guatemala"
* address.district = "Guatemala"
* address.country = "Guatemala"
* contact.name.text = "María López"
* contact.telecom.system = #phone
* contact.telecom.value = "55550000"
* contact.address.line = "4a. avenida 1-23 zona 1"
* extension[nombreMadre].valueString = "María López"
* extension[nombrePadre].valueString = "José Pérez"

Instance: EjemploPacienteSinCUI
InstanceOf: HROPatient
Title: "Ejemplo de paciente sin CUI"
Description: "Paciente ficticio registrado solo con historia clínica (por ejemplo, un recién nacido)."
Usage: #example
* id = "654321"
* active = true
* identifier[historiaClinica].use = #official
* identifier[historiaClinica].type = $v2-0203#MR "Medical record number"
* identifier[historiaClinica].system = "http://hro.gob.gt/historia-clinica"
* identifier[historiaClinica].value = "654321"
* name.use = #official
* name.family = "García"
* name.given[0] = "Ana"
* gender = #female
* birthDate = "2026-09-20"
* address.country = "Guatemala"
* extension[nombreMadre].valueString = "Lucía García"
