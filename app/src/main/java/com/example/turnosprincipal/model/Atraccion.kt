package com.example.turnosprincipal.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable

data class Atraccion(
    val _id: String? = null,
    val nombre: String,
    val tiempoXpersona: Int,
    val turnoActual: String,
    val activa: Boolean
)