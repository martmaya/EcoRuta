# EcoRuta

Prototipo Android en Java para registrar acciones ecológicas con Firebase Authentication y Cloud Firestore.

## Configuración de Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Registra una app Android con el identificador `com.example.ecoruta`.
3. Descarga `google-services.json` y colócalo en `app/google-services.json`.
4. En Authentication, habilita el proveedor **Correo electrónico/contraseña**.
5. Crea una base de datos de Cloud Firestore.
6. Publica el contenido de [`firestore.rules`](firestore.rules) en **Firestore Database → Reglas**.
7. Sincroniza Gradle y ejecuta la app.

El plugin de Google Services solo se aplica cuando existe `app/google-services.json`. Sin ese archivo, el proyecto puede compilar, pero la pantalla indica que falta la configuración y no habilita el acceso.

## Datos y acceso

Cada cuenta solo puede leer, crear y eliminar sus documentos en `users/{uid}/acciones/{actionId}`. Las reglas validan propietario, campos y longitudes, y rechazan cambios posteriores. La app también valida los campos antes de enviarlos; la validación del cliente nunca sustituye las reglas del servidor.

La app guarda únicamente correo para autenticación y datos de acciones ecológicas. No solicita matrícula, ubicación ni otros datos personales innecesarios. Las contraseñas las gestiona Firebase Authentication y no se escriben en Firestore ni en registros de la app. La comunicación con Firebase usa HTTPS/TLS; la app deshabilita tráfico HTTP sin cifrar. Android Auto Backup está desactivado para no copiar preferencias internas de autenticación a otro dispositivo.

Se pueden eliminar acciones individualmente desde la pantalla. La cuenta de Firebase se administra desde Firebase Authentication; la app no incluye eliminación de cuenta todavía.

Las acciones que estaban guardadas localmente en versiones anteriores no se suben automáticamente al iniciar sesión. Así no se asocian datos locales con una cuenta elegida por error.

## Repositorio y credenciales

El proyecto incluye las reglas y el código de cliente. No incluyas claves de cuentas de servicio, archivos de Admin SDK ni credenciales privadas. `google-services.json` contiene configuración de cliente Android; la protección de datos depende de Authentication y de las reglas publicadas, no de ocultar ese archivo.

Antes de presentar el prototipo, verifica en Firebase Console que Authentication y Firestore estén habilitados, que las reglas publicadas coincidan con este archivo y que dos cuentas distintas no puedan acceder a las acciones de la otra. Para publicar, también conviene activar Firebase App Check y configurar sus proveedores para las versiones de desarrollo y producción.
