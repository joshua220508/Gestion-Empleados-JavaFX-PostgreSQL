package com.example.gestionempleados.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Empleado {

    private int idEmpleado;
    private String codigo;
    private String nombre;
    private String correo;
    private String cargo;
    private BigDecimal salario;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Empleado(int idEmpleado, String codigo, String nombre,
                    String correo, String cargo, BigDecimal salario,
                    LocalDateTime fechaRegistro, boolean activo) {
        this.idEmpleado = idEmpleado;
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public Empleado(String codigo, String nombre, String correo,
                    String cargo, BigDecimal salario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}

