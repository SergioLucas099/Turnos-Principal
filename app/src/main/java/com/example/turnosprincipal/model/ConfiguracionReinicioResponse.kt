package com.example.turnosprincipal.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfiguracionReinicioResponse(
    val hora: Int,
    val minuto: Int
)