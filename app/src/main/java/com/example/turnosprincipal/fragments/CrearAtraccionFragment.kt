package com.example.turnosprincipal.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.VentanaPrincipal
import com.example.turnosprincipal.adapter.AtraccionAdapter
import com.example.turnosprincipal.model.Atraccion
import com.example.turnosprincipal.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.coroutines.launch

class CrearAtraccionFragment : Fragment() {

    private lateinit var atrasCrearTurnos: ImageView
    private lateinit var editTextNAtraccion: EditText
    private lateinit var txtTiempoxPersona: TextView
    private lateinit var txtMinSeg: TextView
    private lateinit var btnSubirInfo: Button
    private lateinit var btnPickerTime: Button
    private lateinit var RevListaAtraccion: RecyclerView
    private lateinit var adapter: AtraccionAdapter
    private val listaAtracciones = mutableListOf<Atraccion>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_crear_atraccion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextNAtraccion = view.findViewById(R.id.editTextNAtraccion)
        btnSubirInfo = view.findViewById(R.id.btnSubirInfo)
        RevListaAtraccion = view.findViewById(R.id.RevListaAtraccion)
        btnPickerTime = view.findViewById(R.id.btnPickerTime)
        txtTiempoxPersona = view.findViewById(R.id.txtTiempoxPersona)
        txtMinSeg = view.findViewById(R.id.txtMinSeg)

        btnPickerTime.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.dialog_tiempo, null)

            val pickerMinutos = dialogView.findViewById<NumberPicker>(R.id.pickerMinutos)
            val pickerSegundos = dialogView.findViewById<NumberPicker>(R.id.pickerSegundos)

            pickerMinutos.minValue = 0
            pickerMinutos.maxValue = 59

            pickerSegundos.minValue = 0
            pickerSegundos.maxValue = 59

            val builder = AlertDialog.Builder(view.context)
                .setTitle("Seleccionar Tiempo")
                .setView(dialogView)
                .setPositiveButton("Aceptar") { _, _ ->

                    val minutos = pickerMinutos.value
                    val segundos = pickerSegundos.value

                    val totalSegundos = minutos * 60 + segundos

                    if (totalSegundos < 60){
                        txtMinSeg.setText("Seg")
                    }else if (totalSegundos >= 60){
                        txtMinSeg.setText("Min")
                    }

                    txtTiempoxPersona.text =
                        String.format("%02d:%02d", minutos, segundos)

                    txtTiempoxPersona.tag = totalSegundos
                }
                .setNegativeButton("Cancelar", null)

            builder.show()
        }

        btnSubirInfo.setOnClickListener {
            val nombreAtrac = editTextNAtraccion.text.toString()
            val segundosAtrac = txtTiempoxPersona.tag as? Int ?: 0
            val turnoAtrac = "0"

            crearAtraccion(nombreAtrac, segundosAtrac, turnoAtrac)
        }

        adapter = AtraccionAdapter(
            listaAtracciones,
            { atraccion -> eliminarAtraccion(atraccion) },
            { atraccionActualizada -> actualizarAtraccion(atraccionActualizada) }
        )

        RevListaAtraccion.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)
        RevListaAtraccion.adapter = adapter
        cargarAtracciones()
    }

    private fun crearAtraccion(nombre: String, segundos: Int, turno: String) {

        lifecycleScope.launch {

            try {
                val nuevaAtraccion = Atraccion(
                    nombre = nombre,
                    tiempoXpersona = segundos,
                    turnoActual = turno,
                    activa = true
                )

                val atraccionCreada: Atraccion =
                    ApiClient.client.post("${ApiClient.BASE_URL}/atracciones") {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(nuevaAtraccion)
                    }.body()

                adapter.agregar(atraccionCreada)

                Toast.makeText(
                    requireContext(),
                    "Atracción creada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                editTextNAtraccion.text.clear()
                txtTiempoxPersona.setText("00:00")
                txtMinSeg.setText("Seg")

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

    private fun cargarAtracciones() {

        lifecycleScope.launch {

            try {
                val lista: List<Atraccion> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/atracciones")
                        .body()

                adapter.actualizarLista(lista)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun eliminarAtraccion(atraccion: Atraccion) {

        lifecycleScope.launch {

            try {

                ApiClient.client.delete("${ApiClient.BASE_URL}/atracciones/${atraccion._id}")

                adapter.eliminarLocal(atraccion)

                Toast.makeText(
                    requireContext(),
                    "Atracción eliminada",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarAtraccion(atraccion: Atraccion) {

        lifecycleScope.launch {

            try {

                ApiClient.client.put("${ApiClient.BASE_URL}/atracciones/${atraccion._id}") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(atraccion)
                }

                cargarAtracciones()

                Toast.makeText(requireContext(), "Actualizada", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}