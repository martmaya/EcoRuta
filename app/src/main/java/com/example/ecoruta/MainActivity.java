package com.example.ecoruta;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecoruta.adapters.EcoAdapter;
import com.example.ecoruta.models.Eco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<Eco> acciones = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration accionesListener;
    private EcoAdapter adapter;
    private EditText correo;
    private EditText contrasena;
    private LinearLayout panelSesion;
    private LinearLayout panelUsuario;
    private LinearLayout contenidoAcciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout contenedor = findViewById(R.id.contenedorAcciones);
        adapter = new EcoAdapter(this, contenedor, acciones, this::confirmarEliminarAccion);
        correo = findViewById(R.id.inputCorreo);
        contrasena = findViewById(R.id.inputContrasena);
        panelSesion = findViewById(R.id.panelSesion);
        panelUsuario = findViewById(R.id.panelUsuario);
        contenidoAcciones = findViewById(R.id.contenidoAcciones);

        findViewById(R.id.btnIniciarSesion).setOnClickListener(v -> autenticar(false));
        findViewById(R.id.btnCrearCuenta).setOnClickListener(v -> autenticar(true));
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> auth.signOut());
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> mostrarFormulario());

        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null) {
            findViewById(R.id.txtConfiguracion).setVisibility(View.VISIBLE);
            panelSesion.setVisibility(View.GONE);
            return;
        }

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        authListener = firebaseAuth -> actualizarEstadoSesion(firebaseAuth.getCurrentUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && authListener != null) {
            auth.addAuthStateListener(authListener);
        }
    }

    @Override
    protected void onStop() {
        if (auth != null && authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        detenerEscuchaAcciones();
        super.onStop();
    }

    private void actualizarEstadoSesion(FirebaseUser usuario) {
        boolean autenticado = usuario != null;
        panelSesion.setVisibility(autenticado ? View.GONE : View.VISIBLE);
        panelUsuario.setVisibility(autenticado ? View.VISIBLE : View.GONE);
        contenidoAcciones.setVisibility(autenticado ? View.VISIBLE : View.GONE);

        if (!autenticado) {
            detenerEscuchaAcciones();
            acciones.clear();
            adapter.actualizar();
            return;
        }

        ((TextView) findViewById(R.id.txtUsuario)).setText(usuario.getEmail());
        escucharAcciones(usuario.getUid());
    }

    private void autenticar(boolean crearCuenta) {
        String email = correo.getText().toString().trim();
        String clave = contrasena.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || clave.length() < 8) {
            Toast.makeText(this, R.string.error_correo_contrasena, Toast.LENGTH_LONG).show();
            return;
        }

        if (crearCuenta) {
            auth.createUserWithEmailAndPassword(email, clave)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, R.string.cuenta_creada, Toast.LENGTH_SHORT).show();
                            contrasena.setText("");
                        } else {
                            mostrarErrorAutenticacion();
                        }
                    });
        } else {
            auth.signInWithEmailAndPassword(email, clave)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, R.string.sesion_iniciada, Toast.LENGTH_SHORT).show();
                            contrasena.setText("");
                        } else {
                            mostrarErrorAutenticacion();
                        }
                    });
        }
    }

    private void mostrarErrorAutenticacion() {
        Toast.makeText(this, R.string.error_autenticacion, Toast.LENGTH_LONG).show();
    }

    private void mostrarFormulario() {
        LinearLayout formulario = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_registrar_eco, null);
        EditText accion = formulario.findViewById(R.id.inputAccion);
        EditText categoria = formulario.findViewById(R.id.inputCategoria);
        EditText impacto = formulario.findViewById(R.id.inputImpacto);
        EditText descripcion = formulario.findViewById(R.id.inputDescripcion);

        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_registro)
                .setView(formulario)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, (dialog, which) -> guardarAccion(
                        accion.getText().toString().trim(),
                        categoria.getText().toString().trim(),
                        impacto.getText().toString().trim(),
                        descripcion.getText().toString().trim()
                ))
                .show();
    }

    private void guardarAccion(String accion, String categoria, String impacto, String descripcion) {
        if (TextUtils.isEmpty(accion) || TextUtils.isEmpty(categoria) || TextUtils.isEmpty(impacto)) {
            Toast.makeText(this, R.string.error_campos, Toast.LENGTH_SHORT).show();
            return;
        }
        if (accion.length() > 100 || categoria.length() > 40 || impacto.length() > 100 || descripcion.length() > 500) {
            Toast.makeText(this, R.string.error_largo_campos, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) {
            actualizarEstadoSesion(null);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("uid", usuario.getUid());
        datos.put("accion", accion);
        datos.put("categoria", categoria);
        datos.put("impacto", impacto);
        datos.put("descripcion", descripcion);
        datos.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("users").document(usuario.getUid()).collection("acciones")
                .add(datos)
                .addOnSuccessListener(document -> Toast.makeText(this, R.string.accion_guardada, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG).show());
    }

    private void confirmarEliminarAccion(Eco accion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(R.string.confirmar_eliminar_mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> eliminarAccion(accion))
                .show();
    }

    private void eliminarAccion(Eco accion) {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) return;
        firestore.collection("users").document(usuario.getUid()).collection("acciones")
                .document(accion.getId())
                .delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, R.string.accion_eliminada, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, R.string.error_eliminar, Toast.LENGTH_LONG).show());
    }

    private void escucharAcciones(String uid) {
        detenerEscuchaAcciones();
        accionesListener = firestore.collection("users").document(uid).collection("acciones")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, R.string.error_cargar, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot == null) return;

                    acciones.clear();
                    for (DocumentSnapshot documento : snapshot.getDocuments()) {
                        Timestamp creado = documento.getTimestamp("createdAt");
                        String fecha = creado == null ? "" : new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(creado.toDate());
                        acciones.add(new Eco(
                                documento.getId(),
                                valor(documento, "accion"),
                                valor(documento, "categoria"),
                                fecha,
                                valor(documento, "impacto"),
                                valor(documento, "descripcion")
                        ));
                    }
                    adapter.actualizar();
                });
    }

    private String valor(DocumentSnapshot documento, String campo) {
        String valor = documento.getString(campo);
        return valor == null ? "" : valor;
    }

    private void detenerEscuchaAcciones() {
        if (accionesListener != null) {
            accionesListener.remove();
            accionesListener = null;
        }
    }
}
