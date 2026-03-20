package com.example.turnosprincipal.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.model.Turnos
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        val personasTurno: TextView =
            itemView.findViewById(R.id.personasTurno)
        val tiempoTurno: TextView =
            itemView.findViewById(R.id.tiempoTurno)
        val TurnoFinalizado: LinearLayout =
            itemView.findViewById(R.id.TurnoFinalizado)
        val TurnoCancelado: LinearLayout =
            itemView.findViewById(R.id.TurnoCancelado)
        val TurnoEspera: LinearLayout =
            itemView.findViewById(R.id.TurnoEspera)
        val TurnoAprobado: LinearLayout =
            itemView.findViewById(R.id.TurnoAprobado)
        val ContenidoAtraccion: ConstraintLayout =
            itemView.findViewById(R.id.ContenidoAtraccion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TurnosAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.turnos_item, parent, false)

        return TurnosAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: TurnosAdapterViewHolder,
        position: Int) {

        val turno = lista[position]

        holder.nombreAtraccion.text = turno.nombreAtraccion
        holder.turnoAsignado.text = turno.numeroTurno
        holder.personasTurno.text = turno.numeroPersonas.toString()
        holder.tiempoTurno.text = formatearTiempo(turno.duracion)

        if (turno.estado == "ESPERA") {
            holder.TurnoEspera.visibility = View.VISIBLE
            holder.TurnoFinalizado.visibility = View.INVISIBLE
            holder.TurnoCancelado.visibility = View.INVISIBLE
            holder.TurnoAprobado.visibility = View.INVISIBLE
        } else if (turno.estado == "FINALIZADO") {
            holder.TurnoEspera.visibility = View.INVISIBLE
            holder.TurnoFinalizado.visibility = View.VISIBLE
            holder.TurnoCancelado.visibility = View.INVISIBLE
            holder.TurnoAprobado.visibility = View.INVISIBLE
        } else if (turno.estado == "APROBADO") {
            holder.TurnoEspera.visibility = View.INVISIBLE
            holder.TurnoFinalizado.visibility = View.INVISIBLE
            holder.TurnoCancelado.visibility = View.INVISIBLE
            holder.TurnoAprobado.visibility = View.VISIBLE
        } else if (turno.estado == "CANCELADO") {
            holder.TurnoEspera.visibility = View.INVISIBLE
            holder.TurnoFinalizado.visibility = View.INVISIBLE
            holder.TurnoCancelado.visibility = View.VISIBLE
            holder.TurnoAprobado.visibility = View.INVISIBLE
        }

        holder.ContenidoAtraccion.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_confirmar_info, null)

            val txtDatosAtraccionTurno = dialogView.findViewById<TextView>(R.id.txtDatosAtraccionTurno)
            txtDatosAtraccionTurno.text = turno.nombreAtraccion

            val txtDatosNumeroTurno = dialogView.findViewById<TextView>(R.id.txtDatosNumeroTurno)
            txtDatosNumeroTurno.text = turno.numeroTurno

            val txtDatosPersonasTurno = dialogView.findViewById<TextView>(R.id.txtDatosPersonasTurno)
            txtDatosPersonasTurno.text = turno.numeroPersonas.toString()

            val txtDatosTelefonoTurno = dialogView.findViewById<TextView>(R.id.txtDatosTelefonoTurno)
            txtDatosTelefonoTurno.text = turno.telefono
            if (turno.telefono.isEmpty()) {
                txtDatosTelefonoTurno.text = "No registrado"
            }

            val txtDatosTiempoTurno = dialogView.findViewById<TextView>(R.id.txtDatosTiempoTurno)
            txtDatosTiempoTurno.text = formatearTiempo(turno.duracion)

            val txtDatosTiempoEsperaTurno = dialogView.findViewById<TextView>(R.id.txtDatosTiempoEsperaTurno)
            txtDatosTiempoEsperaTurno.text = formatearTiempo(turno.tiempoEspera)

            val txtDatosFechaTurno = dialogView.findViewById<TextView>(R.id.txtDatosFechaTurno)
            val fecha = LocalDateTime.parse(turno.fecha)

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")

            txtDatosFechaTurno.text = fecha
                .format(formatter)
                .replace("AM","a.m")
                .replace("PM","p.m")

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setView(dialogView)
                .setPositiveButton("Aceptar") { dialog, _ ->
                    // acción si el usuario confirma
                    dialog.dismiss()
                }

            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Turnos>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    private fun formatearTiempo(segundos: Int): String {

        val horas = segundos / 3600
        val minutos = (segundos % 3600) / 60
        val seg = segundos % 60

        return when {
            horas > 0 -> String.format("%02d:%02d hr", horas, minutos)
            minutos > 0 -> String.format("%02d:%02d min", minutos, seg)
            else -> String.format("00:%02d seg", seg)
        }
    }
}