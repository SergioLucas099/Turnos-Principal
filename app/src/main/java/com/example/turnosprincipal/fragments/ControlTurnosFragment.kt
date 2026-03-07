package com.example.turnosprincipal.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.AtraccionAdapter
import com.example.turnosprincipal.adapter.TurnosAdapter
import com.example.turnosprincipal.adapter.VerAtraccionAdapter
import com.example.turnosprincipal.model.Atraccion
import com.example.turnosprincipal.model.Turnos
import com.example.turnosprincipal.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ControlTurnosFragment : Fragment() {

    private lateinit var RevListaTurnos: RecyclerView
    private lateinit var RevListaAtracciones: RecyclerView
    private lateinit var adapterTurnos: TurnosAdapter
    private lateinit var adapterAtraccion: VerAtraccionAdapter
    private val listaTurnos = mutableListOf<Turnos>()
    private val listaAtracciones = mutableListOf<Atraccion>()
    var nombreAtraccion = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control_turnos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista Atracciones
        RevListaAtracciones = view.findViewById(R.id.RevListaAtracciones)
        adapterAtraccion = VerAtraccionAdapter(
            listaAtracciones,
            {atraccionesActualizadas -> actualizarAtracciones(atraccionesActualizadas)},
            {atraccionSeleccionada ->
                nombreAtraccion = atraccionSeleccionada.nombre
                if(nombreAtraccion.isEmpty()){
                    cargarTurnos()
                    conectarWebSocket()
                }else{
                    cargarTurnos()
                    conectarWebSocket()
                }
            }
        )

        RevListaAtracciones.layoutManager =
            LinearLayoutManager(
                view.context,
                RecyclerView.HORIZONTAL,
                false)

        RevListaAtracciones.adapter = adapterAtraccion
        cargarAtracciones()
        conectarWebSocket()

        // Lista Turnos
        RevListaTurnos = view.findViewById(R.id.RevListaTurnos)
        adapterTurnos = TurnosAdapter(
            listaTurnos,
            { turnosActualizados -> actualizarTurnos(turnosActualizados) }
        )

        RevListaTurnos.layoutManager =
            LinearLayoutManager(requireContext())

        RevListaTurnos.adapter = adapterTurnos
        cargarTurnos()
        conectarWebSocket()
    }

    private fun cargarAtracciones() {
        lifecycleScope.launch {
            try {
                val lista: List<Atraccion> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/atracciones")
                        .body()
                adapterAtraccion.actualizarLista(lista)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarAtracciones(atraccion: Atraccion) {
        lifecycleScope.launch {
            try {
                val lista: List<Atraccion> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/atracciones")
                        .body()
                adapterAtraccion.actualizarLista(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarTurnos() {
        lifecycleScope.launch {
            try {
                val lista: List<Turnos> =
                    ApiClient.client
                        .get("${ApiClient.BASE_URL}/turnos")
                        .body()

                val listaFiltrada = if(nombreAtraccion.isEmpty()){
                    lista
                } else {
                    lista.filter {
                        it.nombreAtraccion == nombreAtraccion
                    }
                }

                adapterTurnos.actualizarLista(listaFiltrada)

            } catch (e: Exception) {
                Log.e("ERROR_TURNOS", e.message ?: "Error desconocido")
            }
        }
    }

    private fun actualizarTurnos(turnos: Turnos){
        lifecycleScope.launch {
            try {
                ApiClient.client.put("${ApiClient.BASE_URL}/turno/${turnos._id}") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(turnos)
                }
                cargarTurnos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun conectarWebSocket() {
        lifecycleScope.launch {
            try {
                ApiClient.client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = "192.168.0.200",
                    port = 8080,
                    path = "/ws/turnos"
                ) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val mensaje = frame.readText()
                            when(mensaje){
                                "TURNOS_UPDATED" -> {
                                    withContext(Dispatchers.Main) {
                                        cargarTurnos()
                                    }
                                }
                                "ATRACCIONES_UPDATED" -> {
                                    withContext(Dispatchers.Main) {
                                        cargarAtracciones()
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}