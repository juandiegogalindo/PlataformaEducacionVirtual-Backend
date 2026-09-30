package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InscripcionRequest {

    @NotNull
    private Long cursoId;
}