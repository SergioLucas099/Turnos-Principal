package com.example.turnosprincipal.model

import kotlinx.serialization.Serializable

@Serializable
data class Multimedia(
    val _id: String? = null,
    val tipo: String,
    val url: String,
    val activo: Boolean,
    val sonido: Boolean,
    val nombre: String
)
