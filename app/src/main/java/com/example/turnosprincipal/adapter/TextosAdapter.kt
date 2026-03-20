package com.example.turnosprincipal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.VideoAdapter.VideoViewHolder
import com.example.turnosprincipal.model.Multimedia
import com.example.turnosprincipal.model.TextoGuardado

class TextosAdapter (
    private var lista: MutableList<TextoGuardado>,
    private val onActivar: (TextoGuardado) -> Unit,
    private val onEliminar: (TextoGuardado) -> Unit
) : RecyclerView.Adapter<TextosAdapter.TextosViewHolder>(){

    inner class TextosViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtNombreTexto: TextView =
            view.findViewById(R.id.txtNombreTexto)

        val txtEstadoTexto: TextView =
            view.findViewById(R.id.txtEstadoTexto)

        val btnActivarTexto: Button =
            view.findViewById(R.id.btnActivarTexto)

        val btnEliminarTexto: Button =
            view.findViewById(R.id.btnEliminarTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextosViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_textos, parent, false)

        return TextosViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TextosViewHolder,
        position: Int
    ) {
        val texto: TextoGuardado = lista[position]

        holder.txtNombreTexto.text = texto.texto

        if (texto.activo) {
            holder.txtEstadoTexto.text = "Activo"
            holder.txtEstadoTexto.setTextColor(
                holder.itemView.context.getColor(R.color.green)
            )
        } else {
            holder.txtEstadoTexto.text = "Inactivo"
            holder.txtEstadoTexto.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_red_light)
            )
        }

        holder.btnActivarTexto.setOnClickListener {
            onActivar(texto)
        }

        holder.btnEliminarTexto.setOnClickListener {
            onEliminar(texto)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<TextoGuardado>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}