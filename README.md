# ORLO — App móvil

App de asesoría de imagen personal: diagnóstico de estilo, lecciones cortas, asesor con inteligencia artificial y armario digital.

Proyecto Android en Kotlin con Jetpack Compose, Hilt, Retrofit y Firebase (Authentication, Firestore y Storage). El contenido (estilos, cuestionario, lecciones), el cálculo del resultado, el asesor IA y el estado premium los sirve el backend de [ORLO-WEB](https://github.com/AndresContreras1/ORLO-WEB).

## Arquitectura

```
app/src/main/java/com/orlo/app
├── data
│   ├── datasource            contratos de cada fuente (interfaces) y data sources de Auth, Storage e imagen local
│   │   ├── impl/firestore    perfil, resultados, avance, prendas, outfits y conversaciones
│   │   ├── impl/retrofit     contenido, asesor y cuenta del backend
│   │   └── services          servicios Retrofit e interceptor del token de Firebase
│   ├── dtos                  DTOs de Firestore y del backend con sus funciones de mapeo
│   ├── injection             módulos de Hilt (Firebase, Retrofit, data sources, dispatcher)
│   ├── model                 modelos que usa la UI
│   └── repository            repositorios: try/catch, Result y excepciones propias
└── ui
    ├── navigation            Screen (rutas), AppNavigation, barra inferior y NavigationLogic
    ├── screens/<pantalla>    Screen + ViewModel + State (+ components)
    ├── theme                 colores, tipografía y formas
    └── utils                 componentes reutilizables y mensajes de error
```

Flujo: `Screen → ViewModel → Repository → DataSource → Firebase / backend`. Las prendas, los outfits, el perfil, el avance de las lecciones y las conversaciones llegan en vivo: los data sources los exponen con `callbackFlow` sobre `addSnapshotListener` y los ViewModels los recolectan mientras viven. Las pantallas reciben lambdas y solo `AppNavigation` navega. Cada ViewModel expone un `StateFlow` con su estado (`isLoading`, `errorMessageRes` y los datos).

Los datos privados del usuario viven en Firestore bajo `users/{uid}` y las fotos en Storage (`garmentImages/{uid}/` y `profileImages/`). Firebase es compartido con la web: las reglas, las Cloud Functions, el seed y el modelo de datos (`docs/modelo-de-datos.md`) están en [ORLO-WEB](https://github.com/AndresContreras1/ORLO-WEB), carpeta `firebase/`.

## Requisitos

- Android Studio con el SDK 36 y un emulador (minSdk 26).
- Firebase CLI (`npm install -g firebase-tools`) para el emulador local.
- El backend de ORLO-WEB corriendo en `http://localhost:8090`.

## Cómo correrla en desarrollo

1. Emulador de Firebase (Auth 9099, Firestore 8080, Storage 9199, Functions 5001, interfaz en http://localhost:4000), desde `ORLO-WEB/firebase`:
   ```bash
   cd functions && npm install && cd .. && firebase emulators:start --project demo-orlo
   ```
2. Backend de ORLO-WEB en el puerto 8090 (ver su README).
3. Datos de prueba con prendas reales, desde `ORLO-WEB/firebase/seed`: `npm install && npm run seed`. Crea, entre otras, la cuenta `laura.prueba@orlo.test` con contraseña `Prueba2026`.
4. Abrir el proyecto en Android Studio y correr `app` en el emulador de Android.

En `debug` la app usa el emulador de Firebase y el backend del computador (`10.0.2.2` desde el emulador de Android). El `google-services.json` del repositorio es del proyecto de prueba `demo-orlo`: solo sirve con el emulador.

En desarrollo el backend corre con `ORLO_PREMIUM_PARA_TODOS=true`: todas las funciones están abiertas para cualquier cuenta.

## Pruebas

```bash
./gradlew testDebugUnitTest
```

Unitarias con MockK, Truth y `runTest` (65): mapeos de DTOs, 31 de repositorios y 24 de ViewModels con los data sources o repositorios moqueados. Los flujos en vivo se simulan con `MutableStateFlow`.

```bash
./gradlew connectedDebugAndroidTest
```

Instrumentadas (35), contra el emulador de Firebase de `ORLO-WEB/firebase` y el backend local:
- 18 de data sources (Firestore: usuario, armario, conversaciones, guardados y outfit del día, con las reglas compartidas y los listeners en vivo).
- 11 de repositorios con sus data sources reales (fotos a Storage, perfil y armario en vivo, borrado de fotos); lo que va al backend se reemplaza por un doble.
- 2 de ViewModels con repositorios reales: lo que cambia en Firestore llega solo al estado.
- 4 e2e con Hilt: el registro, un usuario nuevo que corrige una contraseña de 4 caracteres, hace el cuestionario y completa una lección, y un usuario registrado que agrega una prenda, arma un outfit, lo marca favorito y lo ve en el historial.

Necesitan el emulador de Firebase, el backend y un emulador de Android encendidos.

## Pasar a Firebase real

1. Crear el proyecto en la consola de Firebase con Authentication (correo y contraseña), Firestore y Storage.
2. Registrar la app Android con el paquete `com.orlo.app` y reemplazar `app/google-services.json`.
3. Publicar las reglas y las funciones desde `ORLO-WEB/firebase`: `firebase deploy --project <id-del-proyecto>`.
4. Cambiar `API_BASE_URL` del tipo `release` en `app/build.gradle.kts` por la dirección del backend publicado.

## Piezas fuera de la arquitectura base

- `FirebaseTokenInterceptor`: agrega el token de Firebase a cada petición al backend para que este sepa quién la hace.
- `ImagenLocalDataSource`: comprime las fotos (prendas y perfil) antes de subirlas y las endereza según el EXIF de la cámara.
- Fotos desde la cámara con `TakePicture` y un `FileProvider` (`res/xml/rutas_de_fotos.xml`), o desde la galería y los archivos con `GetContent`. Ninguna de las dos pide permisos.
- Notificaciones con Firebase Cloud Messaging: la app se suscribe al tema `lecciones` y `OrloMessagingService` muestra el aviso; tocarlo abre la lección. Desde Android 13 se pide el permiso al llegar al inicio. Con el proyecto `demo-orlo` no llegan avisos reales: hace falta un proyecto de Firebase real.
- Coil manda un User-Agent propio (`ORLO/<versión> (Android)`): Wikimedia, de donde salen las fotos del catálogo de Explora, rechaza las descargas sin él.
- `EmuladorFirebase`: en debug, las URLs de las fotos se guardan con `127.0.0.1` (como las ve la web) y Coil las pide a `10.0.2.2`.
- El cobro de la suscripción todavía no está conectado a Google Play: el acceso premium se activa desde el panel.
