# AppEAFIT

> 📓 **Antes de trabajar en este proyecto, lee [`BITACORA.md`](BITACORA.md).** Ahí está
> la documentación completa de arquitectura, estructura, reglas de seguridad y el
> registro histórico de cambios — y la regla obligatoria de actualizarlo cada vez que
> alguien (persona o IA) trabaje aquí.

Aplicación móvil no oficial para la comunidad de la Universidad EAFIT — **100% virtual, sin
componente presencial** — inspirada en la experiencia de apps institucionales universitarias
(carnet digital, servicios, notificaciones) y adaptada a la identidad visual de EAFIT (azul
`#146AEF`, negro y blanco).

La app tiene **cuatro roles**, cada uno con su propio panel de servicios:

| Rol | Qué puede hacer |
|---|---|
| **Estudiante** | Notas, horario (clases virtuales), calculadora de promedio, calendario académico interactivo, evaluación docente, biblioteca (préstamos), carnet digital con QR |
| **Profesor** | Mis cursos, tomar asistencia (sesiones virtuales en vivo), cargar notas, publicar anuncios de curso |
| **Administrativo** | Directorio institucional, gestionar anuncios |
| **Administrador de la app** | Gestión de usuarios y roles, gestión de contenido, notificaciones push masivas, estadísticas |

> Como la universidad no tiene campus físico, la app deliberadamente **no** incluye funciones que
> asuman presencialidad (objetos perdidos, reserva de salas/salones, ubicación de aulas). Si vas a
> agregar una función nueva, ten esto en cuenta antes de diseñarla.

## Stack técnico

- **Kotlin + Jetpack Compose** (Material 3), arquitectura MVVM con inyección de dependencias manual
  (`AppContainer`, sin Hilt para minimizar puntos de fallo de anotación).
- **Firebase**: Auth (correo institucional `@eafit.edu.co`), Firestore, Storage (fotos de perfil),
  Cloud Messaging (notificaciones push).
- **Room**: caché local offline-first para perfil, cursos/horario, notas, noticias y calendario
  académico, de forma que esa información se pueda consultar sin conexión a internet una vez se
  sincronizó al menos una vez (pensado para zonas con conectividad intermitente).
- **Navigation Compose**, Coil (imágenes), ZXing (QR del carnet), WorkManager, DataStore.

## Estructura del proyecto

```
app/src/main/java/co/edu/eafit/appeafit/
  core/           Utilidades transversales: DI manual, conectividad, notificaciones
  data/
    local/        Entidades y DAOs de Room (caché offline)
    repository/    Repositorios (Firestore + caché local)
  domain/model/    Modelos de dominio
  ui/
    theme/         Colores, tipografía y tema EAFIT
    navigation/    Grafo de navegación por rol
    auth/          Login, registro, recuperar contraseña
    home/ services/ carnet/ profile/ notifications/
    student/ professor/ staff/ admin/   Pantallas específicas por rol
```

## Configurar Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Agrega una app Android con el paquete `co.edu.eafit.appeafit` (y opcionalmente
   `co.edu.eafit.appeafit.debug` para el build de depuración).
3. Descarga el `google-services.json` real y colócalo en `app/google-services.json`
   (ese archivo **nunca** se sube al repositorio, ver `.gitignore`; usa
   `app/google-services.json.example` como referencia de formato).
4. Habilita **Authentication → Correo electrónico/contraseña**.
5. Crea una base de datos **Firestore** (modo producción) y despliega las reglas de este repo:
   ```
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```
6. Habilita **Storage** para las fotos de perfil.
7. (Opcional) Habilita **Cloud Messaging** para notificaciones push.

El primer usuario que se registra queda como `student`. Para promover a alguien a `professor`,
`staff` o `admin`, un administrador debe hacerlo desde **Administrador → Gestión de usuarios**, o
manualmente cambiando el campo `role` del documento en `users/{uid}` desde la consola de Firebase
la primera vez (para crear al primer administrador).

## Compilar

```
./gradlew assembleDebug
```

Requiere Android SDK (compileSdk 36) y JDK 17+. El wrapper de Gradle está incluido.

## Seguridad

- `app/google-services.json`, keystores y cualquier credencial están en `.gitignore`.
- Las reglas de Firestore (`firestore.rules`) y Storage (`storage.rules`) aplican control de acceso
  por rol: cada colección valida quién puede leer/escribir según `users/{uid}.role`.
