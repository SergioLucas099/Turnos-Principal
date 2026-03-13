package com.example.turnosprincipal.model

import kotlinx.serialization.Serializable

@Serializable
data class ProgramarReinicioRequest(
    val hora: Int,
    val minuto: Int
)