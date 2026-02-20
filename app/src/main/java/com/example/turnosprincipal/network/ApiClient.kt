package com.example.turnosprincipal.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*

object ApiClient {

    val client = HttpClient(Android) {
        install(ContentNegotiation) { json() }
        install(WebSockets)
    }

    const val BASE_URL = "http://192.168.2.118:8080"
    //⚠️ IMPORTANTE
    //Reemplaza IP por la IP de tu PC en red local.
}