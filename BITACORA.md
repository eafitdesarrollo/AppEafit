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
| **Estudiante** | Notas, horario, calculadora de promedio, calendario académico, evaluación docente, biblioteca (préstamos), objetos perdidos, reserva de espacios, carnet digital con QR |
| **Profesor** | Mis cursos, tomar asistencia, cargar notas, publicar anuncios de curso |
| **Administrativo (staff)** | Directorio institucional, gestionar anuncios, gestionar reservas de espacios, gestionar objetos perdidos |
| **Administrador (admin)** | Gestión de usuarios y roles, gestión de contenido, notificaciones push masivas, estadísticas |

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
  padre (`Movil/EAFIT/`) NO es un repo git, solo contiene `repo/` más 14 capturas de
  pantalla sueltas (`WhatsApp Image ....jpeg`) que son referencias visuales de la app
  institucional oficial de EAFIT (mockups/QA), no parte del código.
- Al 2026-09-13 el historial de git tiene un solo commit inicial
  ("Initial AppEAFIT: Android app with role-based access...").

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
  repository/LostItemRepository.kt        Objetos perdidos
  repository/ReservationRepository.kt     Espacios y reservas (con validación de solapamiento de horario)
  repository/LoanRepository.kt            Préstamos de biblioteca (renovación con límite y transacción atómica)
  repository/SettingsRepository.kt        Preferencias locales (DataStore) — ej. notificaciones push activadas/desactivadas

domain/model/                            Data classes puras (sin dependencias de Firebase/Room): AppNotification, Attendance,
                                          CalendarEvent, Course/Enrollment/ScheduleSlot, Grade, Loan, LostItem, NewsItem,
                                          Reservation/Space, Role, User

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
  student/                AcademicCalendarScreen, GpaCalculatorScreen, GradesScreen + GradesViewModel (promedio ponderado por
                         créditos), LibraryScreen (renovación de préstamos con límite), LostItemsScreen + LostItemsViewModel,
                         ReservationViewModel + SpaceReservationScreen (con cancelación propia y validación de solapamiento),
                         ScheduleScreen + ScheduleViewModel, TeacherEvaluationScreen
  professor/              MyCoursesScreen, AttendanceScreen, GradeEntryScreen, AnnouncementsScreen
  staff/                  DirectoryScreen, ManageAnnouncementsScreen (refresca al abrir)
  admin/                  ManageUsersScreen (con guardia de "último admin"), BroadcastScreen, StatsScreen
```

Recursos Android (`app/src/main/res/`): `values/strings.xml` (todos los textos en
español), `values/colors.xml` (paleta EAFIT), `values/themes.xml`, `drawable/`
(íconos vectoriales), `mipmap-anydpi-v26/` (ícono de launcher).

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
| `lostItems/{id}` | auto | title, description, location, imageUrl, reportedBy, status, createdAt | quien reporta (create), reportante o staff/admin (update) |
| `spaces/{id}` | auto | name, location, capacity, type | staff/admin |
| `reservations/{id}` | auto | spaceId, spaceName, userId, userName, date, startTime, endTime, purpose, status, createdAt | el propio usuario (create/cancelar), staff/admin (aprobar/rechazar) |
| `loans/{id}` | auto | studentId, itemTitle, loanedAt, dueAt, returned, renewalCount | staff/admin (create/delete), estudiante dueño o staff/admin (update, incl. renovación) |
| `notifications/{id}` | auto | title, body, targetRole, targetUserId, createdAt | solo admin (desde 2026-09-13; antes cualquier usuario podía editar el contenido) |
| `notifications/{id}/reads/{uid}` | uid del usuario | uid, readAt | cada usuario, solo su propio marcador (NUEVO 2026-09-13 — reemplaza el campo `read` compartido) |

Las reglas completas están en `firestore.rules` (ver sección 4 para el detalle de qué
cambió y por qué). Los índices compuestos y de collection-group están en
`firestore.indexes.json`.

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
