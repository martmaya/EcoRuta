package com.example.ecoruta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ecoruta.R;
import com.example.ecoruta.models.Eco;

import java.util.List;

/** Presenta las acciones ecológicas registradas en la lista de la pantalla. */
public class EcoAdapter {
    public interface OnDeleteClickListener {
        void onDeleteClick(Eco accion);
    }

    private final Context context;
    private final LinearLayout contenedor;
    private final List<Eco> acciones;
    private final LayoutInflater inflater;
    private final OnDeleteClickListener deleteClickListener;

    public EcoAdapter(Context context, LinearLayout contenedor, List<Eco> acciones, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.contenedor = contenedor;
        this.acciones = acciones;
        this.deleteClickListener = deleteClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    public void actualizar() {
        contenedor.removeAllViews();
        if (acciones.isEmpty()) {
            TextView vacio = new TextView(context);
            vacio.setText(R.string.lista_vacia);
            vacio.setTextSize(16);
            vacio.setPadding(8, 24, 8, 24);
            contenedor.addView(vacio);
            return;
        }

        for (Eco accion : acciones) {
            View tarjeta = inflater.inflate(R.layout.item_eco, contenedor, false);
            ((TextView) tarjeta.findViewById(R.id.txtAccion)).setText(accion.getAccion());
            ((TextView) tarjeta.findViewById(R.id.txtCategoria)).setText(context.getString(R.string.etiqueta_categoria, accion.getCategoria()));
            ((TextView) tarjeta.findViewById(R.id.txtFecha)).setText(context.getString(R.string.etiqueta_fecha, accion.getFecha()));
            ((TextView) tarjeta.findViewById(R.id.txtImpacto)).setText(context.getString(R.string.etiqueta_impacto, accion.getImpacto()));
            TextView descripcion = tarjeta.findViewById(R.id.txtDescripcion);
            if (accion.getDescripcion().isEmpty()) {
                descripcion.setVisibility(View.GONE);
            } else {
                descripcion.setText(accion.getDescripcion());
            }
            tarjeta.findViewById(R.id.btnEliminarAccion).setOnClickListener(v -> deleteClickListener.onDeleteClick(accion));
            contenedor.addView(tarjeta);
        }
    }
}
