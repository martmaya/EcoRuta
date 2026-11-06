# EcoRuta

Aplicación Android en Java para registrar acciones ecológicas con Firebase Authentication y Cloud Firestore. La pantalla principal sigue la referencia visual de Eco Ruta: fondo blanco, panel azul marino curvo, ilustración de la Tierra, acentos verdes y navegación inferior fija.

## Configuración de Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registra una app Android con el identificador `com.example.ecoruta`.
3. Descarga `google-services.json` y colócalo en `app/google-services.json`.
4. En Authentication, habilita el proveedor **Correo electrónico/contraseña**.
5. Crea una base de datos de Cloud Firestore.
6. Publica el contenido actualizado de [`firestore.rules`](firestore.rules) en **Firestore Database → Reglas**.
7. Sincroniza Gradle y ejecuta la app.

El plugin de Google Services solo se aplica cuando existe `app/google-services.json`. Sin ese archivo, la pantalla indica que falta la configuración y no habilita el acceso. La app siembra las categorías iniciales Ordenar, Reciclar y Ahorro para cada usuario; después admite nuevas categorías con icono elegido.

## Datos y acceso

Cada cuenta obtiene sus categorías en `users/{uid}/categorias/{categoryId}` y sus acciones en `users/{uid}/acciones/{actionId}`. Las acciones guardan su `categoriaId`, nombre de categoría y fecha de creación del servidor. Las reglas validan propietario, campos y longitudes, y rechazan cambios posteriores. La app también valida los campos antes de enviarlos; la validación del cliente nunca sustituye las reglas del servidor.

La pantalla Inicio muestra el total, el progreso hacia la primera meta de 10 acciones, las categorías interactivas y las acciones recientes. Registrar, consultar una categoría e historial usan ventanas modales. Perfil muestra solamente el nombre, correo y foto que estén disponibles en Firebase Authentication. Si el nombre o la foto no están definidos en la cuenta, no se inventan.

La app guarda únicamente correo para autenticación y datos de acciones ecológicas. No solicita matrícula, ubicación ni otros datos personales innecesarios. Las contraseñas las gestiona Firebase Authentication y no se escriben en Firestore ni en registros de la app. La comunicación con Firebase usa HTTPS/TLS; la app deshabilita tráfico HTTP sin cifrar. Android Auto Backup está desactivado para no copiar preferencias internas de autenticación a otro dispositivo.

Se pueden eliminar acciones individualmente desde la pantalla. La cuenta de Firebase se administra desde Firebase Authentication; la app no incluye eliminación de cuenta todavía.

Las acciones que estaban guardadas localmente en versiones anteriores no se suben automáticamente al iniciar sesión. Así no se asocian datos locales con una cuenta elegida por error.

## Repositorio y credenciales

El proyecto incluye las reglas y el código de cliente. No incluyas claves de cuentas de servicio, archivos de Admin SDK ni credenciales privadas. `google-services.json` contiene configuración de cliente Android; la protección de datos depende de Authentication y de las reglas publicadas, no de ocultar ese archivo.

Si ya habías publicado las reglas anteriores, vuelve a publicar este archivo: las reglas actuales también protegen la colección `categorias` y validan el nuevo formato de acción.

Antes de presentar el prototipo, verifica en Firebase Console que Authentication y Firestore estén habilitados, que las reglas publicadas coincidan con este archivo y que dos cuentas distintas no puedan acceder a las categorías ni a las acciones de la otra. Para publicar, también conviene activar Firebase App Check y configurar sus proveedores para las versiones de desarrollo y producción.
