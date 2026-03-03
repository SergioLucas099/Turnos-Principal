package com.example.turnosprincipal.model

import kotlinx.serialization.Serializable

@Serializable
data class Turnos (
    val _id: String? = null,
    val atraccionId: String,
    val nombreAtraccion: String,
    val numeroTurno: String,
    val numeroPersonas: Int,
    val telefono: String,
    val tiempoEspera: Int,
    val estado: String,
    val fecha: String
)