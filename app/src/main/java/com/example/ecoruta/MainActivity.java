package com.example.ecoruta;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowCompat;

import com.example.ecoruta.models.Eco;
import com.example.ecoruta.models.EcoCategory;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<EcoCategory> categorias = new ArrayList<>();
    private final ArrayList<Eco> acciones = new ArrayList<>();
    private final ExecutorService avatarExecutor = Executors.newSingleThreadExecutor();

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseAuth.AuthStateListener authListener;
    private ListenerRegistration categoriasListener;
    private ListenerRegistration accionesListener;
    private EditText correo;
    private EditText contrasena;
    private EditText correoRegistro;
    private EditText contrasenaRegistro;
    private EditText confirmarContrasenaRegistro;
    private LinearLayout contenedorCategorias;
    private FrameLayout screenLogin;
    private FrameLayout screenSignup;
    private LinearLayout screenHome;
    private LinearLayout screenRegister;
    private LinearLayout screenProfile;
    private LinearLayout navBottom;
    private TextView btnMostrarClave;
    private TextView txtTotalAcciones;
    private OnBackPressedCallback signupBackCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enlazarVistas();
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().getDecorView().setForceDarkAllowed(false);
        }
        signupBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                volverALogin();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, signupBackCallback);
        configurarNavegacion();

        FirebaseApp app = FirebaseApp.initializeApp(this);
        if (app == null) {
            findViewById(R.id.txtConfiguracion).setVisibility(View.VISIBLE);
            findViewById(R.id.btnIniciarSesion).setEnabled(false);
            findViewById(R.id.btnIrCrearCuenta).setEnabled(false);
            findViewById(R.id.btnCrearCuenta).setEnabled(false);
            return;
        }

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        authListener = firebaseAuth -> actualizarEstadoSesion(firebaseAuth.getCurrentUser());
        findViewById(R.id.btnIniciarSesion).setOnClickListener(v -> autenticar(false));
        findViewById(R.id.btnIrCrearCuenta).setOnClickListener(v -> mostrarPantalla(screenSignup, false));
        findViewById(R.id.btnCrearCuenta).setOnClickListener(v -> autenticar(true));
        findViewById(R.id.btnVolverALogin).setOnClickListener(v -> volverALogin());
        findViewById(R.id.btnVolverALoginTexto).setOnClickListener(v -> volverALogin());
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> auth.signOut());
        findViewById(R.id.btnNavInicio).setOnClickListener(v -> mostrarInicio());
        findViewById(R.id.btnNavRegistrar).setOnClickListener(v -> mostrarMenuRegistro());
        findViewById(R.id.btnNavPerfil).setOnClickListener(v -> mostrarPerfil());
        findViewById(R.id.cardNuevaCategoria).setOnClickListener(v -> mostrarFormularioCategoria());
        findViewById(R.id.cardNuevaAccion).setOnClickListener(v -> mostrarFormularioAccion());
        findViewById(R.id.btnMostrarClave).setOnClickListener(v -> alternarVisibilidadContrasena());
    }

    private void enlazarVistas() {
        screenLogin = findViewById(R.id.screenLogin);
        screenSignup = findViewById(R.id.screenSignup);
        screenHome = findViewById(R.id.screenHome);
        screenRegister = findViewById(R.id.screenRegister);
        screenProfile = findViewById(R.id.screenProfile);
        navBottom = findViewById(R.id.navBottom);
        correo = findViewById(R.id.inputCorreo);
        contrasena = findViewById(R.id.inputContrasena);
        correoRegistro = findViewById(R.id.inputCorreoRegistro);
        contrasenaRegistro = findViewById(R.id.inputContrasenaRegistro);
        confirmarContrasenaRegistro = findViewById(R.id.inputConfirmarContrasenaRegistro);
        btnMostrarClave = findViewById(R.id.btnMostrarClave);
        contenedorCategorias = findViewById(R.id.contenedorCategorias);
        txtTotalAcciones = findViewById(R.id.txtTotalAcciones);
        configurarIlustracionHero();
    }

    private void configurarIlustracionHero() {
        configurarRecorteTierra(findViewById(R.id.imgEarthHero));
    }

    private void configurarRecorteTierra(ImageView tierra) {
        tierra.addOnLayoutChangeListener((view, left, top, right, bottom,
                                          oldLeft, oldTop, oldRight, oldBottom) -> ajustarImagenTierra(tierra));
        tierra.post(() -> ajustarImagenTierra(tierra));
    }

    private void ajustarImagenTierra(ImageView tierra) {
        if (tierra.getWidth() == 0 || tierra.getHeight() == 0) return;
        Drawable drawable = tierra.getDrawable();
        if (!(drawable instanceof BitmapDrawable)) return;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        RectF areaTierra = new RectF(bitmap.getWidth() * 0.18f, bitmap.getHeight() * 0.12f,
                bitmap.getWidth() * 0.80f, bitmap.getHeight() * 0.52f);
        Matrix matriz = new Matrix();
        matriz.setRectToRect(areaTierra,
                new RectF(0, 0, tierra.getWidth(), tierra.getHeight()), Matrix.ScaleToFit.CENTER);
        tierra.setImageMatrix(matriz);
    }

    private void configurarNavegacion() {
        // Los elementos de navegación permanecen fijos mientras cambia el contenido principal.
        navBottom.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth != null && authListener != null) auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        if (auth != null && authListener != null) auth.removeAuthStateListener(authListener);
        detenerEscuchasDeDatos();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        avatarExecutor.shutdownNow();
        super.onDestroy();
    }

    private void actualizarEstadoSesion(FirebaseUser usuario) {
        if (usuario == null) {
            detenerEscuchasDeDatos();
            categorias.clear();
            acciones.clear();
            renderizarCategorias();
            renderizarContadorAcciones();
            mostrarPantalla(screenLogin, false);
            return;
        }

        mostrarPantalla(screenHome, true);
        escucharDatosDelUsuario(usuario.getUid());
    }

    private void escucharDatosDelUsuario(String uid) {
        detenerEscuchasDeDatos();
        ListenerRegistration nuevaEscuchaCategorias = firestore.collection("users").document(uid)
                .collection("categorias").orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, R.string.error_cargar, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot == null) return;
                    categorias.clear();
                    for (DocumentSnapshot documento : snapshot.getDocuments()) {
                        categorias.add(new EcoCategory(documento.getId(), valor(documento, "nombre"),
                                valor(documento, "descripcion"), valor(documento, "icono")));
                    }
                    renderizarCategorias();
                });
        categoriasListener = nuevaEscuchaCategorias;

        ListenerRegistration nuevaEscuchaAcciones = firestore.collection("users").document(uid)
                .collection("acciones").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, R.string.error_cargar, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot == null) return;
                    acciones.clear();
                    for (DocumentSnapshot documento : snapshot.getDocuments()) {
                        Timestamp creada = documento.getTimestamp("createdAt");
                        String fecha = creada == null
                                ? valor(documento, "fecha")
                                : new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(creada.toDate());
                        String categoriaNombre = valor(documento, "categoriaNombre");
                        if (categoriaNombre.isEmpty()) categoriaNombre = valor(documento, "categoria");
                        String categoriaId = valor(documento, "categoriaId");
                        if (categoriaId.isEmpty()) categoriaId = resolverCategoriaAnterior(categoriaNombre);
                        String nombre = valor(documento, "nombre");
                        if (nombre.isEmpty()) nombre = valor(documento, "accion");
                        acciones.add(new Eco(documento.getId(), nombre, valor(documento, "descripcion"),
                                categoriaId, categoriaNombre, fecha));
                    }
                    renderizarContadorAcciones();
                });
        accionesListener = nuevaEscuchaAcciones;
        crearCategoriasInicialesSiFaltan(uid);
    }

    private void detenerEscuchasDeDatos() {
        if (categoriasListener != null) {
            categoriasListener.remove();
            categoriasListener = null;
        }
        if (accionesListener != null) {
            accionesListener.remove();
            accionesListener = null;
        }
    }

    private void crearCategoriasInicialesSiFaltan(String uid) {
        String[][] iniciales = {
                {"default_ordenar", "Ordenar", "Separa y organiza tus residuos.", "ordenar"},
                {"default_reciclar", "Reciclar", "Dale una nueva vida a los materiales.", "reciclar"},
                {"default_ahorro", "Ahorro", "Ahorra agua y energía cada día.", "ahorro"}
        };
        for (String[] categoria : iniciales) {
            firestore.collection("users").document(uid).collection("categorias").document(categoria[0])
                    .get().addOnSuccessListener(documento -> {
                        if (documento.exists()) return;
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("uid", uid);
                        datos.put("nombre", categoria[1]);
                        datos.put("descripcion", categoria[2]);
                        datos.put("icono", categoria[3]);
                        datos.put("createdAt", FieldValue.serverTimestamp());
                        firestore.collection("users").document(uid).collection("categorias")
                                .document(categoria[0]).set(datos)
                                .addOnFailureListener(e -> Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, R.string.error_cargar, Toast.LENGTH_LONG).show());
        }
    }

    private String resolverCategoriaAnterior(String nombre) {
        String normalizado = nombre.toLowerCase(Locale.ROOT);
        if (normalizado.contains("orden")) return "default_ordenar";
        if (normalizado.contains("recicl")) return "default_reciclar";
        if (normalizado.contains("ahorr")) return "default_ahorro";
        return "";
    }

    private void renderizarCategorias() {
        contenedorCategorias.removeAllViews();
        if (categorias.isEmpty()) {
            TextView esperando = texto(getString(R.string.categorias_cargando), 13, Color.WHITE);
            contenedorCategorias.addView(esperando);
            return;
        }
        LinearLayout fila = null;
        for (int index = 0; index < categorias.size(); index++) {
            if (index % 3 == 0) {
                fila = new LinearLayout(this);
                fila.setOrientation(LinearLayout.HORIZONTAL);
                fila.setGravity(Gravity.CENTER);
                contenedorCategorias.addView(fila, new LinearLayout.LayoutParams(-1, -2));
            }
            EcoCategory categoria = categorias.get(index);
            MaterialCardView tarjeta = new MaterialCardView(this);
            tarjeta.setRadius(dp(18));
            tarjeta.setCardElevation(dp(2));
            tarjeta.setStrokeWidth(dp(1));
            tarjeta.setStrokeColor(getColor(R.color.eco_canvas));
            tarjeta.setCardBackgroundColor(getColor(R.color.white));
            tarjeta.setClickable(true);
            tarjeta.setFocusable(true);

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(10), dp(4), dp(10));
            tarjeta.addView(item, new ViewGroup.LayoutParams(-1, -2));

            FrameLayout circulo = new FrameLayout(this);
            GradientDrawable circleBackground = new GradientDrawable();
            circleBackground.setShape(GradientDrawable.OVAL);
            circleBackground.setColor(getColor(R.color.eco_canvas));
            circulo.setBackground(circleBackground);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dp(58), dp(58));
            circulo.setLayoutParams(circleParams);

            int iconResource = iconoVectorial(categoria.getIcono());
            if (iconResource != 0) {
                ImageView icono = new ImageView(this);
                icono.setImageResource(iconResource);
                icono.setContentDescription(categoria.getNombre());
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER);
                circulo.addView(icono, imageParams);
            } else {
                TextView icono = texto(iconoEmoji(categoria.getIcono()), 26, getColor(R.color.eco_green));
                icono.setGravity(Gravity.CENTER);
                circulo.addView(icono, new FrameLayout.LayoutParams(-1, -1));
            }

            TextView nombre = texto(categoria.getNombre(), 12, getColor(R.color.eco_navy));
            nombre.setTypeface(null, Typeface.BOLD);
            nombre.setGravity(Gravity.CENTER);
            nombre.setMaxLines(2);
            nombre.setPadding(0, dp(8), 0, 0);
            item.addView(circulo);
            item.addView(nombre, new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));
            tarjeta.setContentDescription(getString(R.string.abrir_categoria, categoria.getNombre()));
            tarjeta.setOnClickListener(v -> mostrarDetalleCategoria(categoria));
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            itemParams.setMargins(dp(5), dp(4), dp(5), dp(8));
            fila.addView(tarjeta, itemParams);
        }
    }

    private int iconoVectorial(String icono) {
        switch (icono) {
            case "ordenar": return R.drawable.ic_category_sort;
            case "reciclar": return R.drawable.ic_category_recycle;
            case "ahorro": return R.drawable.ic_category_saving;
            default: return 0;
        }
    }

    private String iconoEmoji(String icono) {
        switch (icono) {
            case "water": return "💧";
            case "recycle": return "♻️";
            case "bike": return "🚲";
            case "sun": return "☀️";
            case "tree": return "🌳";
            default: return "🌿";
        }
    }

    private void renderizarContadorAcciones() {
        txtTotalAcciones.setText(String.valueOf(acciones.size()));
    }

    private void mostrarInicio() {
        mostrarPantalla(screenHome, true);
    }

    private void mostrarPerfil() {
        FirebaseUser usuario = auth == null ? null : auth.getCurrentUser();
        if (usuario == null) return;
        mostrarPantalla(screenProfile, true);

        TextView nombre = findViewById(R.id.txtNombrePerfil);
        if (!TextUtils.isEmpty(usuario.getDisplayName())) {
            nombre.setText(usuario.getDisplayName());
            nombre.setVisibility(View.VISIBLE);
        } else {
            nombre.setVisibility(View.GONE);
        }
        TextView correoPerfil = findViewById(R.id.txtCorreoPerfil);
        correoPerfil.setText(TextUtils.isEmpty(usuario.getEmail()) ? getString(R.string.sin_correo) : usuario.getEmail());
        ImageView foto = findViewById(R.id.imgFotoPerfil);
        foto.setImageResource(R.drawable.ic_nav_profile);
        Uri fotoUri = usuario.getPhotoUrl();
        if (fotoUri != null && "https".equalsIgnoreCase(fotoUri.getScheme())) cargarFotoPerfil(fotoUri, foto);
    }

    private void cargarFotoPerfil(Uri uri, ImageView destino) {
        avatarExecutor.execute(() -> {
            HttpURLConnection conexion = null;
            try {
                conexion = (HttpURLConnection) new URL(uri.toString()).openConnection();
                conexion.setConnectTimeout(5000);
                conexion.setReadTimeout(5000);
                conexion.setInstanceFollowRedirects(true);
                conexion.connect();
                Bitmap bitmap = BitmapFactory.decodeStream(conexion.getInputStream());
                if (bitmap != null) runOnUiThread(() -> destino.setImageBitmap(bitmap));
            } catch (Exception ignored) {
                // Si la foto no está disponible, conserva el icono de perfil.
            } finally {
                if (conexion != null) conexion.disconnect();
            }
        });
    }

    private void alternarVisibilidadContrasena() {
        int cursor = contrasena.getSelectionStart();
        boolean visible = contrasena.getTransformationMethod() instanceof PasswordTransformationMethod;
        contrasena.setTransformationMethod(visible
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
        btnMostrarClave.setText(visible ? R.string.ocultar : R.string.mostrar);
        if (cursor >= 0) contrasena.setSelection(Math.min(cursor, contrasena.length()));
    }

    private void autenticar(boolean crearCuenta) {
        EditText campoCorreo = crearCuenta ? correoRegistro : correo;
        EditText campoClave = crearCuenta ? contrasenaRegistro : contrasena;
        String email = campoCorreo.getText().toString().trim();
        String clave = campoClave.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || clave.length() < 8) {
            Toast.makeText(this, R.string.error_correo_contrasena, Toast.LENGTH_LONG).show();
            return;
        }
        if (crearCuenta) {
            String confirmacion = confirmarContrasenaRegistro.getText().toString();
            if (!clave.equals(confirmacion)) {
                confirmarContrasenaRegistro.setError(getString(R.string.contrasenas_no_coinciden));
                return;
            }
            auth.createUserWithEmailAndPassword(email, clave).addOnCompleteListener(this, tarea -> {
                if (tarea.isSuccessful()) {
                    contrasenaRegistro.setText("");
                    confirmarContrasenaRegistro.setText("");
                    Toast.makeText(this, R.string.cuenta_creada, Toast.LENGTH_SHORT).show();
                } else mostrarErrorAutenticacion();
            });
        } else {
            auth.signInWithEmailAndPassword(email, clave).addOnCompleteListener(this, tarea -> {
                if (tarea.isSuccessful()) {
                    contrasena.setText("");
                    Toast.makeText(this, R.string.sesion_iniciada, Toast.LENGTH_SHORT).show();
                } else mostrarErrorAutenticacion();
            });
        }
    }

    private void volverALogin() {
        mostrarPantalla(screenLogin, false);
    }

    private void mostrarErrorAutenticacion() {
        Toast.makeText(this, R.string.error_autenticacion, Toast.LENGTH_LONG).show();
    }

    private void mostrarMenuRegistro() {
        mostrarPantalla(screenRegister, true);
    }

    private void agregarOpcionRegistro(LinearLayout contenedor, String icono, int titulo, int descripcion, Runnable accion) {
        MaterialCardView tarjeta = new MaterialCardView(this);
        tarjeta.setRadius(dp(18));
        tarjeta.setCardElevation(dp(1));
        tarjeta.setStrokeWidth(dp(1));
        tarjeta.setStrokeColor(getColor(R.color.eco_canvas));
        tarjeta.setCardBackgroundColor(getColor(R.color.white));
        tarjeta.setClickable(true);
        tarjeta.setFocusable(true);
        tarjeta.setOnClickListener(v -> accion.run());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(12);
        contenedor.addView(tarjeta, cardParams);

        LinearLayout fila = new LinearLayout(this);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(dp(14), dp(14), dp(14), dp(14));
        tarjeta.addView(fila);
        TextView iconoView = texto(icono, 28, getColor(R.color.eco_green));
        iconoView.setGravity(Gravity.CENTER);
        fila.addView(iconoView, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -2, 1);
        textParams.leftMargin = dp(12);
        fila.addView(textos, textParams);
        TextView title = texto(getString(titulo), 16, getColor(R.color.eco_navy));
        title.setTypeface(null, Typeface.BOLD);
        textos.addView(title);
        TextView detail = texto(getString(descripcion), 13, getColor(R.color.eco_muted));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(4);
        textos.addView(detail, descParams);
    }

    private void mostrarFormularioCategoria() {
        mostrarFormularioCategoria(null);
    }

    private void mostrarFormularioCategoria(EcoCategory categoriaEditar) {
        BottomSheetDialog dialogo = nuevoBottomSheet();
        LinearLayout contenido = crearContenidoModal();
        agregarEncabezado(contenido, getString(categoriaEditar == null
                ? R.string.nueva_categoria : R.string.editar_categoria), dialogo::dismiss);

        EditText nombre = campo(getString(R.string.hint_categoria_nombre), 40, false);
        EditText descripcion = campo(getString(R.string.hint_categoria_descripcion), 160, false);
        Spinner selectorIcono = new Spinner(this);
        String[] etiquetasIconos = {
                getString(R.string.icon_hoja), getString(R.string.icon_agua), getString(R.string.icon_reciclar),
                getString(R.string.icon_bici), getString(R.string.icon_sol), getString(R.string.icon_arbol),
                getString(R.string.icon_ordenar), getString(R.string.icon_reciclar), getString(R.string.icon_ahorro)
        };
        String[] iconos = {"leaf", "water", "recycle", "bike", "sun", "tree", "ordenar", "reciclar", "ahorro"};
        selectorIcono.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetasIconos));

        if (categoriaEditar != null) {
            nombre.setText(categoriaEditar.getNombre());
            descripcion.setText(categoriaEditar.getDescripcion());
            for (int i = 0; i < iconos.length; i++) {
                if (iconos[i].equals(categoriaEditar.getIcono())) {
                    selectorIcono.setSelection(i);
                    break;
                }
            }
        }

        contenido.addView(nombre);
        contenido.addView(descripcion, margenSuperior(dp(8)));
        TextView labelIcon = texto(getString(R.string.selecciona_icono), 13, getColor(R.color.eco_navy));
        labelIcon.setTypeface(null, Typeface.BOLD);
        contenido.addView(labelIcon, margenSuperior(dp(12)));
        contenido.addView(selectorIcono, margenSuperior(dp(3)));
        Button guardar = boton(getString(categoriaEditar == null
                ? R.string.guardar_categoria : R.string.guardar_cambios));
        contenido.addView(guardar, margenSuperior(dp(16)));
        guardar.setOnClickListener(v -> {
            String titulo = nombre.getText().toString().trim();
            String detalle = descripcion.getText().toString().trim();
            if (titulo.isEmpty()) {
                nombre.setError(getString(R.string.error_campos));
                return;
            }
            for (EcoCategory existente : categorias) {
                if ((categoriaEditar == null || !existente.getId().equals(categoriaEditar.getId()))
                        && existente.getNombre().equalsIgnoreCase(titulo)) {
                    nombre.setError(getString(R.string.error_categoria_duplicada));
                    return;
                }
            }
            int position = selectorIcono.getSelectedItemPosition();
            guardarCategoria(titulo, detalle, iconos[position], dialogo, categoriaEditar);
        });
        dialogo.setContentView(contenido);
        dialogo.show();
    }

    private void guardarCategoria(String nombre, String descripcion, String icono,
                                  BottomSheetDialog dialogo, EcoCategory categoriaEditar) {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) return;
        Map<String, Object> datos = new HashMap<>();
        datos.put("uid", usuario.getUid());
        datos.put("nombre", nombre);
        datos.put("descripcion", descripcion);
        datos.put("icono", icono);
        com.google.firebase.firestore.CollectionReference categoriasRef = firestore.collection("users")
                .document(usuario.getUid()).collection("categorias");
        Task<?> guardado;
        if (categoriaEditar == null) {
            datos.put("createdAt", FieldValue.serverTimestamp());
            guardado = categoriasRef.add(datos);
        } else {
            guardado = categoriasRef.document(categoriaEditar.getId()).update(datos);
        }
        guardado
                .addOnSuccessListener(documento -> {
                    dialogo.dismiss();
                    Toast.makeText(this, categoriaEditar == null
                            ? R.string.categoria_guardada : R.string.categoria_actualizada, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(error -> Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG).show());
    }

    private void mostrarFormularioAccion() {
        mostrarFormularioAccion(null);
    }

    private void mostrarFormularioAccion(Eco accionEditar) {
        if (categorias.isEmpty()) {
            Toast.makeText(this, R.string.sin_categorias, Toast.LENGTH_LONG).show();
            return;
        }
        BottomSheetDialog dialogo = nuevoBottomSheet();
        LinearLayout contenido = crearContenidoModal();
        agregarEncabezado(contenido, getString(accionEditar == null
                ? R.string.nueva_accion : R.string.editar_accion), dialogo::dismiss);
        View formulario = getLayoutInflater().inflate(R.layout.dialog_registrar_eco, contenido, false);
        Spinner selector = formulario.findViewById(R.id.selectorCategoriaAccion);
        ArrayList<String> etiquetas = new ArrayList<>();
        for (EcoCategory categoria : categorias) etiquetas.add(categoria.getNombre());
        selector.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas));
        EditText nombre = formulario.findViewById(R.id.inputNombreAccion);
        EditText descripcion = formulario.findViewById(R.id.inputDescripcionAccion);
        TextView fecha = formulario.findViewById(R.id.txtFechaRegistro);
        if (accionEditar != null) {
            nombre.setText(accionEditar.getNombre());
            descripcion.setText(accionEditar.getDescripcion());
            for (int i = 0; i < categorias.size(); i++) {
                if (categorias.get(i).getId().equals(accionEditar.getCategoriaId())) {
                    selector.setSelection(i);
                    break;
                }
            }
        }
        String fechaActual = accionEditar == null ? new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date())
                : accionEditar.getFecha();
        fecha.setText(getString(R.string.fecha_automatica, fechaActual));
        contenido.addView(formulario);
        Button guardar = boton(getString(accionEditar == null
                ? R.string.guardar_accion : R.string.guardar_cambios));
        contenido.addView(guardar, margenSuperior(dp(10)));
        guardar.setOnClickListener(v -> {
            String titulo = nombre.getText().toString().trim();
            String detalle = descripcion.getText().toString().trim();
            if (titulo.isEmpty()) {
                nombre.setError(getString(R.string.error_campos));
                return;
            }
            EcoCategory categoria = categorias.get(selector.getSelectedItemPosition());
            guardarAccion(categoria, titulo, detalle, dialogo, accionEditar);
        });
        dialogo.setContentView(contenido);
        dialogo.show();
    }

    private void guardarAccion(EcoCategory categoria, String nombre, String descripcion,
                               BottomSheetDialog dialogo, Eco accionEditar) {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) return;
        Map<String, Object> datos = new HashMap<>();
        datos.put("uid", usuario.getUid());
        datos.put("categoriaId", categoria.getId());
        datos.put("categoriaNombre", categoria.getNombre());
        datos.put("nombre", nombre);
        datos.put("descripcion", descripcion);
        com.google.firebase.firestore.CollectionReference accionesRef = firestore.collection("users")
                .document(usuario.getUid()).collection("acciones");
        Task<?> guardado;
        if (accionEditar == null) {
            datos.put("createdAt", FieldValue.serverTimestamp());
            guardado = accionesRef.add(datos);
        } else {
            guardado = accionesRef.document(accionEditar.getId()).update(datos);
        }
        guardado
                .addOnSuccessListener(documento -> {
                    dialogo.dismiss();
                    Toast.makeText(this, accionEditar == null
                            ? R.string.accion_guardada : R.string.accion_actualizada, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(error -> Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG).show());
    }

    private void mostrarDetalleCategoria(EcoCategory categoria) {
        BottomSheetDialog dialogo = nuevoBottomSheet();
        LinearLayout contenido = crearContenidoModal();
        agregarEncabezadoCategoria(contenido, categoria, dialogo::dismiss);
        contenido.addView(crearFilaEdicion(
                () -> { dialogo.dismiss(); mostrarFormularioCategoria(categoria); },
                () -> { dialogo.dismiss(); confirmarEliminarCategoria(categoria); }), margenInferior(dp(12)));
        if (!categoria.getDescripcion().isEmpty()) {
            TextView descripcion = texto(categoria.getDescripcion(), 13, getColor(R.color.eco_muted));
            contenido.addView(descripcion, margenInferior(dp(10)));
        }

        ArrayList<Eco> filtradas = new ArrayList<>();
        for (Eco accion : acciones) {
            if (categoria.getId().equals(accion.getCategoriaId())) filtradas.add(accion);
        }
        if (filtradas.isEmpty()) {
            TextView vacio = texto(getString(R.string.sin_acciones_categoria), 14, getColor(R.color.eco_muted));
            vacio.setGravity(Gravity.CENTER);
            vacio.setPadding(dp(10), dp(24), dp(10), dp(24));
            contenido.addView(vacio);
        } else {
            ScrollView lista = new ScrollView(this);
            LinearLayout filas = new LinearLayout(this);
            filas.setOrientation(LinearLayout.VERTICAL);
            for (Eco accion : filtradas) filas.addView(crearDetalleAccion(accion));
            lista.addView(filas);
            contenido.addView(lista, new LinearLayout.LayoutParams(-1, dp(360)));
        }
        dialogo.setContentView(contenido);
        dialogo.show();
    }

    private void mostrarDetalleAccion(Eco accion) {
        BottomSheetDialog dialogo = nuevoBottomSheet();
        LinearLayout contenido = crearContenidoModal();
        agregarEncabezado(contenido, accion.getNombre(), dialogo::dismiss);
        String nombreCategoria = accion.getCategoriaNombre();
        for (EcoCategory categoria : categorias) {
            if (categoria.getId().equals(accion.getCategoriaId())) {
                nombreCategoria = categoria.getNombre();
                break;
            }
        }
        TextView categoria = texto(nombreCategoria, 13, getColor(R.color.eco_green_dark));
        contenido.addView(categoria, margenInferior(dp(10)));
        TextView descripcion = texto(accion.getDescripcion().isEmpty()
                ? getString(R.string.sin_descripcion) : accion.getDescripcion(), 15, getColor(R.color.eco_navy));
        contenido.addView(descripcion, margenInferior(dp(10)));
        contenido.addView(texto(accion.getFecha(), 12, getColor(R.color.eco_muted)));
        contenido.addView(crearFilaEdicion(
                () -> { dialogo.dismiss(); mostrarFormularioAccion(accion); },
                () -> { dialogo.dismiss(); confirmarEliminarAccion(accion); }), margenSuperior(dp(16)));
        dialogo.setContentView(contenido);
        dialogo.show();
    }

    private LinearLayout crearFilaEdicion(Runnable alEditar, Runnable alEliminar) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.END);
        ImageButton editar = crearBotonIcono(R.drawable.ic_action_edit, R.string.editar,
                makeRoundedBackground(getColor(R.color.eco_canvas), dp(12)));
        ImageButton eliminar = crearBotonIcono(R.drawable.ic_action_delete, R.string.eliminar,
                makeRoundedBackground(Color.rgb(253, 237, 239), dp(12)));
        LinearLayout.LayoutParams paramsEditar = new LinearLayout.LayoutParams(dp(42), dp(42));
        paramsEditar.setMarginEnd(dp(8));
        fila.addView(editar, paramsEditar);
        fila.addView(eliminar, new LinearLayout.LayoutParams(dp(42), dp(42)));
        editar.setOnClickListener(v -> alEditar.run());
        eliminar.setOnClickListener(v -> alEliminar.run());
        return fila;
    }

    private ImageButton crearBotonIcono(int icono, int descripcion, GradientDrawable fondo) {
        ImageButton boton = new ImageButton(this);
        boton.setImageResource(icono);
        boton.setBackground(fondo);
        boton.setPadding(dp(10), dp(10), dp(10), dp(10));
        boton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        boton.setContentDescription(getString(descripcion));
        boton.setClickable(true);
        boton.setFocusable(true);
        return boton;
    }

    private void mostrarHistorial() {
        BottomSheetDialog dialogo = nuevoBottomSheet();
        LinearLayout contenido = crearContenidoModal();
        agregarEncabezado(contenido, getString(R.string.historial_titulo), dialogo::dismiss);
        if (acciones.isEmpty()) {
            TextView vacio = texto(getString(R.string.lista_vacia), 14, getColor(R.color.eco_muted));
            vacio.setPadding(0, dp(20), 0, dp(20));
            contenido.addView(vacio);
        } else {
            ScrollView lista = new ScrollView(this);
            LinearLayout filas = new LinearLayout(this);
            filas.setOrientation(LinearLayout.VERTICAL);
            for (Eco accion : acciones) filas.addView(crearDetalleAccion(accion));
            lista.addView(filas);
            contenido.addView(lista, new LinearLayout.LayoutParams(-1, dp(440)));
        }
        dialogo.setContentView(contenido);
        dialogo.show();
    }

    private View crearDetalleAccion(Eco accion) {
        LinearLayout tarjeta = new LinearLayout(this);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setPadding(dp(14), dp(12), dp(14), dp(12));
        tarjeta.setBackground(makeRoundedBackground(getColor(R.color.eco_canvas), dp(14)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(8);
        tarjeta.setLayoutParams(params);
        TextView nombre = texto(accion.getNombre(), 15, getColor(R.color.eco_navy));
        nombre.setTypeface(null, Typeface.BOLD);
        tarjeta.addView(nombre);
        if (!accion.getDescripcion().isEmpty()) {
            TextView descripcion = texto(accion.getDescripcion(), 13, getColor(R.color.eco_muted));
            tarjeta.addView(descripcion, margenSuperior(dp(3)));
        }
        TextView fecha = texto(accion.getFecha(), 11, getColor(R.color.eco_green_dark));
        tarjeta.addView(fecha, margenSuperior(dp(5)));
        tarjeta.setClickable(true);
        tarjeta.setFocusable(true);
        tarjeta.setOnClickListener(v -> mostrarDetalleAccion(accion));
        return tarjeta;
    }

    private void confirmarEliminarCategoria(EcoCategory categoria) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_categoria_titulo)
                .setMessage(R.string.confirmar_eliminar_categoria_mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> eliminarCategoria(categoria))
                .show();
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
                .document(accion.getId()).delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, R.string.accion_eliminada, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, R.string.error_eliminar, Toast.LENGTH_LONG).show());
    }

    private void eliminarCategoria(EcoCategory categoria) {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario == null) return;
        String uid = usuario.getUid();
        com.google.firebase.firestore.CollectionReference accionesRef = firestore.collection("users")
                .document(uid).collection("acciones");
        accionesRef.get().addOnSuccessListener(snapshot -> {
            ArrayList<DocumentSnapshot> asociadas = new ArrayList<>();
            for (DocumentSnapshot documento : snapshot.getDocuments()) {
                String idCategoria = valor(documento, "categoriaId");
                String nombreCategoria = valor(documento, "categoriaNombre");
                if (categoria.getId().equals(idCategoria)
                        || (idCategoria.isEmpty() && categoria.getNombre().equals(nombreCategoria))) {
                    asociadas.add(documento);
                }
            }

            ArrayList<Task<Void>> borrados = new ArrayList<>();
            final int tamanoLote = 400;
            for (int inicio = 0; inicio < asociadas.size(); inicio += tamanoLote) {
                com.google.firebase.firestore.WriteBatch lote = firestore.batch();
                int fin = Math.min(inicio + tamanoLote, asociadas.size());
                for (int i = inicio; i < fin; i++) lote.delete(asociadas.get(i).getReference());
                borrados.add(lote.commit());
            }

            Tasks.whenAll(borrados).addOnSuccessListener(unused -> firestore.collection("users")
                    .document(uid).collection("categorias").document(categoria.getId()).delete()
                    .addOnSuccessListener(hecho -> Toast.makeText(this, R.string.categoria_eliminada, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(error -> Toast.makeText(this, R.string.error_eliminar_categoria, Toast.LENGTH_LONG).show()))
                    .addOnFailureListener(error -> Toast.makeText(this, R.string.error_eliminar_categoria, Toast.LENGTH_LONG).show());
        }).addOnFailureListener(error -> Toast.makeText(this, R.string.error_eliminar_categoria, Toast.LENGTH_LONG).show());
    }

    private BottomSheetDialog nuevoBottomSheet() {
        BottomSheetDialog dialogo = new BottomSheetDialog(this);
        dialogo.setCanceledOnTouchOutside(true);
        return dialogo;
    }

    private LinearLayout crearContenidoModal() {
        LinearLayout contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(22), dp(12), dp(22), dp(24));
        contenido.setBackground(makeRoundedBackground(Color.WHITE, dp(24)));
        return contenido;
    }

    private void agregarEncabezado(LinearLayout contenedor, String titulo, Runnable cerrar) {
        LinearLayout fila = new LinearLayout(this);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 0, 0, dp(10));
        TextView textoTitulo = texto(titulo, 20, getColor(R.color.eco_navy));
        textoTitulo.setTypeface(null, Typeface.BOLD);
        fila.addView(textoTitulo, new LinearLayout.LayoutParams(0, -2, 1));
        TextView botonCerrar = texto("×", 28, getColor(R.color.eco_muted));
        botonCerrar.setGravity(Gravity.CENTER);
        botonCerrar.setContentDescription(getString(R.string.cerrar));
        botonCerrar.setClickable(true);
        botonCerrar.setFocusable(true);
        botonCerrar.setOnClickListener(v -> cerrar.run());
        fila.addView(botonCerrar, new LinearLayout.LayoutParams(dp(40), dp(40)));
        contenedor.addView(fila);
    }

    private void agregarEncabezadoCategoria(LinearLayout contenedor, EcoCategory categoria, Runnable cerrar) {
        LinearLayout fila = new LinearLayout(this);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 0, 0, dp(10));
        TextView icono = texto(iconoEmoji(categoria.getIcono()), 24, getColor(R.color.eco_green));
        if (iconoVectorial(categoria.getIcono()) != 0) {
            ImageView vector = new ImageView(this);
            vector.setImageResource(iconoVectorial(categoria.getIcono()));
            fila.addView(vector, new LinearLayout.LayoutParams(dp(34), dp(34)));
        } else {
            icono.setGravity(Gravity.CENTER);
            fila.addView(icono, new LinearLayout.LayoutParams(dp(38), dp(38)));
        }
        TextView titulo = texto(categoria.getNombre(), 20, getColor(R.color.eco_navy));
        titulo.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -2, 1);
        titleParams.leftMargin = dp(8);
        fila.addView(titulo, titleParams);
        TextView botonCerrar = texto("×", 28, getColor(R.color.eco_muted));
        botonCerrar.setGravity(Gravity.CENTER);
        botonCerrar.setContentDescription(getString(R.string.cerrar));
        botonCerrar.setClickable(true);
        botonCerrar.setFocusable(true);
        botonCerrar.setOnClickListener(v -> cerrar.run());
        fila.addView(botonCerrar, new LinearLayout.LayoutParams(dp(40), dp(40)));
        contenedor.addView(fila);
    }

    private void mostrarPantalla(View activa, boolean mostrarNav) {
        View[] pantallas = {screenLogin, screenSignup, screenHome, screenRegister, screenProfile};
        for (View pantalla : pantallas) {
            pantalla.animate().cancel();
            if (pantalla == activa) {
                pantalla.setAlpha(0f);
                pantalla.setVisibility(View.VISIBLE);
                pantalla.animate().alpha(1f).setDuration(180).start();
            } else {
                pantalla.setVisibility(View.GONE);
            }
        }
        if (signupBackCallback != null) {
            signupBackCallback.setEnabled(activa == screenSignup);
        }
        if (mostrarNav) actualizarNavegacionInferior(activa);
        navBottom.setVisibility(mostrarNav ? View.VISIBLE : View.GONE);
    }

    private void actualizarNavegacionInferior(View pantallaActiva) {
        int[] ids = {R.id.btnNavInicio, R.id.btnNavRegistrar, R.id.btnNavPerfil};
        int indiceActivo = pantallaActiva == screenHome ? 0 : pantallaActiva == screenRegister ? 1 : 2;
        for (int i = 0; i < ids.length; i++) {
            LinearLayout item = findViewById(ids[i]);
            boolean seleccionado = i == indiceActivo;
            item.setBackgroundResource(seleccionado ? R.drawable.bg_nav_selected : android.R.color.transparent);
            ImageView icono = (ImageView) item.getChildAt(0);
            TextView etiqueta = (TextView) item.getChildAt(1);
            int color = getColor(seleccionado ? R.color.eco_green_dark : R.color.eco_muted);
            icono.setColorFilter(color);
            etiqueta.setTextColor(color);
            etiqueta.setTypeface(null, seleccionado ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private EditText campo(String hint, int maxLength, boolean multiline) {
        EditText campo = new EditText(this);
        campo.setSingleLine(!multiline);
        campo.setHint(hint);
        campo.setTextSize(15);
        campo.setPadding(dp(12), dp(10), dp(12), dp(10));
        campo.setBackgroundResource(R.drawable.bg_input_field);
        campo.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(maxLength)});
        return campo;
    }

    private Button boton(String titulo) {
        com.google.android.material.button.MaterialButton boton =
                new com.google.android.material.button.MaterialButton(this);
        boton.setText(titulo);
        boton.setTextColor(Color.WHITE);
        boton.setAllCaps(false);
        boton.setCornerRadius(dp(15));
        boton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.eco_green_dark)));
        return boton;
    }

    private TextView texto(String contenido, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(contenido);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable makeRoundedBackground(int color, int radius) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(radius);
        return fondo;
    }

    private LinearLayout.LayoutParams margenSuperior(int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = margin;
        return params;
    }

    private LinearLayout.LayoutParams margenInferior(int margin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = margin;
        return params;
    }

    private String valor(DocumentSnapshot documento, String campo) {
        String value = documento.getString(campo);
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

// Verificacion de integracion continua