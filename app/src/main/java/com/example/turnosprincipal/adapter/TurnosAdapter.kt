package com.example.turnosprincipal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.model.Turnos

class TurnosAdapter(
    private val lista: MutableList<Turnos>,
    private val onEditar: (Turnos) -> Unit
) : RecyclerView.Adapter<TurnosAdapter.TurnosAdapterViewHolder>() {

    inner class TurnosAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nombreAtraccion: TextView =
            itemView.findViewById(R.id.NombreAtraccionTurno)

        val turnoAsignado: TextView =
            itemView.findViewById(R.id.TurnoAsignado)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TurnosAdapterViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.turnos_item, parent, false)

        return TurnosAdapterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TurnosAdapterViewHolder,
        position: Int
    ) {

        val turno = lista[position]

        holder.nombreAtraccion.text = turno.nombreAtraccion
        holder.turnoAsignado.text = turno.numeroTurno
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Turnos>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}