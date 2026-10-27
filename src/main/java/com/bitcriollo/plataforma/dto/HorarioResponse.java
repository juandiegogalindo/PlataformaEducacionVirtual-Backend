package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class HorarioResponse {
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}