package com.example.turnosprincipal.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosprincipal.R
import com.example.turnosprincipal.model.Atraccion

class AtraccionAdapter(
    private val lista: MutableList<Atraccion>,
    private val onEliminar: (Atraccion) -> Unit,
    private val onEditar: (Atraccion) -> Unit
) : RecyclerView.Adapter<AtraccionAdapter.AtraccionViewHolder>() {

    inner class AtraccionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nombre: TextView = itemView.findViewById(R.id.nombreAtraccion)
        val btnEliminar: ImageView = itemView.findViewById(R.id.btnEliminarAtraccion)
        val SeleccionarAtraccion: ConstraintLayout = itemView.findViewById(R.id.SeleccionarAtraccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtraccionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.atraccion_item, parent, false)
        return AtraccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AtraccionViewHolder, position: Int) {

        val atraccion = lista[position]

        holder.nombre.text = atraccion.nombre

        holder.btnEliminar.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar")
                .setMessage("¿Deseas eliminar esta atracción?")
                .setPositiveButton("Sí") { _, _ ->
                    onEliminar(atraccion)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.SeleccionarAtraccion.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_editar_atraccion, null)

            val EditTextNomAtracc = dialogView.findViewById<EditText>(R.id.EditTextNomAtracc)
            EditTextNomAtracc.setText(atraccion.nombre)

            val EditTextTurnoAtracc = dialogView.findViewById<EditText>(R.id.EditTextTurnoAtracc)
            EditTextTurnoAtracc.setText(atraccion.turnoActual)

            val TextTiempoxPersona = dialogView.findViewById<TextView>(R.id.TextTiempoxPersona)
            val totalSegundos = atraccion.tiempoXpersona

            val minutos = totalSegundos / 60
            val segundos = totalSegundos % 60

            TextTiempoxPersona.text = String.format("%02d:%02d", minutos, segundos)
            TextTiempoxPersona.tag = totalSegundos

            val txtMinSegEdit = dialogView.findViewById<TextView>(R.id.txtMinSegEdit)

            if (totalSegundos < 60) {
                txtMinSegEdit.text = "Seg"
            } else {
                txtMinSegEdit.text = "Min"
            }

            val btnTimeEdit = dialogView.findViewById<ImageView>(R.id.btnTimeEdit)

            btnTimeEdit.setOnClickListener {

                val dialogView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.dialog_tiempo, null)

                val pickerMinutos = dialogView.findViewById<NumberPicker>(R.id.pickerMinutos)
                val pickerSegundos = dialogView.findViewById<NumberPicker>(R.id.pickerSegundos)

                pickerMinutos.minValue = 0
                pickerMinutos.maxValue = 59

                pickerSegundos.minValue = 0
                pickerSegundos.maxValue = 59

                val totalActual = TextTiempoxPersona.tag as? Int ?: atraccion.tiempoXpersona
                pickerMinutos.value = totalActual / 60
                pickerSegundos.value = totalActual % 60

                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Seleccionar Tiempo")
                    .setView(dialogView)
                    .setPositiveButton("Aceptar"){_, _ ->

                        val minutos = pickerMinutos.value
                        val segundos = pickerSegundos.value

                        val totalSegundos = minutos * 60 + segundos

                        if (totalSegundos < 60) {
                            txtMinSegEdit.text = "Seg"
                        } else {
                            txtMinSegEdit.text = "Min"
                        }

                        TextTiempoxPersona.text =
                            String.format("%02d:%02d", minutos, segundos)

                        TextTiempoxPersona.tag = totalSegundos
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()

            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            builder.setView(dialogView)
                .setPositiveButton("Aceptar", null)

            .setNegativeButton("Cancelar"){ dialog, _ ->
                    dialog.dismiss()
                }

            val dialog = builder.create()
            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val nuevoNombre = EditTextNomAtracc.text.toString()
                val nuevoTurno = EditTextTurnoAtracc.text.toString()
                val nuevoTiempo = TextTiempoxPersona.tag as? Int ?: atraccion.tiempoXpersona

                if (nuevoNombre.isBlank() || nuevoTurno.isBlank()) {
                    Toast.makeText(holder.itemView.context, "Campos vacíos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val atraccionActualizada = atraccion.copy(
                    nombre = nuevoNombre,
                    turnoActual = nuevoTurno,
                    tiempoXpersona = nuevoTiempo
                )

                onEditar(atraccionActualizada)

                dialog.dismiss()
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Atraccion>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun eliminarLocal(atraccion: Atraccion) {
        lista.remove(atraccion)
        notifyDataSetChanged()
    }

    fun agregar(atraccion: Atraccion) {
        lista.add(atraccion)
        notifyItemInserted(lista.size - 1)
    }
}
