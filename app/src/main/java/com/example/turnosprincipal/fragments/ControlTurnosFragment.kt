package com.example.turnosprincipal.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.TurnosAdapter
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
    private lateinit var adapter: TurnosAdapter
    private val listaTurnos = mutableListOf<Turnos>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control_turnos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RevListaTurnos = view.findViewById(R.id.RevListaTurnos)

        adapter = TurnosAdapter(
            listaTurnos,
            { turnosActualizados -> actualizarTurnos(turnosActualizados) }
        )

//        RevListaTurnos.layoutManager = LinearLayoutManager(
//            view.context, RecyclerView.HORIZONTAL, false)
//        RevListaTurnos.adapter = adapter
        RevListaTurnos.layoutManager =
            LinearLayoutManager(requireContext())

        RevListaTurnos.adapter = adapter
        cargarTurnos()
        conectarWebSocket()
    }

    private fun cargarTurnos() {
        lifecycleScope.launch {
            try {
                val lista: List<Turnos> =
                    ApiClient.client
                        .get("${ApiClient.BASE_URL}/turnos")
                        .body()

                Log.d("TURNOS_SIZE", lista.size.toString())

                adapter.actualizarLista(lista)

            } catch (e: Exception) {
                Log.e("ERROR_TURNOS", e.message ?: "Error desconocido")
                e.printStackTrace()
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
                    path = "/ws/atracciones"
                ){
                    //WebSocket conectado a atracciones
                    for (frame in incoming) {
                        if (frame is Frame.Text) {

                            val mensaje = frame.readText()

                            if (mensaje == "ATRACCIONES_UPDATED") {

                                withContext(Dispatchers.Main) {
                                    cargarTurnos()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                //Error conectar WebSocket
                e.printStackTrace()
            }
        }
    }
}