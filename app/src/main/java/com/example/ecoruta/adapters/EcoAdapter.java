package com.example.ecoruta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ecoruta.R;
import com.example.ecoruta.models.Eco;

import java.util.List;

public class EcoAdapter {
    public interface OnDeleteClickListener {
        void onDeleteClick(Eco accion);
    }

    private final Context context;
    private final LinearLayout contenedor;
    private final LayoutInflater inflater;
    private final OnDeleteClickListener deleteClickListener;

    public EcoAdapter(Context context, LinearLayout contenedor, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.contenedor = contenedor;
        this.deleteClickListener = deleteClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    public void actualizar(List<Eco> acciones) {
        contenedor.removeAllViews();
        if (acciones.isEmpty()) {
            TextView vacio = new TextView(context);
            vacio.setText(R.string.lista_vacia);
            vacio.setTextColor(context.getColor(R.color.white));
            vacio.setTextSize(14);
            vacio.setPadding(6, 14, 6, 14);
            contenedor.addView(vacio);
            return;
        }

        for (Eco accion : acciones) {
            View tarjeta = inflater.inflate(R.layout.item_eco, contenedor, false);
            ((TextView) tarjeta.findViewById(R.id.txtAccion)).setText(accion.getNombre());
            ((TextView) tarjeta.findViewById(R.id.txtCategoria)).setText(accion.getCategoriaNombre());
            ((TextView) tarjeta.findViewById(R.id.txtFecha)).setText(accion.getFecha());
            TextView descripcion = tarjeta.findViewById(R.id.txtDescripcion);
            if (accion.getDescripcion().isEmpty()) descripcion.setVisibility(View.GONE);
            else descripcion.setText(accion.getDescripcion());
            tarjeta.findViewById(R.id.btnEliminarAccion).setOnClickListener(v -> deleteClickListener.onDeleteClick(accion));
            contenedor.addView(tarjeta);
        }
    }
}
