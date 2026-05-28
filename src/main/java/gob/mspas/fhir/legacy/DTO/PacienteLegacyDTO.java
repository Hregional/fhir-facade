package gob.mspas.fhir.legacy.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteLegacyDTO {
    private int codigo;
    private int persona;
    private String noHistoriaClinica;
    private String codigoRenap;
    private String nombres;
    private String apellidos;
    private String fechaNacimiento;
    private String sexo;
    private String edad;
    private String nombre_Resposable;
    private String direccion_Responsable;
    private String telefono_Responsable;
    private String nombreMadre;
    private String nombrePadre;
    private String lugarNacimiento;
    private boolean archivo_Fisico;
    private DireccionLegacyDTO direccionPaciente;

    // Getters and Setters
    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public int getPersona() { return persona; }
    public void setPersona(int persona) { this.persona = persona; }

    public String getNoHistoriaClinica() { return noHistoriaClinica; }
    public void setNoHistoriaClinica(String noHistoriaClinica) { this.noHistoriaClinica = noHistoriaClinica; }

    public String getCodigoRenap() { return codigoRenap; }
    public void setCodigoRenap(String codigoRenap) { this.codigoRenap = codigoRenap; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public String getNombre_Resposable() { return nombre_Resposable; }
    public void setNombre_Resposable(String nombre_Resposable) { this.nombre_Resposable = nombre_Resposable; }

    public String getDireccion_Responsable() { return direccion_Responsable; }
    public void setDireccion_Responsable(String direccion_Responsable) { this.direccion_Responsable = direccion_Responsable; }

    public String getTelefono_Responsable() { return telefono_Responsable; }
    public void setTelefono_Responsable(String telefono_Responsable) { this.telefono_Responsable = telefono_Responsable; }

    public String getNombreMadre() { return nombreMadre; }
    public void setNombreMadre(String nombreMadre) { this.nombreMadre = nombreMadre; }

    public String getNombrePadre() { return nombrePadre; }
    public void setNombrePadre(String nombrePadre) { this.nombrePadre = nombrePadre; }

    public String getLugarNacimiento() { return lugarNacimiento; }
    public void setLugarNacimiento(String lugarNacimiento) { this.lugarNacimiento = lugarNacimiento; }

    public boolean isArchivo_Fisico() { return archivo_Fisico; }
    public void setArchivo_Fisico(boolean archivo_Fisico) { this.archivo_Fisico = archivo_Fisico; }

    public DireccionLegacyDTO getDireccionPaciente() { return direccionPaciente; }
    public void setDireccionPaciente(DireccionLegacyDTO direccionPaciente) { this.direccionPaciente = direccionPaciente; }
}
