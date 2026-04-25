package com.example.turnosprincipal.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.turnosprincipal.R
import com.example.turnosprincipal.model.Multimedia
import com.example.turnosprincipal.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.launch

class CrearImagenesFragment : Fragment() {

    private lateinit var editTextNombreImagen: TextInputEditText
    private lateinit var imageView: ImageView
    private lateinit var btnSeleccImg: Button
    private lateinit var btnSubirImagen: Button
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_crear_imagenes,
            container,
            false
        )

        editTextNombreImagen = view.findViewById(R.id.editTextNombreImagen)
        imageView = view.findViewById(R.id.imageView)
        btnSeleccImg = view.findViewById(R.id.btnSeleccImg)
        btnSubirImagen = view.findViewById(R.id.btnSubirImagen)

        btnSeleccImg.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        btnSubirImagen.setOnClickListener {

            val nombre = editTextNombreImagen.text.toString().trim()

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty()) {
                editTextNombreImagen.error = "Campo requerido"
                return@setOnClickListener
            }

            subirImagen(nombre)
            cargarImagenes()
        }

        return view
    }

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                imageView.setImageURI(it)
            }
        }

    private fun subirImagen(nombre: String) {

        lifecycleScope.launch {

            try {

                val inputStream =
                    requireContext().contentResolver.openInputStream(imageUri!!)

                val bytes = inputStream!!.readBytes()
                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())

                ApiClient.client.submitFormWithBinaryData(
                    url = "${baseUrl}/multimedia/upload",
                    formData = formData {

                        append("nombre", nombre)

                        append(
                            "file",
                            bytes,
                            Headers.build {
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "filename=imagen.jpg"
                                )
                            }
                        )
                    }
                )

                Toast.makeText(
                    requireContext(),
                    "Imagen subida",
                    Toast.LENGTH_SHORT
                ).show()

                imageView.setImageDrawable(null)
                editTextNombreImagen.setText("")
                imageUri = null

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarImagenes() {
        lifecycleScope.launch {
            try {
                val baseUrl = ApiClient.BASE_URL
                    ?: ApiClient.getBaseUrl(requireContext())

                val lista: List<Multimedia> =
                    ApiClient.client.get("$baseUrl/multimedia").body()

                // actualizar adapter (igual que videos)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}