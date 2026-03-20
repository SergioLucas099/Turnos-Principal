package com.example.turnosprincipal.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.AtraccionAdapter
import com.example.turnosprincipal.adapter.TurnosAdapter
import com.example.turnosprincipal.adapter.VerAtraccionAdapter
import com.example.turnosprincipal.model.Atraccion
import com.example.turnosprincipal.model.ConfiguracionReinicioResponse
import com.example.turnosprincipal.model.ProgramarReinicioRequest
import com.example.turnosprincipal.model.Turnos
import com.example.turnosprincipal.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
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

    private lateinit var btnReinicioTurnos: ImageView
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

        btnReinicioTurnos = view.findViewById(R.id.btnReinicioTurnos)

        btnReinicioTurnos.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reiniciar_turnos, null)

            val txtMostrartiempoAutom = dialogView.findViewById<TextView>(R.id.txtMostrartiempoAutom)
            val btnReincioManual = dialogView.findViewById<Button>(R.id.btnReincioManual)
            val npHora = dialogView.findViewById<NumberPicker>(R.id.npHora)
            val npMinuto = dialogView.findViewById<NumberPicker>(R.id.npMinuto)
            val npPeriodo = dialogView.findViewById<NumberPicker>(R.id.npPeriodo)

            btnReincioManual.setOnClickListener {

                AlertDialog.Builder(requireContext())
                    .setTitle("Reiniciar Turnos")
                    .setMessage("¿Seguro que desea reiniciar los turnos ahora?")
                    .setPositiveButton("Sí") { _, _ ->

                        lifecycleScope.launch {

                            try {

                                val response = ApiClient.client.post(
                                    "${ApiClient.BASE_URL}/turnos/reiniciar"
                                )

                                Toast.makeText(
                                    requireContext(),
                                    "Turnos reiniciados correctamente",
                                    Toast.LENGTH_LONG
                                ).show()

                            } catch (e: Exception) {

                                Toast.makeText(
                                    requireContext(),
                                    "Error al reiniciar turnos",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            lifecycleScope.launch {

                val texto = obtenerReinicioAutomatico()

                txtMostrartiempoAutom.text = texto
            }

            npHora.minValue = 1
            npHora.maxValue = 12
            npHora.wrapSelectorWheel = true

            val minutos = arrayOf(
                "00","05","10","15","20","25",
                "30","35","40","45","50","55"
            )
            npMinuto.minValue = 0
            npMinuto.maxValue = minutos.size - 1
            npMinuto.displayedValues = minutos

            val periodos = arrayOf("AM", "PM")
            npPeriodo.minValue = 0
            npPeriodo.maxValue = 1
            npPeriodo.displayedValues = periodos

            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setView(dialogView)
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Cancelar"){ dialog, _ ->
                    dialog.dismiss()
                }

            val dialog = builder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val horaSeleccionada = npHora.value
                val minutoSeleccionado = minutos[npMinuto.value].toInt()
                val periodo = periodos[npPeriodo.value]

                var hora24 = horaSeleccionada

                if (periodo == "PM" && horaSeleccionada != 12) {
                    hora24 += 12
                }

                if (periodo == "AM" && horaSeleccionada == 12) {
                    hora24 = 0
                }

                lifecycleScope.launch {

                    try {

                        val response = ApiClient.client.post("${ApiClient.BASE_URL}/turnos/programarReinicio") {

                            contentType(io.ktor.http.ContentType.Application.Json)

                            setBody(
                                ProgramarReinicioRequest(
                                    hora = hora24,
                                    minuto = minutoSeleccionado
                                )
                            )
                        }

                        Log.d("TURNOS", "Respuesta servidor: ${response.status}")

                        Toast.makeText(
                            requireContext(),
                            "Reinicio diario programado",
                            Toast.LENGTH_LONG
                        ).show()

                        dialog.dismiss()

                    } catch (e: Exception) {

                        e.printStackTrace()

                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Lista Atracciones
        RevListaAtracciones = view.findViewById(R.id.RevListaAtracciones)

        adapterAtraccion = VerAtraccionAdapter(
            listaAtracciones,
            { atraccionesActualizadas -> actualizarAtracciones(atraccionesActualizadas) },
            { atraccionSeleccionada ->
                nombreAtraccion = atraccionSeleccionada.nombre
                cargarTurnos()
            }
        )

        RevListaAtracciones.layoutManager =
            LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

        RevListaAtracciones.adapter = adapterAtraccion


        // Lista Turnos
        RevListaTurnos = view.findViewById(R.id.RevListaTurnos)

        adapterTurnos = TurnosAdapter(
            listaTurnos,
            { turnosActualizados -> actualizarTurnos(turnosActualizados) }
        )

        RevListaTurnos.layoutManager =
            LinearLayoutManager(requireContext())

        RevListaTurnos.adapter = adapterTurnos


        // Cargar datos
        cargarAtracciones()
        cargarTurnos()

        // Conectar WebSocket UNA SOLA VEZ
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

    private suspend fun obtenerReinicioAutomatico(): String {

        return try {

            val config: ConfiguracionReinicioResponse =
                ApiClient.client
                    .get("${ApiClient.BASE_URL}/turnos/configuracionReinicio")
                    .body()

            if (config.hora == 0 && config.minuto == 0) {
                return "Reinicio automático: No configurado"
            }

            var hora = config.hora
            val minuto = config.minuto

            val periodo = if (hora >= 12) "PM" else "AM"

            if (hora > 12) hora -= 12
            if (hora == 0) hora = 12

            val horaTexto = String.format("%02d:%02d %s", hora, minuto, periodo)

            "Reinicio automático: $horaTexto"

        } catch (e: Exception) {

            e.printStackTrace()

            "Reinicio automático: No configurado"
        }
    }

    private fun conectarWebSocket() {

        lifecycleScope.launch {

            try {

                ApiClient.client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = "192.168.0.182",
                    port = 8080,
                    path = "/ws/turnos"
                ) {

                    for (frame in incoming) {

                        if (frame is Frame.Text) {

                            val mensaje = frame.readText()
                            Log.d("WEBSOCKET", "Mensaje recibido: $mensaje")

                            if (mensaje == "TURNOS_UPDATED") {

                                withContext(Dispatchers.Main) {
                                    cargarTurnos()
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