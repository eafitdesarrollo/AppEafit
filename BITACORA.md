# BITÁCORA DEL PROYECTO — AppEAFIT

> **Este archivo es la fuente de verdad para retomar el trabajo en este proyecto.**
> Si eres una persona, una IA o un agente que va a trabajar aquí por primera vez, lee
> este documento COMPLETO antes de tocar código. Con esto deberías tener suficiente
> contexto para saber qué es el proyecto, cómo está organizado, qué se ha hecho, qué
> falta, y dónde está cada cosa — sin tener que releer todo el repositorio desde cero.

---

## 0. REGLA OBLIGATORIA DE ACTUALIZACIÓN (leer primero)

**Cada vez que alguien — persona, IA o agente — trabaje en este proyecto (agregue una
función, corrija un bug, cambie configuración, toque las reglas de Firestore, actualice
dependencias, etc.), DEBE agregar una entrada nueva al final de la sección
["8. REGISTRO DE CAMBIOS"](#8-registro-de-cambios-bitácora-cronológica-append-only) de
este archivo ANTES de dar la tarea por terminada.**

Reglas de esa entrada:

1. **Nunca se borran ni se editan entradas anteriores.** Este es un registro histórico
   de solo-agregar (append-only). Si algo quedó mal documentado, se corrige agregando
   una entrada nueva que lo aclare, no editando la vieja.
2. **Toda entrada debe llevar fecha real (año-mes-día) y nombre completo de quien hizo
   el trabajo**, con este formato exacto de encabezado:
   ```
   ### AAAA-MM-DD — Nombre Completo
   ```
3. **Si quien está haciendo el cambio es una IA/agente**: antes de escribir la entrada,
   la IA **debe preguntarle a la persona que la está dirigiendo cuál es su nombre
   completo** (a menos que ya se lo haya dado explícitamente en la conversación). Nunca
   se debe inventar un nombre, dejarlo en blanco, ni usar "el usuario" o "IA" como
   autor. El nombre que va en la entrada es el de la PERSONA responsable del trabajo,
   no el del modelo/agente.
4. **El contenido debe ser específico y accionable**, no un resumen vago. Cada entrada
   debe decir: qué se hizo, en qué archivos, por qué (si corrige un bug: qué bug, qué
   escenario lo disparaba), y qué queda pendiente si algo no se terminó. Alguien que
   solo lea esa entrada (sin ver el diff) debe entender el cambio.
5. Si el cambio toca `firestore.rules` o `storage.rules`, la entrada debe decirlo
   explícitamente y recordar que hay que desplegar las reglas
   (`firebase deploy --only firestore:rules,firestore:indexes,storage`) — editar el
   archivo local no cambia nada en producción hasta que se despliega.
6. Si el cambio agrega o modifica un ítem de la lista de pendientes (sección 6), esa
   lista también se debe actualizar en el mismo commit/entrega.

---

## 1. VISIÓN GENERAL DEL PROYECTO

**AppEAFIT** es una aplicación móvil Android **no oficial** para la comunidad de la
Universidad EAFIT, inspirada en la app institucional oficial (carnet digital, servicios,
notificaciones) pero construida de forma independiente por Santiago Guerrero Parrado.

La app tiene **cuatro roles**, cada uno con su propio panel de servicios:

| Rol | Qué puede hacer |
|---|---|
| **Estudiante** | Notas, horario (clases virtuales), calculadora de promedio, calendario académico interactivo, evaluación docente, biblioteca (préstamos), carnet digital con QR |
| **Profesor** | Mis cursos, tomar asistencia (sesiones virtuales), cargar notas, publicar anuncios de curso |
| **Administrativo (staff)** | Directorio institucional, gestionar anuncios |
| **Administrador (admin)** | Gestión de usuarios y roles, gestión de contenido, notificaciones push masivas, estadísticas |

> Desde el 2026-09-13, la app **no** tiene "objetos perdidos" ni "reserva de espacios" —
> se eliminaron por completo (código + Firestore) porque EAFIT aquí es 100% virtual, sin
> campus físico. Ver la entrada del 2026-09-13 (punto 18) en la sección 8.

Desde el 2026-09-13 (noche), en **Perfil → Configuración** hay dos preferencias nuevas,
además del switch de notificaciones push: **tema** (sistema / claro / oscuro) e
**idioma** (sistema / español / inglés). Ambas se guardan en DataStore
(`SettingsRepository`) y se aplican de inmediato (el tema recompone en caliente; el
idioma recrea la Activity). La app queda completamente traducida al inglés
(`values-en/strings.xml`). Ver el punto 21 de la sección 8 para el detalle técnico.

El primer usuario que se registra queda como `student`. Para promover a alguien a
`professor`, `staff` o `admin`, un administrador debe hacerlo desde
**Administrador → Gestión de usuarios**, o manualmente cambiando el campo `role` del
documento en `users/{uid}` desde la consola de Firebase (necesario para crear al primer
administrador, ya que nadie parte siendo admin).

### Stack técnico

- **Kotlin + Jetpack Compose** (Material 3), arquitectura MVVM con inyección de
  dependencias manual (`AppContainer`, sin Hilt para minimizar puntos de fallo de
  anotación).
- **Firebase**: Auth (correo institucional `@eafit.edu.co`), Firestore, Storage (fotos
  de perfil), Cloud Messaging (notificaciones push).
- **Room**: caché local offline-first para perfil, cursos/horario, notas, noticias y
  calendario académico — la información se puede consultar sin conexión una vez se
  sincronizó al menos una vez.
- **Navigation Compose**, Coil (imágenes), ZXing (QR del carnet), WorkManager,
  DataStore (preferencias).
- **Versiones clave** (`gradle/libs.versions.toml`): AGP 8.13.2, Kotlin 2.2.21,
  compileSdk/targetSdk 36, minSdk 26, Compose BOM 2025.09.01, Firebase BOM 34.19.0,
  Room 2.8.5, JDK 17.

### Repositorio

- Remoto: `https://github.com/eafitdesarrollo/AppEafit.git`, rama `main`.
- El repo Git real vive en la carpeta `repo/` de este directorio de trabajo; la carpeta
  padre (`Movil/EAFIT/`) NO es un repo git. **Actualizado 2026-09-13 (noche)**: las 14
  capturas de pantalla sueltas (`WhatsApp Image ....jpeg`) que antes vivían en esa carpeta
  padre ya no están — deben haberse movido o borrado manualmente en algún momento después
  de la auditoría de la tarde del 2026-09-13; a la fecha la carpeta padre solo contiene
  `repo/` (y la carpeta de configuración local `.claude/`, que tampoco es parte del código).
- Al 2026-09-13 el historial de git tiene 4 commits, todos del mismo día salvo el inicial
  (2026-09-11): "Initial AppEAFIT...", "Auditoria de seguridad...", "Documenta el
  despliegue de firestore.rules...", "Rediseno completo de la UI...".

---

## 2. ARQUITECTURA Y ESTRUCTURA DE CARPETAS

Todo el código vive bajo `repo/app/src/main/java/co/edu/eafit/appeafit/`:

```
core/
  connectivity/ConnectivityObserver.kt   Flow reactivo de conectividad (callbackFlow sobre ConnectivityManager)
  di/AppContainer.kt                     Contenedor de dependencias manual: instancia Firebase, Room y todos los repositorios
  di/ViewModelFactory.kt                 Factories genéricas para crear ViewModels con AppContainer inyectado
  notifications/EafitMessagingService.kt Servicio de FCM: recibe push, muestra notificación, guarda el token del dispositivo
  util/UiState.kt                        Sealed class genérica Loading/Success/Error para pantallas
  util/Errors.kt                         Traductor de excepciones de Firebase a mensajes en español (compartido por Auth y Profile)

data/
  local/AppDatabase.kt                   Base de datos Room (singleton, fallbackToDestructiveMigration)
  local/dao/Daos.kt                      Todos los DAOs (UserDao, CourseDao, EnrollmentDao, GradeDao, NewsDao, CalendarEventDao, SyncStateDao)
  local/entity/Entities.kt                Entidades Room (CachedUserEntity, CachedCourseEntity, CachedEnrollmentEntity, CachedGradeEntity, CachedNewsEntity, CachedCalendarEventEntity, SyncStateEntity)
  repository/FirestoreMappers.kt          Funciones de extensión DocumentSnapshot -> modelo de dominio (y toMap() inverso) para TODAS las colecciones
  repository/AuthRepository.kt            Firebase Auth: login, registro, reset de contraseña, cambio de contraseña (con reautenticación), borrar cuenta
  repository/UserRepository.kt            CRUD de users/{uid}: perfil, rol, activo/inactivo, listado completo (admin/staff/profesor)
  repository/CourseRepository.kt          Cursos, matrículas (enrollments), estudiantes matriculados por curso — caché Room + Firestore
  repository/GradeRepository.kt           Notas por estudiante — caché Room + Firestore
  repository/AttendanceRepository.kt      Sesiones de asistencia por curso
  repository/CalendarRepository.kt        Calendario académico institucional — caché Room + Firestore
  repository/NewsRepository.kt            Anuncios/noticias institucionales y de curso — caché Room + Firestore
  repository/NotificationRepository.kt    Notificaciones (personales, por rol, broadcast) + marcador de "leído" POR USUARIO (subcolección reads/)
  repository/TeacherEvaluationRepository.kt Evaluación docente (creación + verificación de "ya evaluado")
  repository/LoanRepository.kt            Préstamos de biblioteca (renovación con límite y transacción atómica)
  repository/SettingsRepository.kt        Preferencias locales (DataStore): notificaciones push, tema (ThemeMode:
                                          SYSTEM/LIGHT/DARK) e idioma (language_tag: "system"/"es"/"en", con
                                          languageBlocking() para lectura síncrona desde MainActivity.attachBaseContext)

domain/model/                            Data classes puras (sin dependencias de Firebase/Room): AppNotification, Attendance,
                                          CalendarEvent, Course/Enrollment/ScheduleSlot (sin campo "room" desde 2026-09-13),
                                          Grade, Loan, NewsItem, Role, User
                                          (LostItem y Reservation/Space se eliminaron el 2026-09-13, ver sección 8)

ui/
  theme/                Color.kt, Theme.kt, Type.kt — paleta EAFIT (azul #146AEF, navy, cyan, negro, blanco), M3, claro/oscuro
  navigation/            Routes.kt (strings de rutas), EafitNavHost.kt (grafo raíz), MainScreen.kt (bottom bar + host),
                         CommonNavGraph.kt (rutas compartidas por todos los roles), StudentNavGraph.kt, ProfessorNavGraph.kt,
                         StaffNavGraph.kt, AdminNavGraph.kt (rutas específicas por rol), SessionViewModel.kt (estado de sesión:
                         CheckingSession/LoggedOut/LoggedIn)
  auth/                  LoginScreen, RegisterScreen, ForgotPasswordScreen, AuthViewModel (login/registro/reset, valida dominio
                         @eafit.edu.co, revierte la cuenta de Auth si falla crear el perfil)
  splash/SplashScreen.kt Pantalla de arranque
  home/                  HomeScreen, HomeViewModel — noticias + accesos rápidos según rol
  services/               ServiceCatalog.kt (catálogo de servicios por rol, SOLO afecta qué se MUESTRA en la UI — no es una
                         barrera de seguridad real, ver sección 4), ServicesScreen.kt
  carnet/                CarnetScreen.kt (carnet digital con QR), QrCodeGenerator.kt (ZXing)
  notifications/         NotificationsScreen, NotificationsViewModel
  profile/               ProfileScreen, EditProfileScreen, ChangePasswordScreen (pide contraseña actual, reautentica),
                         SettingsScreen (toggle de notificaciones push), ProfileViewModel
  components/             CommonComponents.kt (LoadingState/ErrorState/EmptyState/RoleBadge, etc.), EafitBottomBar.kt, NewsCard.kt
  student/                AcademicCalendarScreen (calendario mensual interactivo real, ver sección 8), GpaCalculatorScreen,
                         GradesScreen + GradesViewModel (promedio ponderado por créditos), LibraryScreen (renovación de
                         préstamos con límite), ScheduleScreen + ScheduleViewModel (horario "Virtual", sin salón),
                         TeacherEvaluationScreen
  professor/              MyCoursesScreen, AttendanceScreen, GradeEntryScreen, AnnouncementsScreen
  staff/                  DirectoryScreen, ManageAnnouncementsScreen (refresca al abrir)
  admin/                  ManageUsersScreen (con guardia de "último admin"), BroadcastScreen, StatsScreen
```

Recursos Android (`app/src/main/res/`): `values/strings.xml` (español, idioma base),
`values-en/strings.xml` (traducción completa al inglés, misma cantidad de líneas/claves
que `values/strings.xml` desde el 2026-09-13), `values/colors.xml` (paleta EAFIT),
`values/themes.xml`, `xml/locales_config.xml` (declara los locales soportados `es`/`en`,
referenciado desde `android:localeConfig` en el `<application>` del manifest — nuevo
2026-09-13), `drawable/` (íconos vectoriales), `mipmap-anydpi-v26/` (ícono de launcher).

Configuración de build: `app/build.gradle.kts`, `gradle/libs.versions.toml` (catálogo
de versiones), `settings.gradle.kts`, `gradle.properties`, `app/proguard-rules.pro`
(mantiene `domain/model/**` para la (de)serialización reflectiva de Firestore).

---

## 3. MODELO DE DATOS

### 3.1 Colecciones de Firestore

| Colección | Documento clave | Campos principales | Quién escribe |
|---|---|---|---|
| `users/{uid}` | uid del usuario | email, fullName, role, program, institutionalId, photoUrl, active, createdAt, fcmToken | el propio usuario (perfil), admin (rol/activo) |
| `news/{id}` | auto | title, category, body, imageUrl, authorId, publishedAt | profesor (anuncios de curso), staff/admin (institucionales) |
| `calendarEvents/{id}` | auto | title, description, date | staff/admin |
| `courses/{id}` | auto | name, code, professorId, professorName, credits, schedule[] | staff/admin |
| `enrollments/{id}` | auto | studentId, courseId | staff/admin |
| `grades/{id}` | auto | studentId, courseId, courseName, item, score, maxScore, weightPercent, date | el profesor DUEÑO del curso (desde 2026-09-13; antes cualquier profesor), staff/admin |
| `attendance/{id}` | auto | courseId, date, records[] (studentId, studentName, present) | el profesor DUEÑO del curso (desde 2026-09-13), staff/admin |
| `teacherEvaluations/{id}` | auto | studentId, courseId, ratings... | el propio estudiante (create); solo lectura propia + staff/admin (desde 2026-09-13) |
| `loans/{id}` | auto | studentId, itemTitle, loanedAt, dueAt, returned, renewalCount | staff/admin (create/delete), estudiante dueño o staff/admin (update, incl. renovación) |
| `notifications/{id}` | auto | title, body, targetRole, targetUserId, createdAt | solo admin (desde 2026-09-13; antes cualquier usuario podía editar el contenido) |
| `notifications/{id}/reads/{uid}` | uid del usuario | uid, readAt | cada usuario, solo su propio marcador (NUEVO 2026-09-13 — reemplaza el campo `read` compartido) |

Las reglas completas están en `firestore.rules` (ver sección 4 para el detalle de qué
cambió y por qué). Los índices compuestos y de collection-group están en
`firestore.indexes.json`.

> Las colecciones `lostItems`, `spaces` y `reservations` existieron hasta el 2026-09-13
> y se eliminaron por completo (código + reglas) ese día — ver sección 8, punto 18.

### 3.2 Caché local (Room) — `data/local/`

`AppDatabase` (Room) cachea, por ahora: `cached_user`, `cached_course`,
`cached_enrollment`, `cached_grade`, `cached_news`, `cached_calendar_event` y
`sync_state`. Cada repositorio sigue el mismo patrón: `observeCached...()` expone un
`Flow` de Room como única fuente de verdad para la UI (funciona offline), y
`refresh()`/`refreshForX()` trae de Firestore y actualiza Room. **Desde 2026-09-13,
`news`, `calendarEvents` y los cursos por profesor limpian el caché antes de insertar**
(antes solo insertaban/reemplazaban por clave, así que un documento borrado en Firestore
quedaba "fantasma" para siempre en el caché de esos tres repos — `grades` y los cursos
por estudiante ya lo hacían bien desde el commit inicial).

`AppDatabase` usa `fallbackToDestructiveMigration(true)` desde la v1. **Pendiente**:
antes de subir la versión del esquema (agregar/quitar una columna o tabla), hay que
escribir una migración explícita o se borrará todo el caché local de todos los usuarios
sin aviso (ver sección 6).

`loans` **no** tiene caché Room — `LoanRepository` lee siempre directo de Firestore.

---

## 4. REGLAS DE SEGURIDAD (`firestore.rules` / `storage.rules`)

Los roles se leen del campo `role` en `users/{uid}` (funciones `isAdmin()`,
`isStaffOrAdmin()`, `isProfessor()`). **Importante**: el filtrado de qué pantallas/rutas
se muestran en la UI según el rol (`ServiceCatalog.kt`, los distintos `NavGraph`) es
**solo cosmético** — no es una barrera de seguridad real. La seguridad real vive
ÚNICA Y EXCLUSIVAMENTE en `firestore.rules` / `storage.rules`. Cualquier cambio de
permisos DEBE hacerse ahí, nunca asumir que "la UI ya lo bloquea".

### Cambios de seguridad aplicados el 2026-09-13 (ver entrada del changelog para detalle completo)

1. `users/{uid}`: lectura restringida a dueño / profesor / staff / admin (antes:
   cualquier usuario autenticado podía leer el directorio completo).
2. `notifications/{id}`: el contenido ya es inmutable salvo por admin (antes: cualquier
   usuario autenticado podía reescribir título/cuerpo/destinatario de cualquier
   notificación). El estado de "leído" ahora vive en `notifications/{id}/reads/{uid}`,
   un documento por usuario que cada quien solo puede leer/crear el suyo.
3. `grades` / `attendance`: crear/editar ahora exige que el profesor sea el **dueño**
   del curso (`courses/{courseId}.professorId == uid`), vía la función
   `ownsCourse(courseId)`. Antes cualquier profesor autenticado podía escribir notas o
   asistencia de cualquier curso ajeno.
4. `teacherEvaluations`: un estudiante ahora puede leer **sus propias** evaluaciones
   (para poder verificar si ya evaluó un curso) sin poder leer las de otros; antes solo
   staff/admin podían leer, lo que dejaba inoperante el control de "ya evaluado" desde
   el propio cliente.

**Estado de despliegue (actualizado 2026-09-13, tarde):**

- ✅ **`firestore.rules` YA ESTÁ DESPLEGADO en producción** (proyecto `appeafit-297d5`),
  con los 4 cambios de seguridad de arriba activos. Verificado con
  `firebase deploy --only firestore:rules` → `Deploy complete!`.
- ⏳ **`firestore.indexes.json` pendiente de desplegar.** No es un tema de seguridad
  (son solo índices de rendimiento de consultas), así que no es urgente. Falla con
  `403` al desplegar porque la cuenta de servicio usada para el deploy necesita el rol
  **"Administrador de índices de Cloud Datastore"** (`roles/datastore.indexAdmin`) en
  IAM del proyecto GCP. Comando para reintentar una vez agregado ese rol:
  ```
  firebase deploy --only firestore:indexes --project appeafit-297d5
  ```
- ⏳ **`storage.rules` pendiente de desplegar — bloqueado por facturación, no por
  código.** Firebase quitó el plan gratuito para Storage; el proyecto necesita que el
  cliente (EAFIT) active un plan de pago (agregar tarjeta / plan Blaze) antes de poder
  siquiera crear el bucket de Storage. **Decisión del 2026-09-13 (Santiago Guerrero
  Parrado): dejarlo pendiente hasta que el cliente pague la cuenta de Firebase.**
  Cuando se resuelva: ir a `https://console.firebase.google.com/project/appeafit-297d5/storage`,
  completar el asistente de "Comenzar" para crear el bucket, y luego
  `firebase deploy --only storage --project appeafit-297d5`.

Si tocas `firestore.rules`/`storage.rules` de nuevo en el futuro, recuerda que hay que
volver a desplegar — editar el archivo local no cambia nada en producción hasta correr
el comando de deploy (o pegar el contenido a mano en la consola de Firebase).

---

## 5. CONFIGURACIÓN Y BUILD

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Agregar una app Android con el paquete `co.edu.eafit.appeafit` (y opcionalmente
   `co.edu.eafit.appeafit.debug` para el build de depuración).
3. Descargar el `google-services.json` real y colocarlo en `app/google-services.json`
   (ese archivo **nunca** se sube al repositorio, ver `.gitignore`; usar
   `app/google-services.json.example` como referencia de formato — **no** contiene
   credenciales reales, es una plantilla).
4. Habilitar **Authentication → Correo electrónico/contraseña**.
5. Crear una base de datos **Firestore** (modo producción) y desplegar las reglas de
   este repo (ver comando arriba).
6. Habilitar **Storage** para las fotos de perfil.
7. (Opcional) Habilitar **Cloud Messaging** para notificaciones push.

Compilar: `./gradlew assembleDebug` (requiere Android SDK compileSdk 36 y JDK 17+; el
wrapper de Gradle está incluido). Para solo validar que el código Kotlin compila sin
generar el APK completo: `./gradlew compileDebugKotlin`.

**Nota de entorno (Windows + OneDrive)**: si `./gradlew` falla con un error tipo
`Cannot snapshot ...: not a regular file` en una tarea de KSP, es un problema de
sincronización de OneDrive con la carpeta `app/build/generated/`, no del código. Se
soluciona con `./gradlew --stop` y borrando `app/build/generated/ksp` antes de
recompilar.

**Desde 2026-09-13 (noche)**: `gradle/gradle-daemon-jvm.properties` (generado por
`updateDaemonJvm`) fija la JVM del *daemon* de Gradle en JDK 25 (vía el resolutor de
toolchains de foojay) — esto es independiente del JDK 17 que pide `compileOptions` para
compilar el código de la app, solo afecta con qué JVM corre el propio proceso de Gradle.
`gradle.properties` agregó `org.gradle.tooling.parallel=true` (sync paralelo del IDE,
Gradle 9.4+). Se agregó la dependencia `androidx.appcompat:appcompat:1.7.1` — **cabo
suelto**: no se usa en ningún archivo del código (`AppCompatDelegate` solo aparece
mencionado en comentarios explicando por qué NO se usó esa API para el selector de
idioma, ver punto 21 de la sección 8); es candidata a eliminarse de
`app/build.gradle.kts`/`libs.versions.toml` si nadie la termina usando.

Seguridad de secretos: `app/google-services.json`, keystores y cualquier credencial
están en `.gitignore`. Verificado el 2026-09-13: no hay credenciales reales
versionadas en el repo (`git ls-files` no muestra ningún `google-services.json` real,
solo el `.example`).

---

## 6. PENDIENTES CONOCIDOS (deuda técnica, a la fecha 2026-09-13)

Todo lo listado aquí fue identificado en la auditoría del 2026-09-13 (sección 7) y
**no** se corrigió todavía, por requerir infraestructura adicional (Cloud Functions),
ser de alcance mayor, o ser de severidad baja/cosmética. Quien retome el proyecto puede
usar esta lista como backlog priorizado.

### Pendientes de despliegue (no de código — ver sección 4 para detalle y comandos)
- **Índices de Firestore** (`firestore.indexes.json`): falta un rol de IAM
  (`roles/datastore.indexAdmin`) en la cuenta de servicio de deploy. No urgente, es
  solo rendimiento de consultas.
- **Reglas de Storage** (`storage.rules`): bloqueado porque Firebase ya no tiene plan
  gratuito para Storage — requiere que el cliente active facturación primero. Decisión
  2026-09-13: esperar a que EAFIT pague la cuenta de Firebase.

### Seguimiento de seguridad — cuenta de servicio usada para el deploy del 2026-09-13
Para poder desplegar `firestore.rules` sin una terminal interactiva disponible, se creó
una clave de cuenta de servicio (Firebase Admin SDK) del proyecto `appeafit-297d5` y se
le agregaron estos roles en IAM (además de los que ya tenía):
`Consumidor de Service Usage`, `Administrador de Firebase Rules`, `Administrador de
Firebase`. La clave privada (.json) se descargó, se usó una sola vez desde la máquina
local, y se borró inmediatamente después (`rm` confirmado, no quedó copia local). **No
se revocó/eliminó la clave ni los roles agregados en la consola de Google Cloud** — la
cuenta de servicio `firebase-adminsdk-fbsvc@appeafit-297d5.iam.gserviceaccount.com`
sigue teniendo esos permisos adicionales y podría existir la clave privada como
credencial activa en IAM → Cuentas de servicio → Claves. **Recomendación pendiente**:
alguien con acceso a la consola debería revisar
`https://console.cloud.google.com/iam-admin/serviceaccounts?project=appeafit-297d5`,
confirmar si esa clave sigue listada y, si ya no se necesita para despliegues futuros,
eliminarla (no los roles, que sí hacen falta para el próximo deploy de índices).

### Requieren infraestructura adicional (Cloud Functions / backend)
- **QR del carnet digital falsificable**: `CarnetScreen.kt` codifica
  `"EAFIT-ID:{uid}:{institutionalId}"` en texto plano, sin firma ni expiración.
  Cualquiera que conozca esos dos valores puede fabricar un QR idéntico. La solución
  correcta requiere firmar el contenido en el servidor (Cloud Function callable, con un
  secreto que nunca viaje al cliente) y opcionalmente rotarlo con expiración corta —
  esto no se puede resolver de forma segura solo del lado del cliente.
- **Límite de renovaciones de préstamos reforzado solo en el cliente**: aunque desde
  2026-09-13 `LoanRepository.renew()` usa una transacción de Firestore para evitar la
  condición de carrera y aplicar el límite (`MAX_LOAN_RENEWALS`), la regla
  `loans/{loanId}` sigue permitiendo que el estudiante dueño edite cualquier campo del
  préstamo (`allow update: if ... resource.data.studentId == myUid() ...`) — un cliente
  modificado podría poner `renewalCount` en 0 manualmente. Para cerrar esto del todo se
  necesitaría mover la renovación a una Cloud Function o acotar la regla a los campos
  exactos que puede tocar un estudiante.

### De alcance mayor (no se tocaron para no introducir riesgo sin poder probarlos en un emulador)
- **Anuncios de curso mezclados en el feed general**: `NewsItem` no distingue
  "institucional" de "solo para estudiantes matriculados en este curso" — cualquier
  anuncio publicado por un profesor (`AnnouncementsScreen`) aparece en el feed de
  TODOS los estudiantes, no solo los matriculados en ese curso. Arreglarlo bien
  requiere agregar un `courseId` opcional a `NewsItem`/Firestore y filtrar en
  `HomeViewModel`/`NewsRepository` cruzando contra las matrículas del estudiante.
- **`fallbackToDestructiveMigration`** en `AppDatabase.kt` desde la v1: no es un bug
  hoy, pero el día que se agregue/quite una columna o tabla sin escribir una migración
  Room explícita, se borrará todo el caché local de todos los usuarios sin aviso.
  Revisar esto ANTES del primer cambio de esquema.

### Dependencia sin usar (agregada 2026-09-13 noche)
- `androidx.appcompat:appcompat:1.7.1` se agregó a `app/build.gradle.kts` /
  `gradle/libs.versions.toml` durante el trabajo del selector de idioma, pero no se
  terminó usando (se optó por `attachBaseContext` manual en vez de
  `AppCompatDelegate.setApplicationLocales()`, ver punto 21 de la sección 8). Revisar si
  se elimina o si se usa de verdad como respaldo.

### Datos huérfanos en Firestore de producción (hallado 2026-09-13 noche, ver punto 22)
- Las colecciones `lostItems` (1 documento, "audífonos") y `spaces` (2 documentos,
  "Auditorio 2" y otro) **siguen existiendo en la base de datos real** (`appeafit-297d5`)
  a pesar de que la sección 1 y el punto 18 de la sección 8 afirman que "objetos
  perdidos" y "reserva de espacios" se eliminaron por completo, código + Firestore. Lo
  que se eliminó completo fue el código y las reglas (confirmado: `firestore.rules` ya
  no menciona esas colecciones); los documentos que ya existían antes de esa eliminación
  simplemente quedaron huérfanos, inaccesibles desde la app (sin reglas que los permitan)
  pero visibles en la consola. Borrarlos manualmente en
  `https://console.firebase.google.com/project/appeafit-297d5/firestore` (colecciones
  `lostItems` y `spaces`) es solo limpieza cosmética, sin ningún riesgo de seguridad ni
  urgencia.

### Cosméticas / bajo impacto (no se tocaron, prioridad baja)
- Buscador de `HomeScreen` es puramente decorativo (no filtra nada todavía).
- `EafitMessagingService.showNotification` usa un ID aleatorio (`Random.nextInt()`)
  por notificación — no agrupa/reemplaza avisos repetidos en la bandeja del sistema.
- `Role.fromId` degrada silenciosamente a `STUDENT` ante un valor de rol corrupto o
  desconocido en Firestore, sin ningún log de aviso.
- Duplicación visual del layout de header (logo + fondo navy) entre `LoginScreen`,
  `SplashScreen` y `CarnetScreen` — se podría extraer a un componente común.
- `TeacherEvaluationScreen` reutiliza `ScheduleViewModel` (pensado para horario) solo
  para obtener la lista de cursos del estudiante — funciona, pero es confuso.
- Fecha/hora en `SpaceReservationScreen` se validan con regex simple (`aaaa-mm-dd`,
  `HH:mm`), no con un `DatePicker`/`TimePicker` nativo — mejora de UX pendiente.

---

## 7. AUDITORÍA DEL 2026-09-13 (resumen ejecutivo)

El 2026-09-13 se hizo una revisión exhaustiva de todo el proyecto (código, reglas de
Firestore, configuración de build, recursos, y los archivos sueltos fuera del repo),
solicitada por Santiago Guerrero Parrado. El detalle completo de hallazgos y las
correcciones aplicadas están en la entrada del 2026-09-13 en la sección 8. Los hallazgos
de seguridad más graves (lectura abierta de `users/`, escritura abierta de
`notifications/`, profesores editando cursos ajenos, evaluación docente duplicable) y
varios bugs funcionales (estado de "leído" compartido, promedio ponderado incorrecto,
`changePassword` sin reautenticar, caché sin purgar, límite de `whereIn`, renovación de
préstamos sin límite, token FCM perdido en registro, cuentas huérfanas al fallar el
registro, falta de guardia de "último admin", reservas sin validar solapamiento ni
exponer cancelación propia, anuncios sin refrescar) **ya fueron corregidos** ese mismo
día — ver sección 6 para lo que sigue pendiente.

---

## 8. REGISTRO DE CAMBIOS (bitácora cronológica, append-only)

### 2026-09-12 — Santiago Guerrero Parrado

Creación inicial completa del proyecto AppEAFIT. Se construyó desde cero la aplicación
Android descrita en las secciones 1 a 5 de este documento:

- Estructura del proyecto Gradle (Kotlin DSL), catálogo de versiones
  (`gradle/libs.versions.toml`), configuración de módulo `app` (compileSdk/targetSdk 36,
  minSdk 26, JDK 17, Compose, KSP para Room, proguard).
- Integración completa de Firebase: Auth (correo institucional), Firestore, Storage,
  Cloud Messaging, Analytics — incluyendo `firestore.rules`, `storage.rules`,
  `firestore.indexes.json` y `firebase.json`.
- Arquitectura MVVM con inyección de dependencias manual (`AppContainer`,
  `ViewModelFactory`) — sin Hilt, deliberadamente, para minimizar puntos de fallo de
  anotación.
- Capa de datos completa: 13 repositorios (`data/repository/`), mappers Firestore↔Kotlin
  (`FirestoreMappers.kt`), caché offline-first con Room (`data/local/`) para perfil,
  cursos/horario, notas, noticias y calendario académico.
- Modelos de dominio (`domain/model/`) para las 11 entidades del negocio (usuarios,
  cursos, notas, asistencia, notificaciones, objetos perdidos, reservas, préstamos,
  noticias, calendario, evaluación docente).
- Toda la interfaz de usuario en Jetpack Compose + Material 3, con tema visual propio
  de EAFIT (azul `#146AEF`, navy, negro, blanco): pantallas de autenticación (login,
  registro, recuperar contraseña), navegación por rol (4 grafos: estudiante, profesor,
  staff, admin, más un grafo común), home, servicios, carnet digital con QR (ZXing),
  perfil, notificaciones, y las pantallas específicas de cada uno de los 4 roles
  descritas en la sección 1.
- README.md inicial con instrucciones de configuración de Firebase y compilación.
- Primer y único commit del historial de git a esta fecha:
  "Initial AppEAFIT: Android app with role-based access (student, professor, staff,
  admin)".

Este fue el estado íntegro del proyecto antes de cualquier auditoría o corrección
posterior. Todo lo que no aparezca modificado en una entrada posterior de este registro
sigue exactamente como se describe aquí.

---

### 2026-09-13 — Santiago Guerrero Parrado

**Contexto**: se pidió una revisión exhaustiva de absolutamente todo el proyecto
(código, reglas de Firestore, configuración, documentos y archivos sueltos), y luego
aplicar las correcciones de los problemas encontrados, además de crear este archivo de
bitácora.

**1. Auditoría completa.** Se revisó línea por línea el 100% del código Kotlin
(~85 archivos, ~6100 líneas), `firestore.rules`, `storage.rules`, `AndroidManifest.xml`,
Gradle/versiones, recursos, y se verificó que no hay credenciales reales versionadas.
Se identificaron hallazgos de seguridad críticos, bugs funcionales confirmados y deuda
técnica menor — el detalle completo de esa auditoría (incluyendo los hallazgos que NO
se corrigieron y por qué) quedó resumido en las secciones 6 y 7 de este mismo archivo.

**2. Correcciones de seguridad en `firestore.rules` y `firestore.indexes.json`.**
- `users/{uid}`: la lectura ya no es libre para cualquier autenticado; se restringió a
  dueño, profesor, staff o admin (función `myUid() == uid || isProfessor() ||
  isStaffOrAdmin()`), preservando el uso legítimo que hacen `CourseRepository
  .listEnrolledStudents()` (profesor viendo sus estudiantes) y el directorio de
  staff/admin.
- `notifications/{id}`: se eliminó el `allow update: if isSignedIn()` que permitía a
  cualquier usuario reescribir el contenido de cualquier notificación; ahora solo el
  admin puede crear/editar/borrar el documento. Se agregó la subcolección
  `notifications/{id}/reads/{uid}` (cada usuario solo puede leer/crear su propio
  marcador) para el estado de "leído" por usuario.
- `grades`/`attendance`: se agregó la función `ownsCourse(courseId)` (consulta
  `courses/{courseId}.professorId == request.auth.uid`) y se exige en `create`/`update`
  para profesores — ya no puede un profesor cualquiera escribir notas/asistencia de un
  curso ajeno.
- `teacherEvaluations`: se permitió que el propio estudiante lea sus evaluaciones
  (`resource.data.studentId == myUid()`), además de staff/admin — antes esto era
  imposible y `hasEvaluated()` recibía `PERMISSION_DENIED` siempre.
- `firestore.indexes.json`: se agregó un `fieldOverride` de collection-group para
  `reads.uid`, necesario para la consulta `collectionGroup("reads")
  .whereEqualTo("uid", ...)` que usa `NotificationRepository.listFor()`.
- **Pendiente de desplegar**: estos cambios están en los archivos del repo pero no
  tienen efecto hasta correr
  `firebase deploy --only firestore:rules,firestore:indexes,storage` contra el
  proyecto de Firebase real.

**3. Notificaciones — estado de "leído" por usuario (bug de integridad de datos).**
Archivos: `data/repository/NotificationRepository.kt`,
`ui/notifications/NotificationsViewModel.kt`. Antes, el campo `read` vivía en el
documento compartido de la notificación: el primer usuario que abría un broadcast lo
marcaba como leído para TODOS los demás destinatarios. Ahora `listFor()` combina el
contenido de la notificación con una consulta `collectionGroup("reads")` filtrada por
`uid`, y `markRead(notificationId, userId)` escribe en
`notifications/{id}/reads/{uid}` en vez de mutar el documento compartido.

**4. `AuthRepository.changePassword` — reautenticación (bug funcional que rompía el
flujo en la mayoría de sesiones reales).** Archivos: `data/repository/AuthRepository.kt`,
`ui/profile/ProfileViewModel.kt`, `ui/profile/ChangePasswordScreen.kt`,
`ui/auth/AuthViewModel.kt`, `core/util/Errors.kt` (nuevo). Firebase exige un login
"reciente" para cambiar la contraseña; antes se llamaba `updatePassword()` directo, sin
reautenticar, y fallaba con un error genérico en cualquier sesión no recién iniciada.
Ahora se pide la contraseña actual, se reautentica con
`EmailAuthProvider.getCredential(...)` + `reauthenticate()`, y luego se aplica la nueva
contraseña. De paso se extrajo `toFriendlyMessage()` de `AuthViewModel` a una función
compartida `Throwable.toFriendlyAuthMessage()` en `core/util/Errors.kt` (usada ahora
también por `ProfileViewModel`), con un caso nuevo para el error de "se requiere volver
a iniciar sesión".

**5. Promedio general de notas — fórmula inconsistente con la calculadora de GPA (bug
funcional).** Archivo: `ui/student/GradesViewModel.kt`. Antes `overallAverage` era un
promedio simple de los promedios por curso (ignorando créditos); ahora se pondera por
los créditos de cada curso (uniendo con `courseRepository.observeCachedForStudent`),
igual que hace `GpaCalculatorScreen` — antes ambas pantallas podían mostrar números
distintos para el mismo semestre.

**6. Caché local (Room) sin purgar entradas eliminadas remotamente (bug funcional).**
Archivos: `data/local/dao/Daos.kt` (nuevo método `CourseDao.clearForProfessor`),
`data/repository/NewsRepository.kt`, `data/repository/CalendarRepository.kt`,
`data/repository/CourseRepository.kt` (`refreshForProfessor`). Los tres repos ahora
limpian el caché antes de insertar (mismo patrón que ya usaban `GradeRepository` y
`CourseRepository.refreshForStudent`); antes, una noticia/evento/curso borrado en
Firestore quedaba visible para siempre en el dispositivo que ya lo tenía cacheado.

**7. Truncamiento silencioso de `whereIn` a 30 elementos (bug funcional, caso límite).**
Archivo: `data/repository/CourseRepository.kt` (`refreshForStudent`,
`listEnrolledStudents`). Ahora se consulta en bloques de 30 (`chunked(30)`) y se
combinan los resultados, en vez de recortar la lista con `.take(30)` sin aviso.

**8. Renovación de préstamos — sin límite ni condición de carrera (bug funcional).**
Archivos: `domain/model/Loan.kt` (nuevo campo `renewalCount`, constante
`MAX_LOAN_RENEWALS = 2`, propiedad `canRenew`), `data/repository/FirestoreMappers.kt`,
`data/repository/LoanRepository.kt`, `ui/student/LibraryScreen.kt`. Antes el nuevo
`dueAt` se calculaba a partir de un `currentDueAt` capturado en la UI (doble tap
disparaba escrituras duplicadas) y no había tope de renovaciones. Ahora
`LoanRepository.renew()` usa una transacción de Firestore que lee el estado real del
préstamo, valida que no esté devuelto ni supere `MAX_LOAN_RENEWALS`, y solo entonces
incrementa `dueAt`/`renewalCount` atómicamente; la UI deshabilita el botón y muestra
"Sin renovaciones" cuando corresponde, y muestra el error si la transacción lo rechaza.
**Nota de seguridad pendiente**: la regla de Firestore de `loans` sigue permitiendo que
el estudiante dueño edite cualquier campo, ver sección 6.

**9. Token FCM perdido si el perfil aún no existe (bug funcional).** Archivos:
`core/notifications/EafitMessagingService.kt`, `data/repository/UserRepository.kt`. El
token puede llegar (`onNewToken`) antes de que exista `users/{uid}` (justo después de
crear la cuenta en Auth, antes de crear el perfil en Firestore); ese `.update()` fallaba
100% silenciosamente. Ahora queda logueado, y además
`UserRepository.createUserProfile()` reintenta guardar el token vigente
(`FirebaseMessaging.getInstance().token`) justo después de crear el documento de perfil,
cubriendo esa carrera sin arriesgar crear un documento de usuario incompleto.

**10. Registro con cuenta huérfana si falla crear el perfil (hallazgo de seguridad/
integridad, severidad alta).** Archivos: `data/repository/AuthRepository.kt` (nuevo
método `deleteCurrentAccount()`), `ui/auth/AuthViewModel.kt`. Antes, si
`createUserProfile()` fallaba después de crear la cuenta en Firebase Auth, esa cuenta
quedaba "viva" en Auth sin perfil en Firestore, rompiendo cualquier regla basada en
`myRole()` sin ninguna forma de recuperarse salvo borrarla manualmente desde la consola.
Ahora, si falla la creación del perfil, se borra automáticamente la cuenta de Auth recién
creada para que la persona pueda simplemente reintentar el registro.

**11. Guardia de "último administrador" (hallazgo de seguridad/operativo).** Archivo:
`ui/admin/ManageUsersScreen.kt`. Antes un admin podía quitarse a sí mismo (o a otro) el
rol de admin, o desactivar la cuenta, con un solo clic — si era el único admin activo,
la app quedaba sin ningún administrador, recuperable solo manualmente desde la consola
de Firebase. Ahora, si la persona es la última cuenta admin activa, se muestra un
`AlertDialog` de confirmación explícita antes de aplicar el cambio de rol o de
desactivación.

**12. Reservas de espacio — solapamiento sin validar y sin botón de cancelación propia
(bugs funcionales).** Archivos: `data/repository/ReservationRepository.kt`,
`ui/student/ReservationViewModel.kt`, `ui/student/SpaceReservationScreen.kt`.
`create()` ahora consulta las reservas existentes del mismo espacio/fecha y rechaza la
nueva si se solapa en horario con una que no esté rechazada/cancelada (antes, dos
personas podían reservar la misma sala a la misma hora sin ningún aviso). Se agregó
también un botón "Cancelar reserva" para el estudiante dueño (la regla de Firestore ya
lo permitía desde el commit inicial, pero ninguna pantalla lo exponía), y validación
básica de formato de fecha (`aaaa-mm-dd`) y hora (`HH:mm`) que habilita/deshabilita el
botón de guardar y marca los campos en error — no reemplaza un `DatePicker`/`TimePicker`
nativo (ver sección 6, pendiente).

**13. Notificaciones push — el switch de Configuración no hacía nada (bug funcional).**
Archivo: `core/notifications/EafitMessagingService.kt`. `onMessageReceived` ahora lee
`SettingsRepository(applicationContext).notificationsEnabled` antes de mostrar la
notificación en la bandeja del sistema; antes ese switch solo guardaba una preferencia
en DataStore que ningún otro archivo del proyecto leía.

**14. `ManageAnnouncementsScreen` no refrescaba al abrir (bug funcional menor).**
Archivo: `ui/staff/ManageAnnouncementsScreen.kt`. Antes solo leía el caché local (Room)
al entrar, sin llamar nunca a `refresh()` (solo se refrescaba después de publicar/
borrar) — si el caché estaba vacío o desactualizado, la pantalla se veía vacía aunque
sí hubiera anuncios en Firestore. Ahora llama a `reload()` también al abrir la pantalla.

**Validación**: todos los cambios de Kotlin se compilaron exitosamente con
`./gradlew compileDebugKotlin` (incluye el procesamiento de anotaciones de Room vía
KSP) antes de darlos por terminados. No se ejecutaron pruebas de UI en un
emulador/dispositivo real ni se desplegaron las reglas de Firestore a un proyecto real
— eso queda pendiente para quien continúe (ver recordatorio de despliegue en la
sección 4).

**Qué NO se corrigió y por qué**: ver sección 6 ("Pendientes conocidos") — QR del
carnet falsificable (requiere Cloud Functions), anuncios de curso mezclados en el feed
general (requiere cambio de modelo de datos más amplio), y varios ítems cosméticos de
prioridad baja.

**15. Creación de este archivo `BITACORA.md`.** Documenta desde cero todo el proyecto
(arquitectura, estructura de carpetas, modelo de datos, reglas de seguridad,
configuración, pendientes) y dos entradas de este registro cronológico: la del
2026-09-12 (creación inicial del proyecto completo) y esta del 2026-09-13 (auditoría +
correcciones). A partir de ahora, toda persona o IA que trabaje en este proyecto debe
seguir la regla de la sección 0.

**16. Push a GitHub.** Se subió el commit con todas las correcciones de esta entrada
más `BITACORA.md` a `origin/main` (`https://github.com/eafitdesarrollo/AppEafit.git`).

**17. Despliegue de `firestore.rules` a producción.** Sin una terminal interactiva
disponible para `firebase login`, se generó una clave de cuenta de servicio (Firebase
Admin SDK) del proyecto `appeafit-297d5`, se le agregaron los roles de IAM necesarios
(ver sección 6, "Seguimiento de seguridad") y se corrió
`firebase deploy --only firestore:rules --project appeafit-297d5`, con resultado
`Deploy complete!`. **Las 4 correcciones de seguridad de `firestore.rules` descritas en
el punto 2 de esta misma entrada ya están activas en producción**, no solo en el
repositorio. La clave privada de la cuenta de servicio se usó una sola vez y se borró
inmediatamente después de terminar.

Quedaron pendientes de desplegar (no de código, ver sección 6 para el detalle): los
índices de `firestore.indexes.json` (falta un rol de IAM adicional, no urgente) y
`storage.rules` (Firebase ya no tiene plan gratuito para Storage; decisión de Santiago
Guerrero Parrado del 2026-09-13: esperar a que el cliente EAFIT pague la cuenta de
Firebase antes de continuar con esto).

**18. Rediseño completo de la interfaz (tema visual, animaciones, responsive) y ajuste
del catálogo de funciones a una universidad 100% virtual.** Pedido explícito de Santiago
Guerrero Parrado: la app se veía "monocromática, plana, con palabras cortadas en
diferentes dispositivos"; además había funciones que asumen presencialidad (no aplica,
EAFIT aquí es 100% virtual, sin campus físico).

- **Funciones eliminadas por completo** (código, rutas, catálogo de servicios, reglas de
  Firestore, mappers, `AppContainer`, strings — no solo la UI): **Objetos perdidos**
  (`LostItem`, `LostItemRepository`, `LostItemsScreen/ViewModel`, colección
  `lostItems`) y **Reserva de espacios** (`Reservation`/`Space`, `ReservationRepository`,
  `SpaceReservationScreen/ViewModel`, colecciones `spaces`/`reservations`). Decisión
  explícita del usuario: no tiene sentido reservar salas físicas ni reportar objetos
  perdidos en un campus que no existe.
- **`Course.ScheduleSlot` perdió el campo `room`** (salón físico) — un horario ahora solo
  tiene día/hora; las pantallas de horario (`ScheduleScreen`, `MyCoursesScreen`) muestran
  un indicador "Virtual" en su lugar. "Tomar asistencia" (profesor) SÍ se mantuvo —
  decisión explícita: sigue teniendo sentido para sesiones virtuales sincrónicas en vivo,
  solo se le quitó cualquier referencia a salón físico (nunca tuvo, se verificó).
- **Sistema de diseño reconstruido**: `ui/theme/Color.kt` (paleta ampliada — acento cálido
  `EafitCoral`/`EafitAmber` para romper el monocromatismo azul, superficies tonales
  light/dark completas, `GradientHero` reutilizable), `ui/theme/Theme.kt` (esquema de
  color M3 completo + `EafitShapes` consistente), `ui/theme/Type.kt` (tipografía con más
  peso/letter-spacing), `ui/theme/Motion.kt` (nuevo — `Modifier.pressScale`, constantes de
  duración `EafitMotion`). `ui/components/CommonComponents.kt` ampliado con `EafitCard`,
  `EafitButton`, `EafitOutlinedButton`, `GradientHeroBox`, y `EmptyState`/`ErrorState`/
  `OfflineBanner` ahora animados (antes aparecían de golpe).
- **Calendario académico reconstruido desde cero** (`AcademicCalendarScreen.kt`): antes
  era solo una lista plana de eventos; ahora es un calendario mensual interactivo real
  (grilla con navegación entre meses, día seleccionable, indicador de qué días tienen
  eventos) con la lista de eventos del día elegido debajo, con transiciones animadas.
- **Transiciones de navegación**: `EafitNavHost.kt` y `MainScreen.kt` ahora animan el
  cambio entre pantallas y entre pestañas del bottom bar (antes cambiaban de golpe, sin
  transición).
- **Fix sistemático de "texto cortado en distintos dispositivos"**: se revisó cada `Row`
  con un `Text` junto a un ícono/switch/badge fijo (nombres de usuario, cursos, noticias,
  anuncios, etc.) en las ~40 pantallas de la app, agregando `Modifier.weight(1f)` +
  `maxLines` + `overflow = TextOverflow.Ellipsis` donde faltaba — antes un nombre largo
  podía desbordar el ancho de la pantalla en vez de recortarse con puntos suspensivos.
- **Cómo se hizo**: dado el tamaño (todo de una sola vez, por decisión explícita del
  usuario), se repartió el trabajo en 4 agentes en paralelo, cada uno con un grupo fijo
  de pantallas y las mismas instrucciones de estilo, después de que el sistema de diseño
  base y el calendario quedaran terminados de forma centralizada (para evitar
  inconsistencias entre agentes). Dos de los cuatro agentes fallaron a mitad de camino
  por un límite de uso de la cuenta (rate limit); los archivos que dejaron a medio hacer
  (`ManageUsersScreen.kt`, `BroadcastScreen.kt`, `StatsScreen.kt`, `LibraryScreen.kt`,
  `ScheduleScreen.kt`, `TeacherEvaluationScreen.kt`) se terminaron directamente después de
  verificar exactamente qué archivos habían quedado sin tocar.
- **Validación**: `./gradlew compileDebugKotlin` y `./gradlew assembleDebug` (APK debug
  completo) exitosos después del rediseño completo. No se probó en un emulador/dispositivo
  real — eso queda pendiente para quien continúe (ver sección 6).
- **Pendiente de decisión futura** (no se tocó, fuera de alcance de este pedido): revisar
  si "Biblioteca" (préstamos) sigue teniendo sentido tal cual está planteada (¿libros
  físicos por correo, o licencias de e-books?) dado que la universidad es 100% virtual —
  no se eliminó ni se cambió porque no fue parte de lo pedido, pero vale la pena
  revisarlo con el mismo criterio que se aplicó a objetos perdidos/reserva de espacios.

**19. Se instaló y corrió la app en un dispositivo físico real por primera vez**
(conectado por USB con depuración habilitada — un Oppo/OnePlus modelo CPH2579), vía
`adb`/`./gradlew installDebug`. Se verificó por `logcat` que arranca sin crashes. Esta
fue la primera vez que se prueba la app fuera de una compilación — todo lo validado
antes (secciones previas) era solo a nivel de compilación, no de comportamiento real en
pantalla.

**20. Se recrearon las 4 cuentas de prueba (demo) con una contraseña conocida para
todos los roles.** A petición de Santiago Guerrero Parrado: se **borraron por completo**
(Firebase Auth + el documento correspondiente en Firestore `users/{uid}`) las 4 cuentas
demo que existían, y se crearon 4 cuentas nuevas con los mismos correos y los mismos
datos de perfil (nombre, programa, código institucional), pero con una contraseña nueva
e igual para las 4. Se hizo con un script de Node usando el SDK de administración de
Firebase (`firebase-admin`) y una clave de cuenta de servicio generada solo para esto y
borrada inmediatamente después de usarla (mismo patrón de higiene que en el punto 17).

**Credenciales de prueba (a partir del 2026-09-13) — usar para probar los 4 roles:**

| Rol | Correo institucional | Contraseña |
|---|---|---|
| Estudiante | `estudiante.demo@eafit.edu.co` | `310322snGQ@` |
| Profesor | `profesor.demo@eafit.edu.co` | `310322snGQ@` |
| Administrativo (staff) | `administrativo.demo@eafit.edu.co` | `310322snGQ@` |
| Administrador (admin) | `admin.demo@eafit.edu.co` | `310322snGQ@` |

> ⚠️ **Advertencia de seguridad, léela antes de asumir que esto es seguro dejarlo así**:
> el repositorio `eafitdesarrollo/AppEafit` en GitHub es **público**. Esta contraseña
> queda visible para cualquiera en internet en cuanto este archivo se suba (y para
> siempre en el historial de git, aunque después se borre de aquí). Es aceptable
> ÚNICAMENTE porque son 4 cuentas de prueba ficticias sin datos reales de estudiantes.
> Antes de que este proyecto maneje datos reales o el repo deje de ser un prototipo:
> 1. Cambiar esta contraseña (o borrar y recrear estas cuentas de nuevo) por una que no
>    esté en ningún historial de git público.
> 2. Considerar hacer privado el repositorio, o mover cualquier credencial futura a un
>    mecanismo que no sea un archivo versionado (gestor de secretos, variable de entorno
>    fuera del repo, etc.).
> Quien lea esto en el futuro: si esta contraseña ya no funciona, es porque alguien la
> rotó después de esta fecha — pregunta a Santiago Guerrero Parrado o revisa si hay una
> entrada posterior en este mismo registro que la reemplace.

---

### 2026-09-13 — Santiago Guerrero Parrado

**Contexto**: se pidió revisar absolutamente todo el proyecto otra vez — todo el código,
carpeta por carpeta y línea por línea, la base de datos de Firebase completa (consola) y
GitHub — y comparar contra esta misma BITACORA para ver si había quedado desactualizada,
actualizándola con lo que hiciera falta.

**0. Hallazgo inicial: había trabajo sin commitear ni documentar.** `git status` sobre
`repo/` mostró 27 archivos modificados y 4 rutas nuevas sin trackear
(`app/src/main/res/values-en/`, `app/src/main/res/xml/`,
`gradle/gradle-daemon-jvm.properties`, además de `.idea/` que es config de IDE y no
código) desde el último commit (`d8dee50`, "Rediseno completo de la UI...", 2026-09-13
15:29). Es decir: alguien (u otra sesión de IA) ya había hecho una cantidad importante de
trabajo nuevo — un selector de tema claro/oscuro, un selector de idioma inglés/español, y
varios ajustes visuales — pero nunca lo commiteó ni lo dejó anotado aquí. Todo lo que
sigue en esta entrada documenta ESE trabajo (no trabajo nuevo de esta sesión), ya
verificado y ahora sí comiteado.

**1. Selector de tema claro/oscuro/sistema.** Archivos: `data/repository/
SettingsRepository.kt` (nuevo enum `ThemeMode { SYSTEM, LIGHT, DARK }`, clave DataStore
`theme_mode`, `Flow<ThemeMode> themeMode` + `setThemeMode()`), `MainActivity.kt` (lee
`themeMode` con `collectAsStateWithLifecycle`, calcula `isDark` — `isSystemInDarkTheme()`
si es SYSTEM, o el valor forzado si es LIGHT/DARK — y se lo pasa a
`AppEafitTheme(darkTheme = isDark)`), `ui/profile/SettingsScreen.kt` (tarjeta nueva con
`SingleChoiceSegmentedButtonRow` de 3 opciones: Sistema/Claro/Oscuro). El cambio de tema
recompone en caliente, sin recrear la Activity.

**2. Corrección de contraste en la paleta oscura (bug visual, severidad media).**
Archivo: `ui/theme/Color.kt`. La primera versión del tema oscuro (creada en el rediseño
del punto 18 de la entrada anterior) tenía `SurfaceDark`/`SurfaceContainerDark`/etc. casi
del mismo tono que `BackgroundDark`, así que en modo oscuro toda la pantalla se veía como
un bloque negro plano sin que se distinguieran las tarjetas del fondo. Ahora hay un salto
de luminancia deliberado (`BackgroundDark` = `#0A0D18` muy oscuro, `SurfaceDark`/
`SurfaceContainerDark` = `#1D2545` notablemente más claro, `SurfaceContainerHighDark` =
`#2A3363`). **Verificado visualmente** (ver punto 6 de esta entrada): con el emulador en
modo oscuro del sistema, las tarjetas de la pantalla de login ahora se distinguen
claramente del fondo.

**3. Selector de idioma español/inglés/sistema — la app queda completamente
traducible.** Archivos y piezas:
- `data/repository/SettingsRepository.kt`: clave DataStore `language_tag` ("system"/
  "es"/"en"), `Flow<String> language` + `setLanguage()`, y `languageBlocking()` (lectura
  bloqueante con `runBlocking`, documentada como necesaria porque `attachBaseContext` no
  puede esperar un `Flow` suspendido antes de inflar recursos).
- `MainActivity.kt`: se sobreescribió `attachBaseContext()` — si el idioma guardado no es
  "system", crea un `Configuration` con el `Locale` correspondiente
  (`Locale.forLanguageTag(tag)`) y envuelve el contexto base con
  `createConfigurationContext()` antes de que se infle cualquier recurso. El comentario
  del código deja explícito por qué se hizo así y no con
  `AppCompatDelegate.setApplicationLocales()`: esa API no garantiza que el cambio se
  refleje de inmediato en un `ComponentActivity` plano (carrera con la propagación
  asíncrona del sistema).
- `AndroidManifest.xml`: se agregó `android:localeConfig="@xml/locales_config"` al
  `<application>`. Archivo nuevo `res/xml/locales_config.xml` declara los locales
  soportados (`es`, `en`).
- `res/values-en/strings.xml` (nuevo, 188 líneas — la misma cantidad que
  `values/strings.xml`): traducción completa al inglés de **todas** las cadenas de texto
  de la app, no solo las de la pantalla de Configuración.
- `res/values/strings.xml` ganó 102 líneas nuevas: muchas cadenas que antes estaban
  escritas directo en Kotlin (`Text("Título")`, `"Selecciona un curso"`,
  `"No tienes cursos asignados"`, etc., en unas 15 pantallas distintas) se extrajeron a
  `strings.xml` para que puedan traducirse — de lo contrario, cambiar el idioma a inglés
  habría dejado esos textos "fantasma" en español. Pantallas tocadas por este barrido:
  `HomeScreen`, `BroadcastScreen`, `ManageUsersScreen` (incluyendo los diálogos de "último
  administrador"), `StatsScreen`, `CarnetScreen`, `AnnouncementsScreen`,
  `GradeEntryScreen`, `MyCoursesScreen`, `ChangePasswordScreen`,
  `ManageAnnouncementsScreen`, `AcademicCalendarScreen`, `GpaCalculatorScreen`,
  `GradesScreen`, `ScheduleScreen`, `SettingsScreen`.
- `ui/services/ServiceCatalog.kt` y `ServicesScreen.kt`: los nombres de los grupos de
  servicios (antes strings sueltos como `"Académico"`, `"Docencia"`, `"Institucional"`,
  `"Administración"` directo en el catálogo) ahora son `groupResId: Int` (recursos
  `R.string.service_group_*`), traducibles.
- `ui/components/CommonComponents.kt`: nueva función `Role.displayLabel()` (composable,
  usa `stringResource`) que traduce el nombre del rol; reemplaza el uso directo de
  `Role.label` (que sigue existiendo y sigue en español, porque también se usa para
  lógica de negocio/Firestore, no solo para mostrarlo en pantalla) en `RoleBadge`,
  `BroadcastScreen`, `ManageUsersScreen` y `CarnetScreen`.
- `ui/student/AcademicCalendarScreen.kt` (bug de integridad de i18n): antes los nombres de
  mes y día del calendario académico se generaban siempre con un `Locale.forLanguageTag
  ("es-CO")` fijo a nivel de archivo — así que aunque cambiaras el idioma de la app a
  inglés, el calendario seguía mostrando "Septiembre", "Lunes", etc. Ahora una función
  `currentLocale() = Locale.getDefault()` se llama en cada uso (no se cachea en un `val`
  de nivel de archivo) para reflejar el idioma recién aplicado por
  `MainActivity.attachBaseContext`.

**4. Ajustes visuales menores (parte del mismo trabajo, sin commit ni documentar
todavía):**
- `ui/components/CommonComponents.kt` (`ServiceCard`): dejó de forzar `aspectRatio(1f)`
  (cuadrado perfecto) y ahora usa `heightIn(min = 116.dp)` con `maxLines = 3` en la
  etiqueta — los nombres de servicio largos tenían poco espacio para el texto en el
  cuadrado fijo. El círculo del ícono cambió de un fondo con gradiente muy tenue
  (`alpha = 0.16f`) + ícono del color primario, a un gradiente sólido (primary→tertiary)
  con ícono blanco — más contraste, se ve mejor también en modo oscuro.
- `ui/home/HomeScreen.kt`: la sección "Actualidad EAFIT" antes desaparecía por completo
  si no había noticias (`if (news.isNotEmpty())` envolvía TODA la sección, encabezado
  incluido); ahora el encabezado siempre se muestra y, si no hay noticias, se ve un
  estado vacío con ícono y mensaje en vez de que la sección entera desaparezca.
- `ui/profile/ProfileScreen.kt`: el botón "Cerrar sesión" (el único `ProfileMenuItem` con
  `highlighted = true`) cambió de `colorScheme.primary`/`onPrimary` (el azul de marca) a
  `colorScheme.errorContainer`/`onErrorContainer` — semánticamente tiene más sentido para
  una acción de este tipo, y es más legible sobre las superficies oscuras nuevas del
  punto 2.
- `ui/carnet/CarnetScreen.kt`: el avatar por defecto (usuario sin foto de perfil) pasó de
  círculo semitransparente blanco con inicial blanca (bajo contraste) a círculo blanco
  sólido con inicial en `colorScheme.primary` y negrita.

**5. Ajustes de build (ver también sección 5 y 6 de este archivo, ya actualizadas):**
`app/build.gradle.kts`/`gradle/libs.versions.toml` agregaron la dependencia
`androidx.appcompat:appcompat:1.7.1`, que **no se usa en ningún archivo** (queda como
pendiente en la sección 6). `gradle/gradle-daemon-jvm.properties` (nuevo, generado por
`updateDaemonJvm`) fija el daemon de Gradle a JDK 25. `gradle.properties` agregó
`org.gradle.tooling.parallel=true`.

**6. Verificación de todo lo anterior:**
- `./gradlew assembleDebug` — `BUILD SUCCESSFUL` (compila sin errores con todos estos
  cambios).
- Sin el celular físico disponible por USB, se instaló el APK debug en el emulador de
  Android Studio (**AVD `Pixel_8_API_36`**) vía `adb install` y se abrió la app
  (`co.edu.eafit.appeafit.debug/co.edu.eafit.appeafit.MainActivity` — el paquete de debug
  lleva el sufijo `.debug`, la actividad NO). Con el idioma del sistema del emulador en
  inglés, la pantalla de login apareció **completamente traducida** ("Welcome to EAFIT",
  "Sign in with your institutional email", etc.), confirmando que el selector de idioma
  en modo "sistema" funciona de punta a punta. Activando el modo oscuro del sistema
  (`adb shell cmd uimode night yes`) se confirmó visualmente la corrección de contraste
  del punto 2 (fondo casi negro con tarjetas claramente más claras, ya no un bloque
  plano).
- **No se probó el login real ni las pantallas autenticadas** (Configuración, Home,
  Perfil ya logueado, etc.): el entorno de este agente bloqueó automáticamente escribir
  la contraseña de la cuenta de prueba en el emulador (clasificador de seguridad que
  trata cualquier escritura de contraseña como "exploración de credenciales", sin
  distinguir que era una cuenta demo ficticia ya pública en este mismo archivo). Queda
  pendiente para quien continúe: iniciar sesión manualmente (con teclado/mouse reales, no
  automatización) con `estudiante.demo@eafit.edu.co` y entrar a Configuración para
  confirmar visualmente los tres segmented buttons (notificaciones/tema/idioma) y que no
  quede ningún texto "fantasma" en español al cambiar a inglés en pantallas autenticadas.
- **Verificación cruzada contra Firebase (consola, vía navegador) y GitHub (`gh` CLI):**
  se confirmó que el estado real de producción sigue coincidiendo exactamente con lo que
  ya documentaba este archivo, sin ninguna discrepancia:
  - `firestore.rules` desplegado en la consola tiene el mismo contenido que el archivo
    del repo (mismas funciones `isAdmin`/`ownsCourse`/etc.), con fecha de despliegue
    "ayer" (coincide con el 2026-09-13 documentado en la sección 4).
  - `firestore.indexes.json` sigue sin desplegar — la pestaña "Índices" de la consola no
    tiene ningún índice manual creado. Coincide con el pendiente documentado.
  - Storage sigue bloqueado por falta de plan de facturación Blaze (mensaje "Storage
    requiere una cuenta de facturación" en la consola). Coincide con el pendiente
    documentado.
  - Las 4 cuentas demo (`admin.demo@`, `administrativo.demo@`, `profesor.demo@`,
    `estudiante.demo@…@eafit.edu.co`) existen en Authentication, creadas el 13 sept 2026.
    Coincide con lo documentado en el punto 20 de la entrada anterior.
  - GitHub: el repo `eafitdesarrollo/AppEafit` sigue público, rama única `main`, sin
    Pull Requests ni Issues abiertos, sin workflows de CI configurados (`.github/` no
    existe). El último push a `origin/main` coincide con el commit `d8dee50`.

**7. Commit y push.** Se commitearon todos los archivos listados en el punto 0 de esta
entrada (los 27 modificados + los 4 nuevos, sin contar `.idea/` que no se sube) junto con
esta actualización de `BITACORA.md`, y se hizo push a
`https://github.com/eafitdesarrollo/AppEafit.git` (rama `main`).

**Qué NO se hizo en esta entrada** (para quien continúe): no se probó el login real ni
las pantallas autenticadas en el emulador (ver punto 6); no se removió la dependencia
`androidx.appcompat` sin usar (ver sección 6, pendientes); no se investigó qué sesión
anterior dejó este trabajo sin commitear ni por qué — no hay forma de saberlo desde el
historial de git porque nunca se llegó a commitear.

---

### 2026-09-13 — Santiago Guerrero Parrado

**Contexto**: continuación directa de la entrada anterior (mismo día, misma revisión
completa del proyecto pedida por Santiago Guerrero Parrado). Esta entrada cubre lo que la
anterior dejó explícitamente pendiente ("Qué NO se hizo en esta entrada") más dos
hallazgos nuevos de comparar la BITACORA contra el estado real de Firebase.

**21. Verificación end-to-end en el emulador, incluyendo login real (pendiente de la
entrada anterior).** Con el AVD `Pixel_8_API_36` (celular físico seguía sin USB
disponible), se instaló el APK debug y se inició sesión con
`estudiante.demo@eafit.edu.co`. Confirmado visualmente, todo correcto:
- Login, Home, Perfil y Configuración cargan **completamente traducidos al inglés**
  (idioma del sistema del emulador), incluyendo textos que antes eran hardcodeados
  (`"Great to see you!"`, `"Favorites"`, `"EAFIT News"`, el badge de rol `"Student"`
  vía `Role.displayLabel()`, y el botón `"Sign out"` ya en su nuevo color de error).
  El contenido que sí sigue en español correctamente (títulos y categorías de noticias
  reales, ej. "Actualización de horarios de biblioteca" / "Biblioteca") es dato de
  Firestore, no texto de la UI — no debía traducirse.
- En **Perfil → Configuración** los tres controles (notificaciones / apariencia /
  idioma) se ven y funcionan como describe el punto 21 de la entrada anterior. Se
  cambió el selector de apariencia a **Oscuro** en vivo (sin reiniciar la Activity) y
  se confirmó la corrección de contraste: las tres tarjetas (`Push notifications`,
  `Appearance`, `Language`) se distinguen claramente del fondo casi negro — ya no es
  "un bloque negro plano".
- No se encontró ningún texto "fantasma" en español en las pantallas recorridas
  (Login, Home, Perfil, Configuración).

**22. Dos hallazgos de desactualización en esta misma BITACORA, encontrados al comparar
contra Firebase Console y el propio repo (no cambios de código, solo documentación +
un archivo de configuración muerto):**
- **Datos huérfanos en Firestore de producción.** La sección 1 y el punto 18 de la
  entrada del 2026-09-13 (tarde) afirman que "objetos perdidos" y "reserva de espacios"
  se eliminaron por completo, código + Firestore. Revisando la consola de
  `appeafit-297d5` directamente, las colecciones `lostItems` (1 documento) y `spaces`
  (2 documentos) **todavía existen** — lo que se eliminó por completo fue el código y
  las reglas (`firestore.rules` ya no las menciona, así que son inaccesibles desde la
  app), pero los documentos que ya existían antes de esa eliminación nunca se borraron
  de la base de datos real. Sin riesgo de seguridad (nada puede leerlos/escribirlos ya),
  pero la afirmación de "eliminado por completo" era imprecisa. Documentado como
  pendiente de limpieza manual en la sección 6; no se borraron los documentos en esta
  entrada porque la automatización de navegador disponible no logró completar la
  acción de forma confiable (la consola de Firebase no respondió de forma consistente a
  los clics automatizados) y no vale la pena forzarlo para 3 documentos de prueba sin
  ningún impacto.
- **Índice muerto en `firestore.indexes.json`.** El archivo todavía tenía un índice
  compuesto para `collectionGroup: "reservations"` (campos `userId`/`createdAt`), una
  colección que ya no existe ni en el código ni en las reglas desde el punto 18 de la
  entrada anterior. Se quitó ese bloque del archivo en esta entrada. Como los índices de
  este archivo **todavía no se han desplegado** (pendiente documentado en la sección 4,
  falta el rol de IAM), este índice muerto nunca llegó a producción — es limpieza pura
  del repo, no un cambio de comportamiento.

**23. Verificación adicional (sin cambios encontrados):** se releyó `firestore.rules`
desplegado en la consola línea por línea contra el archivo del repo — coinciden. Se
confirmó que la carpeta padre (`Movil/EAFIT/`) solo contiene `repo/` y `.claude/` (nada
de código), consistente con lo ya corregido en la entrada anterior sobre las 14 capturas
de pantalla que ya no están.

**24. Commit y push.** Se commiteó `firestore.indexes.json` (índice muerto eliminado) y
esta actualización de `BITACORA.md`, y se hizo push a
`https://github.com/eafitdesarrollo/AppEafit.git` (rama `main`).

**Qué queda pendiente** (ver también sección 6): borrar manualmente los 3 documentos
huérfanos de `lostItems`/`spaces` desde la consola de Firebase; decidir si se elimina la
dependencia `androidx.appcompat` sin usar; desplegar `firestore.indexes.json` (falta el
rol de IAM) y `storage.rules` (falta que EAFIT active facturación).

---

### 2026-09-14 — Santiago Guerrero Parrado

**25. Orientación de pantalla bloqueada en vertical (pedido explícito de Santiago
Guerrero Parrado: "que no permita que el celular se rote... que siempre quede
vertical").** Archivo: `app/src/main/AndroidManifest.xml`. Se agregó
`android:screenOrientation="portrait"` a la única `<activity>` de la app
(`MainActivity`) — al ser una app de una sola Activity (todas las pantallas son
composables dentro del mismo `NavHost`), esto es suficiente para bloquear la app
completa, no hace falta tocarlo pantalla por pantalla.

**Verificación**: se compiló (`./gradlew assembleDebug`, exitoso) y se instaló en el
emulador `Pixel_8_API_36`. Con la app abierta y con sesión ya iniciada (cuenta demo de
estudiante), se forzó rotación a horizontal a nivel de sistema
(`adb shell settings put system accelerometer_rotation 0` +
`adb shell settings put system user_rotation 1`) y se confirmó por captura de pantalla
que la app **se mantiene en vertical** tanto en la pantalla de splash como en Home — no
rota. Se restauró `accelerometer_rotation` a `1` (auto-rotar) al terminar, para no dejar
esa configuración del sistema del emulador alterada.

**Impacto**: ninguna pantalla de la app estaba diseñada pensando en horizontal (no hay
layouts alternativos en `res/layout-land/` ni nada equivalente en Compose), así que este
cambio solo evita que el usuario caiga sin querer en un estado no soportado — no quita
ninguna funcionalidad.

**Commit y push**: cambio commiteado y subido a
`https://github.com/eafitdesarrollo/AppEafit.git` (rama `main`).

---

### 2026-09-15 — Santiago Guerrero Parrado

**26. Rediseño del borde de las cabeceras "hero" a un efecto de "pintura derritiéndose"
(goterones), y esquinas redondeadas en la barra de navegación inferior.** Pedido
explícito de Santiago Guerrero Parrado: la app se seguía viendo "muy cuadrada" incluso
después del rediseño del punto 18 (entrada del 2026-09-13) — el corte recto de las
cabeceras y la barra de navegación inferior era el problema. Se pidió específicamente un
efecto de "que se derritiera el color principal sobre otro" con una imagen de referencia
(un borde de pintura azul goteando), aclarando que debía mantenerse la paleta de EAFIT
(no colores nuevos) y que era la SILUETA del borde la que debía cambiar, no un fondo con
manchas de color de otros tonos (primer intento descartado, ver abajo).

- **Archivo nuevo `ui/theme/DripShape.kt`**: clase `DripShape` que implementa `Shape`
  (`createOutline`) — genera un `Path` con `dripCount` (7 por defecto) goterones
  redondeados de largo variable (patrón fijo `dripDepthPattern`, no aleatorio, para que el
  resultado sea siempre igual) colgando de un borde inferior, usando curvas Bézier
  cúbicas (`cubicTo`) para que cada goterón termine en punta redondeada, no en pico. El
  tamaño del "zona de goteo" (`dripZoneHeight`, 32.dp por defecto) es una medida fija en
  dp, no un porcentaje del alto total, para que se vea igual de grande en una cabecera
  corta (Home) que en una pantalla completa (Splash/Carnet).
- **`ui/components/CommonComponents.kt` (`GradientHeroBox`)**: el parámetro
  `roundedBottom: Boolean` (que aplicaba una esquina simplemente redondeada,
  `RoundedCornerShape`) se reemplazó por `dripBottom: Boolean` que aplica `DripShape()`
  en vez de un corte recto (`RectangleShape`) — usado por defecto en `LoginScreen` y
  `HomeScreen`. `SplashScreen.kt` y `CarnetScreen.kt` pasan `dripBottom = false` porque su
  `GradientHeroBox` ocupa el 100% de la pantalla (no hay "fondo claro" debajo contra el
  cual goteé, se vería como un hueco pegado al borde físico del dispositivo).
- **`ui/home/HomeScreen.kt`**: el `Row` de la cabecera (saludo + campana de
  notificaciones) tenía solo `padding(vertical = 16.dp)`, insuficiente para que los
  goterones de 32dp no cortaran el texto "Estudiante" — se cambió a
  `padding(start=20.dp, end=20.dp, top=16.dp, bottom=40.dp)` para darle aire al borde de
  goteo. **Verificado visualmente en el emulador** que ya no hay texto cortado.
- **`ui/components/EafitBottomBar.kt`**: la `NavigationBar` (M3, rectángulo plano por
  defecto) ahora tiene `Modifier.clip(RoundedCornerShape(topStart=24.dp, topEnd=24.dp))` —
  mismo pedido de "que no se vea cuadrado" aplicado a la barra inferior, con un
  tratamiento más sutil (esquinas redondeadas, no goteo, porque la barra es demasiado
  corta en alto para un goteo legible).
- **Intento descartado (parte de esta misma tarea, revertido antes de commitear)**: la
  primera versión de este cambio usaba un fondo de "blobs" (manchas orgánicas con
  gradiente radial y `BlendMode.Screen`, en un archivo `ui/theme/MeltingBackground.kt`
  que se llegó a compilar y probar) en colores cian/coral/ámbar detrás del contenido de
  `GradientHeroBox`, en vez de cambiar la silueta del borde. Santiago Guerrero Parrado
  aclaró que no era eso lo que pedía (colores fuera de la paleta EAFIT y un efecto de
  "manchas" en vez de "goteo de borde"), así que ese archivo se borró por completo y se
  reemplazó por el enfoque de `DripShape` de arriba — no quedó ningún rastro de la
  primera versión en el código ni en este commit.

**Verificación**: `./gradlew compileDebugKotlin`/`assembleDebug` exitosos en cada
iteración. Instalado en el emulador `Pixel_8_API_36` (sin celular físico conectado):
confirmado visualmente que Login y Home muestran el borde de goteo en los azules de EAFIT
(navy → azul, igual que `GradientHero`) sin cortar ningún texto, que Splash y Carnet
siguen con corte recto (sin goteo, como corresponde a pantalla completa), y que la barra
de navegación inferior tiene las esquinas superiores redondeadas. Se hizo login real con
la cuenta demo de estudiante para verificar Home con sesión iniciada.

**Commit y push**: cambios commiteados y subidos a
`https://github.com/eafitdesarrollo/AppEafit.git` (rama `main`).
