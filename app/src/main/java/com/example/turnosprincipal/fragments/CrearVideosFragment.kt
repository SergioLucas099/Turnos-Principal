package com.example.turnosprincipal.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.VideoAdapter
import com.example.turnosprincipal.model.Multimedia
import com.example.turnosprincipal.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.delete
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.*
import kotlinx.coroutines.launch
import java.io.InputStream

class CrearVideosFragment : Fragment() {

    private lateinit var videoView: VideoView
    private lateinit var btnSeleccionar: Button
    private lateinit var btnSubir: Button
    private lateinit var switchSonido: Switch
    private lateinit var editTextNombreVideo: TextInputEditText
    private lateinit var RevListaVideos: RecyclerView
    private lateinit var adapter: VideoAdapter

    private var videoUri: Uri? = null

    private val videoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {
                videoUri = it

                videoView.setVideoURI(it)

                val mediaController =
                    MediaController(requireContext())

                mediaController.setAnchorView(videoView)
                videoView.setMediaController(mediaController)

                videoView.start()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_crear_videos,
            container,
            false
        )

        videoView = view.findViewById(R.id.videoView)
        btnSeleccionar = view.findViewById(R.id.btnSeleccVideo)
        switchSonido = view.findViewById(R.id.switchSonido)
        editTextNombreVideo = view.findViewById(R.id.editTextNombreVideo)
        RevListaVideos = view.findViewById(R.id.RevListaVideos)
        btnSubir = view.findViewById(R.id.btnSubirVideo)

        btnSeleccionar.setOnClickListener {
            seleccionarVideo()
        }

        btnSubir.setOnClickListener {
            videoUri?.let { subirVideo(it) }
                ?: Toast.makeText(
                    requireContext(),
                    "Selecciona un video primero",
                    Toast.LENGTH_SHORT
                ).show()
        }

        adapter = VideoAdapter(
            emptyList(),
            onActivar = { video -> activarVideo(video) },
            onEliminar = { video -> eliminarVideo(video) }
        )

        RevListaVideos.layoutManager =
            LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false)

        RevListaVideos.adapter = adapter
        RevListaVideos.setHasFixedSize(true)

        cargarVideos()

        return view
    }

    private fun seleccionarVideo() {
        videoPicker.launch("video/mp4")
    }

    private fun subirVideo(uri: Uri) {

        btnSubir.isEnabled = false

        lifecycleScope.launch {

            try {

                val inputStream: InputStream? =
                    requireContext()
                        .contentResolver
                        .openInputStream(uri)

                val bytes = inputStream?.use { it.readBytes() }

                if (bytes == null) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo leer el archivo",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val nombreVideo = editTextNombreVideo.text.toString().trim()

                if (nombreVideo.isEmpty()) {
                    editTextNombreVideo.error = "Ingrese un nombre"
                    btnSubir.isEnabled = true
                    return@launch
                }

                ApiClient.client.submitFormWithBinaryData(
                    url = "${ApiClient.BASE_URL}/multimedia/upload",
                    formData = formData {

                        // 🔹 Archivo
                        append(
                            key = "file",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "video/mp4")
                                append(HttpHeaders.ContentDisposition, "filename=\"video.mp4\"")
                            }
                        )

                        // 🔹 Nombre del video (FORM ITEM correcto)
                        append(
                            "nombre",
                            nombreVideo
                        )

                        // 🔹 Sonido
                        append(
                            "sonido",
                            switchSonido.isChecked.toString()
                        )
                    }
                )

                Toast.makeText(
                    requireContext(),
                    "Video subido correctamente",
                    Toast.LENGTH_LONG
                ).show()

                editTextNombreVideo.setText("")

                cargarVideos()
                videoUri = null
                videoView.stopPlayback()

            } catch (e: Exception) {

                Toast.makeText(
                    requireContext(),
                    "Error al subir: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                btnSubir.isEnabled = true
            }
        }
    }

    private fun cargarVideos() {

        lifecycleScope.launch {

            try {

                val lista: List<Multimedia> =
                    ApiClient.client.get(
                        "${ApiClient.BASE_URL}/multimedia"
                    ).body()

                adapter.actualizarLista(lista)
                println("VIDEOS CARGADOS: ${lista.size}")

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error cargando videos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun activarVideo(video: Multimedia) {

        lifecycleScope.launch {

            try {
                val response = ApiClient.client.put(
                    "${ApiClient.BASE_URL}/multimedia/${video._id}/activar"
                )
                println("ACTIVANDO ID: ${video._id}")
                println("STATUS ACTIVAR: ${response.status}")
                cargarVideos()
                Toast.makeText(
                    requireContext(),
                    "Video activado",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al activar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun eliminarVideo(video: Multimedia) {

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Video")
            .setMessage("¿Estás seguro que deseas eliminar \"${video.nombre}\"?")
            .setPositiveButton("Eliminar") { _, _ ->

                lifecycleScope.launch {
                    try {
                        val response = ApiClient.client.delete(
                            "${ApiClient.BASE_URL}/multimedia/${video._id}"
                        )

                        println("ELIMINANDO ID: ${video._id}")
                        println("STATUS ELIMINAR: ${response.status}")

                        cargarVideos()

                        Toast.makeText(
                            requireContext(),
                            "Video eliminado",
                            Toast.LENGTH_SHORT
                        ).show()

                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al eliminar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}