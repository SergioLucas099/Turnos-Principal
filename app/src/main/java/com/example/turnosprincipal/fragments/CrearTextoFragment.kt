package com.example.turnosprincipal.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.TextosAdapter
import com.example.turnosprincipal.model.TextoGuardado
import com.example.turnosprincipal.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.contentType
import kotlinx.coroutines.launch

class CrearTextoFragment : Fragment() {

    private lateinit var editTextNAtraccion: TextInputEditText
    private lateinit var btnSubirTexto: Button
    private lateinit var btnSubirTextoAviso: Button
    private lateinit var RevTextos: RecyclerView
    private lateinit var adapter: TextosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_crear_texto,
            container,
            false
        )

        editTextNAtraccion = view.findViewById(R.id.editTextNAtraccion)
        btnSubirTexto = view.findViewById(R.id.btnSubirTexto)
        btnSubirTextoAviso = view.findViewById(R.id.btnSubirTextoAviso)
        RevTextos = view.findViewById(R.id.RevTextos)

        // 🔥 Inicializar adapter correctamente
        adapter = TextosAdapter(
            mutableListOf(),
            onActivar = { texto -> activarTexto(texto) },
            onEliminar = { texto -> eliminarTexto(texto) }
        )

        RevTextos.adapter = adapter
        RevTextos.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false)

        btnSubirTexto.setOnClickListener {

            val texto = editTextNAtraccion.text.toString().trim()

            if (texto.isEmpty()) {
                editTextNAtraccion.error = "Campo vacío"
                return@setOnClickListener
            }

            subirTexto(texto)
        }

        obtenerTextos()

        return view
    }

    private fun subirTexto(texto: String) {

        lifecycleScope.launch {

            try {

                val nuevoTexto = TextoGuardado(
                    texto = texto,
                    activo = true
                )

                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())
                ApiClient.client.post("${baseUrl}/textos") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(nuevoTexto)
                }

                Toast.makeText(
                    requireContext(),
                    "Texto guardado",
                    Toast.LENGTH_SHORT
                ).show()

                editTextNAtraccion.setText("")

                obtenerTextos()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun obtenerTextos() {

        lifecycleScope.launch {

            try {
                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())
                val lista: List<TextoGuardado> =
                    ApiClient.client.get("${baseUrl}/textos").body()
                Log.d("API", "TEXTOS: ${lista.size}")
                Log.d("API", "DATOS: $lista")

                adapter.actualizarLista(lista) // ✅ CORREGIDO

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 🔥 ACTIVAR TEXTO
    private fun activarTexto(texto: TextoGuardado) {

        lifecycleScope.launch {

            try {
                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())
                ApiClient.client.put(
                    "${baseUrl}/textos/${texto._id}/activar"
                )

                obtenerTextos()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun eliminarTexto(texto: TextoGuardado) {

        lifecycleScope.launch {

            try {
                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())
                ApiClient.client.delete(
                    "${baseUrl}/textos/${texto._id}"
                )

                obtenerTextos()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}