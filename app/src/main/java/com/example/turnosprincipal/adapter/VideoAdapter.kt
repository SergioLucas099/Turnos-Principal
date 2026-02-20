package com.example.turnosprincipal.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.model.Multimedia

class VideoAdapter(
    private var lista: List<Multimedia>,
    private val onActivar: (Multimedia) -> Unit,
    private val onEliminar: (Multimedia) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val txtNombre: TextView =
            view.findViewById(R.id.txtNombre)

        val txtEstado: TextView =
            view.findViewById(R.id.txtEstado)

        val btnActivar: Button =
            view.findViewById(R.id.btnActivar)

        val btnEliminar: Button =
            view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)

        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder,
        position: Int
    ) {

        val video = lista[position]

        holder.txtNombre.text = video.nombre

        if (video.activo) {
            holder.txtEstado.text = "Activo"
            holder.txtEstado.setTextColor(
                holder.itemView.context.getColor(R.color.green)
            )
        } else {
            holder.txtEstado.text = "Inactivo"
            holder.txtEstado.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_red_light)
            )
        }

        holder.btnActivar.setOnClickListener {
            onActivar(video)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminar(video)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Multimedia>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}