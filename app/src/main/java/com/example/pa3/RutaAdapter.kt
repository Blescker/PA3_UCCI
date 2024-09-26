package com.example.pa3

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.pa3.API.Ruta
import com.example.pa3.API.RutaGeometry

class RutaAdapter(private val context: Context, private val dataSource: List<RutaGeometry>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.item_ruta, parent, false)

        val tvNombreUbicacion = rowView.findViewById<TextView>(R.id.tvNombreUbicacion)
        val tvFechaInicio = rowView.findViewById<TextView>(R.id.tvFechaInicio)
        val tvFechaFin = rowView.findViewById<TextView>(R.id.tvFechaFin)
        val btnDetalleRuta = rowView.findViewById<Button>(R.id.btnDetalleRuta)

        // Obtenemos el objeto RutaGeometry
        val rutaGeometry = getItem(position) as RutaGeometry

        // Accedemos a las propiedades de la ruta
        val ruta = rutaGeometry.properties

        // Asignamos los datos de 'properties' a los TextView
        tvNombreUbicacion.text = ruta.nombre_ubicacion
        tvFechaInicio.text = "Inicio: ${ruta.fecha_inicio}"
        tvFechaFin.text = "Fin: ${ruta.fecha_fin}"

        // Asignar la funcionalidad al botón "DETALLE"
        btnDetalleRuta.setOnClickListener {
            val geometry = rutaGeometry.geometry  // Acceder al campo 'geometry' que contiene el LINESTRING
            if (geometry.isNotEmpty()) {
                val intent = Intent(context, activity_detalle_ruta::class.java)
                intent.putExtra("ruta_id", ruta.id)  // Pasar el ID de la ruta
                intent.putExtra("ubicaciones", geometry)  // Pasar el LINESTRING
                context.startActivity(intent)
            } else {
                Log.e("RutaAdapter", "El campo 'geometry' es nulo o vacío.")
            }
        }

        return rowView
    }
}

