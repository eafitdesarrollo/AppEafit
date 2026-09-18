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
  solo rendimiento de consultas — **excepto** el índice de grupo de colección
  `reads`/`uid` creado manualmente el 2026-09-16 (punto 30, sección 8), que sí es
  necesario para que funcione la pantalla de Notificaciones y **no** está reflejado en
  `firestore.indexes.json`. Si se recrea el proyecto de Firestore desde cero hay que
  volver a crear esa excepción de índice a mano (Firestore → Índices → Automáticos →
  Agregar exención) o agregarla al archivo de índices primero.
- **Reglas de Storage** (`storage.rules`): bloqueado porque Firebase ya no tiene plan
  gratuito para Storage — requiere que el cliente active facturación primero. Decisión
  2026-09-13: esperar a que EAFIT pague la cuenta de Firebase. **2026-09-17**: mientras
  tanto se conectó ImageKit.io como reemplazo temporal de solo imágenes (fotos de
  perfil, imágenes de anuncios) — ver punto 33 de la sección 8 para el detalle
  completo y los pasos exactos para revertir a Firebase Storage cuando se pague.

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
- **`fallbackToDestructiveMigration`** en `AppDatabase.kt`: sigue sin haber ninguna
  migración Room explícita escrita. Ya se ejerció una vez este camino (v1 → v2 el
  2026-09-15, al agregar `CachedGradeEntity.corte` — ver punto 27 de la sección 8) sin
  problema porque el proyecto todavía no tiene usuarios reales con datos que importe
  preservar. La próxima vez que se agregue/quite una columna o tabla, se volverá a
  borrar todo el caché local de todos los usuarios sin aviso — si para ese momento ya
  hay usuarios reales, esto ya no será aceptable y hay que escribir una migración
  explícita en vez de confiar en el fallback destructivo.

### Dependencia sin usar (agregada 2026-09-13 noche)
- `androidx.appcompat:appcompat:1.7.1` se agregó a `app/build.gradle.kts` /
  `gradle/libs.versions.toml` durante el trabajo del selector de idioma, pero no se
  terminó usando (se optó por `attachBaseContext` manual en vez de
  `AppCompatDelegate.setApplicationLocales()`, ver punto 21 de la sección 8). Revisar si
  se elimina o si se usa de verdad como respaldo.

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

---

### 2026-09-15 — Santiago Guerrero Parrado

**27. Calculadora de promedio rediseñada como simulador de notas reales por corte, y
publicación de notas por corte para el profesor.** Pedido explícito de Santiago Guerrero
Parrado con una captura de referencia: la calculadora debía dejar de ser una tabla manual
desconectada de los datos reales y pasar a mostrar las materias en las que el estudiante
está inscrito de verdad, con las notas de cada corte (1, 2, 3) que el profesor ya
publicó, permitiendo simular ediciones sin perder de vista el dato real.

- **Modelo de datos — nuevo campo `corte`.** `domain/model/Grade.kt` agregó
  `val corte: Int = 0` (0 = nota sin corte, compatibilidad con notas viejas del modelo
  libre de ítems). Se propagó a `data/local/entity/Entities.kt`
  (`CachedGradeEntity.corte`), `data/repository/FirestoreMappers.kt` (`toGrade()`/
  `toMap()`, usando `getLong("corte")` porque Firestore guarda los enteros como Long) y
  `data/repository/GradeRepository.kt` (`toDomain()`/`toEntity()`).
- **Cambio de esquema de Room — versión 2.** `data/local/AppDatabase.kt`: `version = 1`
  → `2`. Sin migración explícita: `fallbackToDestructiveMigration(true)` borra y recrea
  el caché local completo (de TODOS los repos, no solo notas) en el primer arranque tras
  actualizar — cada repositorio se re-sincroniza solo desde Firestore en su próximo
  `refresh()`, así que no hay pérdida de datos reales, solo un refresh extra la primera
  vez. Este es exactamente el escenario que la sección 6 llevaba desde el 2026-09-13
  marcado como "revisar ANTES del primer cambio de esquema" — ya revisado y aceptado.
- **`ui/student/GpaCalculatorViewModel.kt` (nuevo).** Combina
  `courseRepository.observeCachedForStudent` + `gradeRepository.observeCachedForStudent`
  y arma `CourseGradeRow(course, realCortes: Map<Int, Double>)` por materia — si el
  profesor publicó el mismo corte más de una vez (corrección), gana la nota más reciente
  por `date`. Dispara `refreshForStudent` de ambos repos en `init`, mismo patrón que
  `GradesViewModel`.
- **`ui/student/GpaCalculatorScreen.kt` (reescrita por completo).** Header
  `GradientHeroBox` con foto/nombre/programa del estudiante + insignia "SIMULADOR DE
  NOTAS"; por cada materia inscrita, una tarjeta con casillas C1/C2/C3 editables
  (`TextField` filled, sin borde) y un panel lateral "Nota final". El FAB, que antes era
  un "+" para agregar materias manuales, ahora es un bote de basura (`Icons.Filled
  .Delete`) que descarta cualquier simulación y vuelve a mostrar solo lo publicado.
  - **Simulación vs. dato real**: cada casilla se identifica por clave
    `"courseId:corte"`. Un `Set<String>` (`editedKeys`) registra qué casillas tocó el
    estudiante a mano — esas nunca se vuelven a pisar con el valor real que llegue
    después (Firestore es async: la primera emisión de la lista de materias casi
    siempre llega con las notas todavía vacías, antes de que termine el refresh). Las
    casillas NO editadas sí se mantienen sincronizadas con el valor real más reciente. El
    botón de reset limpia `editedKeys` y las casillas, y vuelve a sincronizar con lo real.
  - **Cuándo se muestra "Nota final" y "Promedio semestre"** (corregido en vivo probando
    en el celular físico, pedido explícito de Santiago Guerrero Parrado viéndolo
    funcionar): "Nota final" de una materia solo aparece cuando los **3** cortes tienen
    valor (real o simulado) — con 1 o 2 cortes llenos se muestra "—", no un promedio
    parcial. "Promedio semestre" solo aparece cuando **todas** las materias inscritas ya
    tienen su Nota final calculada (las 3 casillas llenas cada una) — si falta una sola
    materia, también muestra "—". Cuando sí se muestra, es un promedio ponderado por los
    créditos (`Course.credits`) de cada materia sobre el total de créditos de las
    materias que entran en la cuenta — mismo criterio de ponderación que
    `GradesViewModel.overallAverage` (documentado en el punto 5 del 2026-09-13).
- **`ui/professor/GradeEntryScreen.kt` (simplificada).** Se quitaron los campos libres
  `item` (nombre de la evaluación), `maxScore` y `weight`% — ahora el profesor elige el
  **corte** (1/2/3) con un `SingleChoiceSegmentedButtonRow` (mismo patrón visual que
  Configuración) en vez de escribir el nombre de la actividad a mano. Al guardar,
  `maxScore` queda fijo en `5.0` (escala EAFIT) y `weightPercent` en `100.0/3` (los 3
  cortes pesan igual) — no se le pide al profesor porque ninguna pantalla necesita
  mostrarlo hoy. `item` se sigue guardando como "Corte 1"/"Corte 2"/"Corte 3" para que
  `GradesScreen` (notas reales del estudiante) siga funcionando sin cambios.
- **`ui/navigation/StudentNavGraph.kt`**: `GpaCalculatorScreen` ahora recibe
  `container` y `user` (antes solo `onBack`), necesarios para el ViewModel y el header.
- **Strings**: se agregaron/quitaron claves en `values/strings.xml` y `values-en/
  strings.xml` en paralelo (siguen con la misma cantidad de líneas/claves, 136 cada uno)
  — nuevas: `gpa_semester_average`, `gpa_final_grade`, `gpa_simulator_badge`,
  `gpa_reset_simulation`, `gpa_empty_no_courses`, `grade_cut_short`,
  `grade_entry_cut_title/desc/1/2/3`. Se quitaron las viejas de la calculadora manual
  (`gpa_subject`, `gpa_credits_abbrev`, `gpa_grade`, `gpa_add_subject`,
  `gpa_weighted_average`) y de `GradeEntryScreen` (`grade_entry_item_name`,
  `grade_entry_max_score`, `grade_entry_weight`).

**28. Dos bugs encontrados probando en el celular físico real (no en emulador) — ambos
corregidos antes de dar la función por terminada:**
- **Las casillas nunca mostraban las notas reales publicadas.** Causa: la sincronización
  inicial (`syncDefaults`) solo rellenaba una casilla la primera vez que existía su
  clave, con el objetivo de no pisar una edición del estudiante — pero como la primera
  emisión de la lista de materias casi siempre llega ANTES de que el refresh de
  Firestore traiga las notas, esa primera sincronización rellenaba todo con `""`, y la
  segunda emisión (con las notas reales) ya no volvía a escribir porque la clave "ya
  existía". Arreglado separando "casilla con valor por defecto" de "casilla editada a
  mano" (`editedKeys`, ver punto 27).
- **"Nota final"/"Promedio semestre" nunca calculaban nada aunque las casillas sí
  mostraran texto.** Causa: `formatGrade()` usaba `Locale.getDefault()` — en un celular
  con locale español (Colombia) eso escribe "3,8" con **coma** decimal, pero
  `String.toDoubleOrNull()` de Kotlin solo entiende **punto**, así que el parseo fallaba
  siempre y `notaFinal()` devolvía `null` sin importar qué hubiera escrito en las
  casillas. Arreglado: `formatGrade()` pasó a usar `Locale.US` (punto, consistente sin
  importar el idioma del teléfono) y se agregó `parseGrade()`, que normaliza `,` → `.`
  antes de parsear (por si el teclado decimal del usuario también escribe coma).

**29. Limpieza a fondo de datos huérfanos en Firestore de producción, más datos de
prueba nuevos.** Pedido explícito de Santiago Guerrero Parrado ("si algo se borra o deja
de existir debe dejar de existir por completo en la base de datos") al ver que una
matrícula de prueba apuntaba a un UID de una cuenta demo que ya no existe (las cuentas
demo se recrearon con UIDs nuevos el 2026-09-13, punto 20 — eso dejó huérfanas varias
referencias viejas que nunca se habían revisado a fondo). Encontrado y corregido, todo
vía la consola de Firebase (sin generar clave de cuenta de servicio — ver más abajo):
  - `courses/demo-course-moviles.professorId` apuntaba a un UID de profesor que ya no
    existe — corregido al UID real de `profesor.demo`.
  - `enrollments/demo-enrollment-1.studentId` apuntaba a un UID de estudiante que ya no
    existe — el documento se **borró por completo** (no se editó in place) y se creó uno
    nuevo con el UID real de `estudiante.demo`.
  - Los 3 documentos de `attendance` (mismo curso) y el único documento de
    `teacherEvaluations` tenían el mismo `studentId` huérfano, además de datos de
    prueba de mala calidad (`date: "snfnnf"`, `comment: "."`) — se borraron los 4
    documentos por completo en vez de corregirlos.
  - Se creó un segundo curso de prueba, **`demo-course-algebra`** ("Álgebra Lineal",
    `MA101`, 3 créditos, profesor real), su matrícula para `estudiante.demo`, y 3 notas
    de corte reales: Álgebra Corte 1 = 3.8; Móviles Corte 1 = 4.2, Corte 2 = 4.5 (Corte 3
    de ambas materias se dejó sin publicar a propósito, para poder probar el simulador
    con datos parcialmente completos).
  - **Regla nueva para el futuro (dejar constancia explícita, pedido de Santiago
    Guerrero Parrado):** toda función de borrado que ya exista o que se agregue en el
    futuro en este proyecto debe borrar POR COMPLETO el dato de la base de datos —
    nunca dejar una referencia huérfana a medio borrar. Antes de dar por terminada
    cualquier función nueva de "eliminar algo", revisar explícitamente qué otras
    colecciones podrían referenciar ese id (studentId/courseId/professorId/etc.) y
    limpiarlas también, o documentar por qué no hace falta. **Auditoría de hoy**: las
    únicas dos funciones de borrado que existen hoy en el código
    (`NewsRepository.delete()` para anuncios, y `AuthRepository.deleteCurrentAccount()`
    para el rollback de registro fallido) ya son completas — ninguna otra colección
    referencia un `news/{id}` por id, y `deleteCurrentAccount()` se llama antes de que
    exista ningún dato dependiente del uid nuevo. No hace falta tocar código hoy; esta
    regla queda para la próxima vez que se agregue una función de borrado real (por
    ejemplo, si en el futuro se agrega borrar un curso o desactivar/eliminar un usuario
    de verdad desde `ManageUsersScreen`, que hoy solo desactiva/cambia rol).
  - **Nota de higiene de credenciales**: para esta limpieza NO se generó ninguna clave
    de cuenta de servicio nueva — el clasificador de seguridad del entorno bloqueó
    automáticamente el intento de revisar/depurar las claves de servicio existentes en
    IAM (ver sección 6, "Seguimiento de seguridad", pendiente sin resolver: las 4 claves
    viejas de sesiones anteriores siguen sin revocar). Todo lo de este punto se hizo con
    ediciones normales de datos vía la consola de Firestore, no credenciales.

**Verificación**: `./gradlew compileDebugKotlin`/`assembleDebug` exitosos en cada
iteración. Probado de punta a punta en el **celular físico real** (Oppo/OnePlus
CPH2579, sin emulador): login real como `estudiante.demo`, calculadora de promedio
mostrando las 2 materias reales con sus cortes publicados, simulación en vivo (escribir
en una casilla vacía actualiza "Nota final"/"Promedio semestre" al instante), botón de
reset descartando la simulación y devolviendo los valores reales; login real como
`profesor.demo`, publicación de una nota de Corte 3 para Álgebra Lineal desde
`GradeEntryScreen` con el resultado "Nota guardada ✓".

**Commit y push**: cambios commiteados y subidos a
`https://github.com/eafitdesarrollo/AppEafit.git` (rama `main`).

### 2026-09-16 — Santiago Guerrero Parrado

**30. Bug real encontrado probando el rol Estudiante en el emulador de Android (celular
físico descargado ese día): la pantalla de Notificaciones siempre mostraba
`PERMISSION_DENIED: Missing or insufficient permissions.` en vez de la lista de avisos,
para cualquier usuario.**

- **Causa raíz #1 (regla de Firestore mal ubicada para collectionGroup queries)**:
  `NotificationRepository.listFor()` usa
  `firestore.collectionGroup("reads").whereEqualTo("uid", userId)` para saber qué
  notificaciones ya leyó el usuario actual. La regla de `reads` estaba declarada
  **anidada** dentro de `/notifications/{notificationId}` (`match /reads/{uid} { ... }`).
  Un `match` anidado normal en `firestore.rules` **no aplica a consultas
  `collectionGroup()`** — solo aplica a lecturas/escrituras en la ruta exacta
  `notifications/{id}/reads/{uid}`. Para que Firestore autorice una `collectionGroup`
  query hace falta declarar la regla con el wildcard recursivo `{path=**}` (
  `match /{path=**}/reads/{uid} { ... }`), a nivel superior (no anidada). Se probaron
  tres condiciones distintas para la regla anidada antes de encontrar esto —
  `myUid() == uid` (el segmento de ruta), un OR agregando
  `resource.data.uid == myUid()` (el patrón que documenta Firebase para este tipo de
  problema), e incluso `if isSignedIn()` a secas (sin ninguna condición sobre datos) —
  las tres devolvían el mismo `PERMISSION_DENIED` sin importar cuánto se esperara
  después de publicar (se probó hasta 20+ minutos, y también se confirmó con
  peticiones directas a la API REST de Firestore fuera de la app, para descartar caché
  del cliente Android o demora de propagación). El diagnóstico correcto se confirmó
  moviendo la regla fuera de `/notifications` a un `match /{path=**}/reads/{uid}` de
  nivel superior en `firestore.rules` — con eso el mismo `PERMISSION_DENIED`
  desapareció de inmediato.
- **Decisión de seguridad tomada con el usuario (Santiago Guerrero Parrado) antes de
  aplicarla**: la condición final de `allow read` en `/{path=**}/reads/{uid}` quedó en
  `if isSignedIn()` (cualquier usuario autenticado puede leer estos documentos), en vez
  de restringirla al dueño del marcador. Motivo: cada documento de `reads` solo
  contiene `uid` (de quien leyó) + `readAt` (fecha) — nada del contenido de la
  notificación ni datos personales — y restringir esto correctamente por dueño para una
  `collectionGroup` query exigiría desnormalizar el dato (por ejemplo duplicar el
  `targetUserId` de la notificación dentro de cada doc de `reads`) o mover la lógica a
  una Cloud Function, ninguna de las cuales se justificaba para resolver un bug
  bloqueante de esta severidad en el momento. Se preguntó explícitamente antes de
  desplegar esta relajación de la regla (el clasificador de seguridad del entorno la
  marcó como "Security Weaken" y pidió confirmación) y el usuario aprobó esta opción.
  El `allow create` sigue exigiendo que `myUid() == uid` y que
  `request.resource.data.uid == uid`, así que un usuario solo puede **crear** su propio
  marcador de leído, aunque pueda leer los de otros.
- **Causa raíz #2 (índice de collection group faltante)**: una vez arreglada la regla,
  la misma consulta pasó a fallar con
  `FAILED_PRECONDITION: The query requires a COLLECTION_GROUP_ASC index for collection
  reads and field uid`. Firestore no crea automáticamente índices de un solo campo con
  alcance "grupo de colecciones" (solo alcance "colección" por defecto). Se resolvió
  agregando una **excepción de índice automático** desde la consola de Firebase
  (Firestore → Índices → pestaña "Automáticos" → "Agregar exención": colección `reads`,
  campo `uid`, alcance "Grupo de colecciones", orden Ascendente habilitado). El índice
  tardó ~1-2 minutos en construirse; una vez listo, la consulta funcionó sin errores.
- **Archivos tocados**: `firestore.rules` (movida y reescrita la regla de `reads`, con
  comentario explicando por qué está fuera de `/notifications` y qué se probó antes de
  llegar a esta solución).
- **⚠️ Regla de Firestore desplegada — recordatorio obligatorio de la sección 0**: este
  cambio se publicó manualmente vía la consola web de Firebase (Firestore → Reglas →
  Publicar), **no** con `firebase deploy --only firestore:rules`, porque no hay una
  cuenta autorizada en el CLI de este entorno (ver limitación ya documentada en el
  punto 29/sección 6 sobre credenciales). El contenido de `firestore.rules` en este
  repo **ya coincide** con lo publicado en producción (revisado línea por línea después
  de desplegar), pero si alguien corre `firebase deploy` más adelante sin revisar esto
  primero, va a re-publicar exactamente lo mismo — no hay drift. El índice de
  `reads`/`uid` (grupo de colección) creado en la consola **no** está reflejado en
  `firestore.indexes.json` de este repo (sigue pendiente el problema de IAM para
  desplegar índices por CLI, ver sección 6) — si se recrea el proyecto de Firestore
  desde cero, hay que volver a crear esta excepción de índice manualmente o agregarla a
  `firestore.indexes.json` primero.
- **Verificado**: emulador Android (`Pixel_...`, no celular físico — celular
  desconectado/descargado ese día), login real como `estudiante.demo`, pantalla de
  Notificaciones pasó de `PERMISSION_DENIED` a "You're all caught up, no new
  notifications" tras el fix, confirmado con reinicio en frío de la app (force-stop +
  relanzar) para descartar cualquier caché.
- **Recordatorio de la regla permanente de esta bitácora** (ya vigente desde el punto
  29, se repite aquí porque este punto vuelve a tocar una regla de `allow`): cualquier
  función de borrado nueva en este proyecto debe seguir borrando el dato por completo
  de la base de datos, nunca dejar una referencia huérfana a medio borrar.

**Pendiente para la próxima sesión**: seguir probando los otros 3 roles
(Profesor, Administrativo, Admin) uno por uno en el mismo emulador, incluyendo enviar
una notificación de broadcast como `admin.demo` y confirmar que los demás roles ahora
sí pueden leerla y marcarla como leída sin el error de permisos.

**31. Dos hallazgos más, siguiendo con la prueba de roles en el emulador: (a) login
nuevo (nunca antes hecho en este emulador) fallaba con "credential incorrecto" para
CUALQUIER cuenta que no tuviera ya sesión guardada; (b) la base de datos local (Room)
nunca se borraba al cerrar sesión.**

- **(a) Login nuevo rechazado con "The supplied auth credential is incorrect,
  malformed or has expired."** Pasaba solo con `profesor.demo` (el celular físico ya
  tenía sesión guardada de `estudiante.demo` de antes, así que nunca se probó un login
  fresco en este emulador hasta ahora). Verificado con la API REST de Identity Toolkit
  que la contraseña SÍ era correcta, descartando error de tipeo. El logcat mostraba
  `FirebaseAuth: Logging in as profesor.demo@eafit.edu.co with empty reCAPTCHA token`
  seguido del rechazo — es decir, el SDK de Firebase Auth intenta un reto de
  reCAPTCHA/Play Integrity antes de autenticar, y si ese reto no se puede completar
  (token vacío), el backend rechaza el login pero lo reporta engañosamente como
  credencial incorrecta. Causa encontrada: en Firebase Console → Configuración del
  proyecto → App Android Debug (`co.edu.eafit.appeafit.debug`), el campo "Huellas
  digitales del certificado SHA" estaba **completamente vacío** — nunca se había
  registrado la huella del `debug.keystore` local, así que Play Integrity no podía
  verificar la app y el reto de reCAPTCHA fallaba silenciosamente. Se generaron las
  huellas SHA-1 y SHA-256 del keystore de debug (
  `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey
  -storepass android -keypass android`, con `-J-Duser.language=en` para esquivar un
  bug de formato regional en este keytool) y se agregaron ambas en la consola de
  Firebase para esa app. Tras reiniciar el proceso de la app (force-stop + relanzar,
  necesario para que se refresque la sesión de atestación), el login de `profesor.demo`
  funcionó normalmente. **No se tocó ninguna configuración de seguridad de reCAPTCHA
  en la consola** — el problema era solo la huella SHA faltante, no una política que
  hubiera que relajar.
  - **Pendiente**: la app de producción (`co.edu.eafit.appeafit`, no `.debug`) también
    tiene las huellas SHA vacías en la consola — si alguna vez se genera un APK/AAB de
    release firmado con un keystore real, hay que repetir este mismo paso con la
    huella de ESE keystore antes de publicarlo, o los logins nuevos fallarán igual en
    producción.
- **(b) La base de datos local (Room, `eafit_offline.db`) no se borraba al cerrar
  sesión.** Pedido explícito de Santiago Guerrero Parrado al notar que cambiar de
  cuenta en el mismo dispositivo dejaba datos acumulados. Verificado en el código: las
  consultas de Room YA estaban acotadas por uid/studentId/professorId (no había fuga
  de datos de un usuario a otro), pero nada llamaba a limpiar las tablas, así que los
  datos de cada cuenta que alguna vez inició sesión en el dispositivo se quedaban ahí
  para siempre, creciendo sin límite — exactamente el tipo de residuo que la regla
  permanente de esta bitácora (punto 29) prohíbe. Arreglado:
  - `core/di/AppContainer.kt`: nuevo método `suspend fun clearLocalCache()` que llama
    a `database.clearAllTables()` (API nativa de Room que borra todas las filas de
    todas las tablas en una transacción) en `Dispatchers.IO`.
  - `ui/navigation/SessionViewModel.kt`: `signOut()` ahora lanza una corrutina que
    primero llama a `container.clearLocalCache()` y luego a
    `container.authRepository.signOut()` (antes solo hacía lo segundo).
  - **Verificado end-to-end en el emulador**: login como `estudiante.demo`, se abrió
    "My grades" para forzar la sincronización (`cached_user`=1, `cached_grade`=4,
    `cached_course`=2, `cached_enrollment`=2 filas, confirmado con
    `sqlite3 databases/eafit_offline.db` vía `adb shell run-as`), cerrar sesión desde
    Perfil → Sign out → Accept, y las 7 tablas de caché (`cached_user`, `cached_grade`,
    `cached_course`, `cached_enrollment`, `cached_news`, `cached_calendar_event`,
    `sync_state`) quedaron en 0 filas inmediatamente después.
- **Archivos tocados**: `core/di/AppContainer.kt`, `ui/navigation/SessionViewModel.kt`.
  Ningún cambio en `firestore.rules` ni en la consola de Firebase para el punto (b).
- **Verificado además**: tras el fix de SHA y el borrado de caché, se probó el flujo
  completo de nuevo: login `estudiante.demo` → notificaciones OK → sign out → caché en
  0 filas → login `profesor.demo` (login fresco, antes bloqueado) → Home de Profesor
  cargó bien → notificaciones de Profesor también sin error de permisos ("You're all
  caught up, no new notifications").
- **`./gradlew compileDebugKotlin` y `assembleDebug` exitosos**; APK reinstalado en el
  emulador con `adb install -r` para probar este cambio.

**Pendiente para la próxima sesión**: falta probar Administrativo y Admin (ahora que el
login fresco funciona para cualquier cuenta), incluyendo el envío de una notificación
de broadcast como `admin.demo`.

**32. Se completó la prueba de los 4 roles en el emulador, incluyendo el flujo de
notificaciones de punta a punta.** Login fresco confirmado para `administrativo.demo`
y `admin.demo` (además de `estudiante.demo` y `profesor.demo` ya probados en el punto
31) — el fix de la huella SHA aplica a nivel de la app completa, no por cuenta.

- **Administrativo (`Staff`)**: Home con "Institutional directory"/"Manage
  announcements", notificaciones sin error ("You're all caught up").
- **Admin**: Home con "Manage users"/"Manage content"/"Send notification". Se probó el
  envío real de un broadcast a "Everyone" (título "Prueba", mensaje "Verificando") →
  el botón cambió a "Sent ✓". Se volvió a abrir Notificaciones del propio Admin y el
  broadcast apareció de inmediato con el punto naranja de "no leído"; al tocarlo pasó a
  texto normal (sin negrita, sin punto) confirmando que el `POST` a `notifications` y
  el `write` a `notifications/{id}/reads/{adminUid}` funcionan correctamente con las
  reglas actuales — es decir, el fix del punto 30 (`match /{path=**}/reads/{uid}`)
  cubre tanto la lectura por `collectionGroup` como la escritura del marcador de
  leído.
- Con esto, los 4 roles (Estudiante, Profesor, Administrativo, Admin) quedan
  verificados end-to-end en el emulador: login, Home con sus servicios propios, y
  Notificaciones (lectura + marcar como leído) sin `PERMISSION_DENIED`.
- **No queda pendiente ninguna prueba de rol para la próxima sesión** de las que se
  venían arrastrando; los pendientes reales son los ya anotados en la sección 6
  (índices de Firestore por CLI, Storage rules bloqueadas por facturación, límite de
  renovaciones de préstamos, etc.) más el pendiente nuevo del punto 31 sobre la app de
  producción sin huella SHA registrada.

### 2026-09-17 — Santiago Guerrero Parrado

**33. Reemplazo TEMPORAL de Firebase Storage por ImageKit.io mientras el cliente paga
el plan Blaze, para que la app se vea completa con imágenes reales (fotos de perfil,
imágenes de anuncios) en vez de recuadros vacíos.**

- **Por qué**: Storage de Firebase sigue bloqueado (ver sección 6, "Reglas de
  Storage") porque Firebase ya no tiene plan gratuito para Storage y el cliente
  todavía no ha activado facturación. Mientras tanto, para que se le pueda mostrar al
  cliente una app con contenido visual completo (no cajas grises vacías donde
  debería haber una foto), se conectó una cuenta gratuita de **ImageKit.io** (20GB de
  almacenamiento + 20GB de transferencia al mes, se renueva cada mes) como reemplazo
  de solo lectura/escritura de imágenes.
- **Cuenta creada**: `eafitdesarrollo@gmail.com` en ImageKit.io (el usuario/cliente la
  creó directamente, yo solo configuré las claves con Claude for Chrome una vez
  registrada). ImageKit ID de la cuenta: `eafit`. URL-endpoint:
  `https://ik.imagekit.io/eafit`.
- **Archivo nuevo**: `core/imagekit/ImageKitClient.kt` — sube (`upload()`) y borra
  (`delete()`) archivos contra la API REST de ImageKit usando OkHttp (dependencia
  nueva, ver `libs.versions.toml`/`app/build.gradle.kts`).
  - **Decisión de seguridad, ya documentada como riesgo aceptado**: ImageKit exige
    firmar cada subida (HMAC-SHA1 de `token+expire` con la clave privada de la
    cuenta). Normalmente esa firma se genera en un backend para no exponer la clave
    privada, pero este proyecto no tiene backend propio (solo Firebase
    Auth/Firestore, sin Cloud Functions). La clave privada quedó **embebida en el
    código del cliente** (`ImageKitClient.PRIVATE_KEY`) para poder firmar ahí mismo
    — mismo nivel de riesgo que ya se acepta en este proyecto para el QR del carnet
    falsificable (sección 6). Aceptable para esta etapa de demo con 4 cuentas de
    prueba; **si este proveedor se usara en producción real, la firma debe moverse a
    un backend**, no seguir embebida en el APK.
  - La clave pública (`public_...`) sí es segura de exponer (equivalente a un API key
    de Firebase); solo la privada (`private_...`) es el riesgo aceptado.
- **Fotos de perfil** (`ui/profile/ProfileViewModel.kt`): reemplazado el upload a
  Firebase Storage por `imageKitClient.upload(folder="appeafit/profile_photos",
  fileName="$uid.jpg", useUniqueFileName=false)`. Con nombre fijo por usuario y
  `useUniqueFileName=false`, ImageKit **sobreescribe** la foto anterior del mismo
  usuario en el mismo lugar en vez de crear un archivo nuevo — igual que el
  comportamiento anterior con Firebase Storage, así que nunca queda una foto vieja
  huérfana cuando alguien cambia su foto.
- **Imágenes de anuncios** (`ui/staff/ManageAnnouncementsScreen.kt`): se agregó un
  selector de imagen (antes no existía NINGÚN campo de imagen en el diálogo de
  "nuevo anuncio", por eso todos los anuncios se veían sin foto). Cada imagen nueva
  se sube a `appeafit/news` con `useUniqueFileName=true` (nombre único, no hay slot
  fijo como en perfil) y se guarda tanto la URL como el `fileId` que devuelve
  ImageKit.
  - **Modelo de datos**: `NewsItem`/`CachedNewsEntity` ganaron el campo
    `imageFileId` (además del `imageUrl` que ya existía) — Room subió de v2 a v3
    (`fallbackToDestructiveMigration`, mismo criterio ya documentado en el punto 27).
    `FirestoreMappers.kt` actualizado para leer/escribir ese campo.
  - **Cumpliendo la regla permanente de esta bitácora ("lo que se borra se borra por
    completo")**: `NewsRepository.delete()` ahora recibe el `NewsItem` completo (no
    solo el id) y, si tiene `imageFileId`, primero llama a
    `imageKitClient.delete(fileId)` para borrar la imagen de ImageKit, y solo
    después borra el documento de Firestore. **Verificado en vivo**: se publicó un
    anuncio de prueba con imagen desde la app (rol Administrativo/Staff), se
    confirmó en Firestore que quedó con `imageUrl`/`imageFileId` reales de ImageKit,
    se borró desde la app, y se confirmó con una petición directa a la API de
    ImageKit (`GET /v1/files/{fileId}/details`) que el archivo devuelve
    `404 The requested file does not exist` — es decir, se borra de verdad, no solo
    de la lista de anuncios.
- **Imágenes de prueba/demo agregadas** (para que la app no se vea vacía al
  mostrársela al cliente): se diseñaron y subieron 2 imágenes de portada (estilo
  navy/azul EAFIT, coherente con el rediseño visual de esta bitácora) para los 2
  anuncios institucionales que ya existían de antes y estaban sin imagen:
  - `news/RA7pSlUStir7Kkt6tXTD` ("Actualización de horarios de biblioteca") →
    `https://ik.imagekit.io/eafit/appeafit/news/news_biblioteca_horarios.jpg`
    (`fileId: 6aac15d2ead997d09ab0163c`).
  - `news/RhWzeSJ8Sgsl1poVkBJ0` ("Bienvenida al semestre 2026-2") →
    `https://ik.imagekit.io/eafit/appeafit/news/news_bienvenida_semestre.jpg`
    (`fileId: 6aac1902ead997d09ad6a2f8`).
  - Estas dos actualizaciones se hicieron directamente contra la API REST de
    Firestore (con un token de `admin.demo`, que sí tiene permiso de `update` sobre
    `news` según las reglas vigentes), no cambian ninguna regla de seguridad ni
    tocan datos de usuarios reales.
- **Verificado end-to-end en el emulador** (los 3 flujos, tras reinstalar el APK
  actualizado): (1) Home/EAFIT News muestra las 2 imágenes reales en vez de cajas
  grises; (2) como Administrativo: crear anuncio nuevo con imagen desde la galería
  → aparece con su miniatura en "Manage announcements" → se borra → imagen
  confirmada eliminada de ImageKit (404); (3) como Administrativo: editar foto de
  perfil → sube a ImageKit → se ve la foto nueva como avatar circular en el
  perfil.
- **`./gradlew compileDebugKotlin`/`assembleDebug` exitosos**; APK reinstalado con
  `adb install -r`.
- **⚠️ Nota de seguridad sobre el push de este commit**: el clasificador de
  seguridad del entorno bloqueó el primer intento de `git push` de este commit
  porque el código (`ImageKitClient.kt`) contiene la clave privada de ImageKit en
  texto plano (ver más arriba, "Decisión de seguridad"). Se le explicó esto a
  Santiago Guerrero Parrado antes de forzar el push; su decisión explícita fue
  **subirlo tal cual** ("eso se va a quitar cuando el cliente pague storage de
  firebase"), con la condición de dejar aquí, en constancia, exactamente qué
  secreto quedó expuesto en el historial de git y qué hay que hacer cuando llegue
  ese momento. Por eso este punto se redacta como checklist explícito en vez de
  prosa general:
  - **Qué quedó expuesto en GitHub** (repo privado `eafitdesarrollo/AppEafit`,
    rama `main`, este commit en adelante): la clave privada completa de la cuenta
    de ImageKit (`eafitdesarrollo@gmail.com`, ImageKit ID `eafit`), en
    `app/src/main/java/co/edu/eafit/appeafit/core/imagekit/ImageKitClient.kt`,
    constante `PRIVATE_KEY`. Con esa clave se puede
    firmar subidas y también **borrar cualquier archivo** de esa cuenta de
    ImageKit vía su API — no da acceso a Firebase ni a ningún otro sistema de este
    proyecto, solo a esa cuenta de ImageKit.
  - **Qué hacer cuando Santiago Guerrero Parrado dé la instrucción de que el
    cliente ya pagó Storage de Firebase** (checklist, en este orden):
    1. Desplegar `storage.rules` (ya escrito en el repo, solo pendiente de plan
       pagado — ver sección 6, "Reglas de Storage").
    2. En `ui/profile/ProfileViewModel.kt` y
       `ui/staff/ManageAnnouncementsScreen.kt`/`data/repository/NewsRepository.kt`,
       volver a usar `container.storage` (Firebase Storage — se dejó declarado y
       sin usar en `AppContainer.kt` a propósito para este momento) en vez de
       `container.imageKitClient`.
    3. Decidir qué hacer con las imágenes ya subidas a ImageKit (las 2 de los
       anuncios institucionales, más cualquier foto de perfil/anuncio que se haya
       subido mientras tanto): no es obligatorio migrarlas, ImageKit puede seguir
       sirviéndolas gratis indefinidamente mientras la cuenta exista; solo hay que
       migrarlas si se quiere depender exclusivamente de Firebase.
    4. **Borrar `ImageKitClient.kt` del código** (o al menos vaciar las constantes
       `PUBLIC_KEY`/`PRIVATE_KEY`) y quitar la dependencia de OkHttp si ya no se
       usa para nada más.
    5. Entrar a `imagekit.io/dashboard/developer/api-keys` (cuenta
       `eafitdesarrollo@gmail.com`) y **regenerar/revocar la clave privada** que
       quedó en el historial de git — aunque se borre del código nuevo, sigue
       existiendo en commits viejos del repo, así que no basta con borrarla del
       archivo; hay que invalidarla desde la consola de ImageKit para que esa
       clave expuesta deje de servir para algo.
    6. Si ya no se va a usar ImageKit para nada más, opcionalmente eliminar la
       cuenta completa desde ImageKit.
- **Recordatorio de la regla permanente de esta bitácora** (pedido explícito de
  Santiago Guerrero Parrado, vigente desde el punto 29): cualquier función de
  borrado, existente o futura, debe borrar el dato por completo de donde esté
  guardado — incluyendo el proveedor de imágenes que esté activo en ese momento
  (ImageKit ahora, Firebase Storage cuando se revierta). Este punto (33) es un
  ejemplo concreto de aplicar esa regla a un proveedor nuevo.

**Pendiente para la próxima sesión**: monitorear el uso de la cuota gratuita de
ImageKit (20GB/mes) desde `imagekit.io/dashboard/usage-analytics` a medida que se
agreguen más imágenes de prueba; revertir a Firebase Storage según los pasos del
punto anterior en cuanto el cliente pague el plan Blaze.

**34. Corregidas dos fallas visuales relacionadas con imágenes, reportadas por
Santiago Guerrero Parrado tras ver las tarjetas de EAFIT News en el Home: (a) las
imágenes se recortaban mal y no se adaptaban a la tarjeta; (b) no había ninguna guía
de qué tamaño de imagen conviene subir en ningún lugar donde se sube una foto.**

- **(a) Recorte de imágenes en las tarjetas de "EAFIT News"** (`ui/components/
  NewsCard.kt`). Causa raíz: la tarjeta completa (220dp × 260dp, proporción vertical
  ~0.85) usaba la imagen como fondo a pantalla completa con
  `ContentScale.Crop` y el texto superpuesto encima con un degradado. Las imágenes
  de anuncios son horizontales (las que se subieron, 800×450px, proporción 16:9)
  — al forzar una imagen horizontal dentro de un recuadro vertical con Crop, se
  recortaba casi todo el ancho de la imagen, dejando solo una franja angosta del
  centro. Arreglado rediseñando la tarjeta: ahora el recuadro de imagen tiene su
  propio `aspectRatio(16f/9f)` (la misma proporción recomendada al subir, ver
  siguiente punto) en la parte de arriba de la tarjeta, y el título va **debajo** de
  la imagen en texto normal (ya no superpuesto ni con degradado) — así la imagen se
  ve completa y el texto siempre es legible sin depender de qué tan oscura sea la
  foto de fondo.
- **(b) Sin ninguna guía de tamaño recomendado al subir fotos, en ningún lado.**
  Pedido explícito: que la app le diga al usuario, al momento de subir cualquier
  foto (perfil, anuncio, o cualquier otro lugar futuro), qué tamaño/proporción
  conviene — pero **de forma permisiva, no obligatoria**: es una guía para que la
  imagen se adapte bien a donde se va a mostrar, no una validación que bloquee la
  subida si no coincide exactamente.
  - Nuevo componente reutilizable `ui/components/ImageSizeHint.kt`: un texto con
    ícono de información, pensado para usarse junto a cualquier selector de imagen
    futuro en la app (no solo los dos que existen hoy).
  - `ui/staff/ManageAnnouncementsScreen.kt`: el recuadro de selección de imagen del
    diálogo "nuevo anuncio" ahora tiene el mismo `aspectRatio(16:9)` de la tarjeta
    final (para que el usuario vea de entrada cómo va a quedar recortada), con el
    hint de texto debajo: "Recomendado: foto horizontal, aprox. 800×450 px
    (proporción 16:9)...".
  - `ui/profile/EditProfileScreen.kt`: hint debajo del avatar: "Recomendado: foto
    cuadrada, aprox. 500×500 px, con la cara centrada...".
  - Estrings nuevos `image_hint_news`/`image_hint_profile` en
    `values/strings.xml` y `values-en/strings.xml` (138 entradas en ambos,
    mantiene la paridad).
  - **No se agregó ninguna validación que bloquee el guardado** si la imagen no
    coincide con la medida recomendada — se subió tal cual lo pidió Santiago
    Guerrero Parrado ("que sea un poco permisiva... y que si no es esa no se puede,
    eso no").
- **De paso, se encontró y corrigió el mismo tipo de recorte mal hecho en las 4
  fotos de perfil circulares de la app** (`ui/profile/ProfileScreen.kt`,
  `ui/profile/EditProfileScreen.kt`, `ui/carnet/CarnetScreen.kt`,
  `ui/student/GpaCalculatorScreen.kt`): todas usaban `Modifier.background(brush,
  CircleShape)` para el fondo pero **sin** `.clip(CircleShape)` ni
  `contentScale = ContentScale.Crop` en el `AsyncImage` — es decir, el fondo se
  pintaba como círculo pero la foto en sí no se recortaba a esa forma, así que
  cualquier foto que no fuera perfectamente cuadrada se veía deformada/mal
  encajada dentro del círculo. Se agregó `.clip(CircleShape)` +
  `contentScale = ContentScale.Crop` a las 4. Esto es exactamente el ejemplo que
  dio Santiago Guerrero Parrado ("como la foto de perfil") de dónde se necesitaba
  esta corrección.
- **Verificado en el emulador** tras reinstalar el APK: las tarjetas de EAFIT News
  ahora muestran la imagen completa en 16:9 con el título debajo, sin recortes
  raros; el avatar de perfil (rol Administrativo, la foto de prueba subida en el
  punto 33) se ve como un círculo limpio y bien recortado; el diálogo de nuevo
  anuncio muestra el recuadro 16:9 y el texto de guía correctamente.
- **`./gradlew compileDebugKotlin`/`assembleDebug` exitosos**; APK reinstalado con
  `adb install -r`.

### 2026-09-17 — Santiago Guerrero Parrado

**35. Se inicia el proyecto del sitio web de EAFIT (`appeafit-web-frontend` +
`appeafit-web-backend`, contratado directamente por EAFIT), que comparte este mismo
proyecto de Firebase (`appeafit-297d5`). Este punto documenta los cambios que ese
proyecto nuevo le hizo a `firestore.rules`, que vive en ESTE repo.**

- **Dos repos nuevos en GitHub** (privados, cuenta `eafitdesarrollo`):
  `appeafit-web-frontend` (React + Vite + TypeScript + React Router + Tailwind +
  react-i18next) y `appeafit-web-backend` (Node + Express + TypeScript), clonados en
  `EAFIT/web/` (junto a `EAFIT/repo/`, que es este repo de la app móvil). Cada uno
  tiene su propia BITACORA.md con las mismas reglas de esta sección 0.
- **`firestore.rules` modificado** para que el sitio web público pueda leer datos sin
  que el visitante inicie sesión (a diferencia de la app móvil, donde todo exige
  login):
  - `news`: `allow read` cambiado de `isSignedIn()` a `true` -- las mismas noticias
    que ya ve la app móvil ahora también se muestran en la sección "Noticias" del
    sitio web, sin duplicar datos.
  - **Colección nueva `hero_slides`**: imágenes/videos en loop del hero de la portada
    del sitio web. Lectura pública (`allow read: if true`), escritura solo
    Staff/Admin. Se sembraron 2 documentos de ejemplo reutilizando las 2 imágenes de
    ImageKit ya subidas para los anuncios de la app móvil (punto 33) -- mismo
    principio: la imagen la administra Admin/Staff, hoy vía ImageKit (temporal),
    después vía Firebase Storage cuando el cliente pague el plan Blaze (pedido
    explícito de Santiago Guerrero Parrado, aplica igual a este hero que a las
    fotos de perfil/anuncios ya documentadas).
  - **Colección nueva `contact_messages`**: mensajes del formulario de contacto del
    sitio web (nombre, correo, teléfono, mensaje -- datos personales de gente que no
    necesariamente tiene cuenta). `allow create: if true` como respaldo (el backend
    los crea normalmente con Admin SDK, que bypasa las reglas), lectura solo
    Staff/Admin porque contiene datos personales.
- **⚠️ Regla de Firestore desplegada — recordatorio obligatorio de la sección 0**:
  los 3 cambios de arriba se publicaron manualmente vía la consola de Firebase
  (mismo método ya usado en los puntos 30/31 de esta bitácora, sin CLI). Verificado
  que `firestore.rules` de este repo coincide línea por línea con lo publicado.
- **Pendiente explícito para la app móvil** (anotado aquí para que quede en
  constancia, aunque no se construyó en esta sesión): falta una pantalla de Admin en
  esta app para gestionar `hero_slides` (crear/editar/borrar slides del sitio web)
  con el mismo patrón que `ManageAnnouncementsScreen` -- hoy esos 2 documentos de
  ejemplo se crearon a mano vía la API de Firestore, no desde la app.
- **Recordatorio de la regla permanente de esta bitácora**: si en el futuro se agrega
  una función para borrar un `hero_slide` (desde la app móvil o donde sea), debe
  borrar también la imagen/video asociado del proveedor que esté activo en ese
  momento (ImageKit ahora, Storage después) -- mismo patrón que ya aplica
  `NewsRepository.delete()` para los anuncios (punto 33).

### 2026-09-17 — Santiago Guerrero Parrado

**36. Se construye el pendiente explícito del punto 35: pantalla de Admin/Staff para
gestionar `hero_slides` desde la app móvil (`ManageHeroSlidesScreen`).** Motivado por
el pedido "termina TODO" -- ya no hay que crear/editar/borrar los slides del hero del
sitio web a mano vía la consola de Firebase.

- **`domain/model/HeroSlide.kt`** (nuevo): `id, type ("image"|"video"), url,
  mediaFileId, title, subtitle, ctaLabel, ctaHref, order` -- mismo shape que
  `HeroSlide` en `web/appeafit-web-frontend/src/types.ts` (única fuente de verdad de
  qué campos lee el sitio), más `mediaFileId` que es de uso interno de esta app (para
  poder borrar el archivo de ImageKit; el sitio web no lo lee ni le importa).
- **`data/repository/FirestoreMappers.kt`**: se agregan `toHeroSlide()` / `toMap()`
  para `HeroSlide`, mismo patrón que `NewsItem`.
- **`data/repository/HeroSlideRepository.kt`** (nuevo): `fetchAll()` (ordenado por
  `order` asc, sin caché local en Room porque es una colección pequeña que solo se
  administra en línea desde esta pantalla), `publish()`, `update()` (para poder
  editar un slide existente, no solo crear/borrar), y `delete()` -- que borra primero
  el archivo de ImageKit (`mediaFileId`) y luego el documento de Firestore, cumpliendo
  la regla permanente de borrado completo.
- **`ui/staff/ManageHeroSlidesScreen.kt`** (nuevo): mismo patrón visual que
  `ManageAnnouncementsScreen` (`Scaffold` + FAB + `LazyColumn` de `EafitCard`s +
  `AlertDialog` de formulario), con dos diferencias explícitas que pedía el hero y que
  anuncios no tenía:
  1. **Soporta imagen O video** -- el selector usa
     `PickVisualMedia.ImageAndVideo` (antes solo existía selección de imagen en todo
     el proyecto) y detecta el tipo real por MIME (`contentResolver.getType`), no por
     extensión. Un video se sube igual que una imagen a través de
     `ImageKitClient.upload()` (ya era genérico, no hacía falta tocarlo) y se
     previsualiza con un ícono (no hay librería de miniaturas de video en el
     proyecto, y agregar una era más de lo que este pendiente pedía).
  2. **Permite editar un slide existente** (tocar la tarjeta o el ícono de lápiz),
     no solo crear/borrar -- si Admin/Staff elige un archivo nuevo al editar, el
     archivo viejo de ImageKit se borra DESPUÉS de que el nuevo suba con éxito (para
     no quedar sin ningún archivo si la subida fallara a mitad de camino).
- **Cableado**: `Routes.STAFF_MANAGE_HERO_SLIDES` / `Routes.ADMIN_MANAGE_HERO_SLIDES`
  (`StaffNavGraph.kt`, `AdminNavGraph.kt`), entrada nueva en `ServiceCatalog.kt` para
  los roles STAFF y ADMIN (grupo "Institucional"/"Administración"), y
  `AppContainer.heroSlideRepository`. Strings nuevas en español
  (`values/strings.xml`) e inglés (`values-en/strings.xml`):
  `service_manage_hero`, `manage_hero_new`, `manage_hero_edit`, `manage_hero_empty`,
  `manage_hero_subtitle`, `manage_hero_cta_label`, `manage_hero_cta_href`,
  `manage_hero_order_label`, `manage_hero_media_hint`.
- **No se tocó `firestore.rules`**: la colección `hero_slides` y su regla
  (`allow create, update, delete: if isStaffOrAdmin()`) ya existían desde el punto 35
  y ya cubren lo que esta pantalla necesita.

**Verificado**: `./gradlew compileDebugKotlin` exitoso (BUILD SUCCESSFUL, sin errores
de compilación en los archivos nuevos ni en los modificados).

**Pendiente para la próxima sesión**: probar la pantalla en un emulador/dispositivo
real (crear un slide de video de verdad y confirmar que se ve bien en el hero del
sitio web); considerar agregar una miniatura real de video en vez del ícono genérico
si se necesita mejor UX.

### 2026-09-18 — Santiago Guerrero Parrado

**37. Funciones completas de Registraduría y Biblioteca para Administrativo
(staff) + fusión de `JuanmaBranch` (rebranding EAFIT → IAFIC) a `main`.**
Motivado por el pedido explícito: *"el administrativo ahora mismo en la app
movil tiene muy pocas funciones y se ve basia la app en la version
administrativo... investiga e implementa por completo funciones para el
administrativo"*, y por separado: *"otro colaborador cambio cosas hizo
cambios grandes, lo hizo sobre otra rama, es JuanmaBranch... nos equivocamos
de universidad no es la EAFIT si no la IAFIC"*.

**Parte A — Funciones nuevas para Administrativo (staff):**

Antes, `ServiceCatalog.kt` solo le daba a staff 3 servicios (Directorio,
Gestionar anuncios, Gestionar hero del sitio web) frente a 6 del estudiante,
4 del profesor y 5 del admin. Se investigó qué funciones tiene normalmente
un administrativo de universidad (registraduría: cursos, matrículas,
calendario académico; biblioteca: préstamos; atención: buzón de PQRS) y se
confirmó que **`firestore.rules` ya permitía a staff/admin escribir en
`courses`, `enrollments`, `calendarEvents`, `loans` y `contact_messages`
desde el origen del proyecto** -- nunca existió la pantalla en la app para
usar ese permiso. Se agregan 5 pantallas nuevas, todas con CRUD completo,
probado de punta a punta contra Firestore real en un emulador (Pixel 8 API
36):

- **`ui/staff/ManageCoursesScreen.kt`** (nueva): crear/editar/borrar cursos
  (nombre, código, créditos, profesor -- selector real de usuarios con rol
  PROFESSOR, horario -- lista de franjas día/hora con selector de día).
  `CourseRepository`: se agregan `updateCourse()`, `deleteCourse()` (borra
  en cascada las matrículas del curso, si no quedaban matrículas huérfanas
  apuntando a un curso ya inexistente).
- **`ui/staff/ManageEnrollmentsScreen.kt`** (nueva): elegir un curso, ver sus
  estudiantes matriculados, matricular uno nuevo (buscador en vivo por
  nombre/correo que excluye a los ya matriculados) o desmatricular.
  `CourseRepository`: se agregan `listEnrollmentsForCourse()` (para tener el
  id de la matrícula, no solo el usuario) y `unenroll()`.
- **`ui/staff/ManageCalendarScreen.kt`** (nueva): crear/editar/borrar eventos
  del calendario académico institucional (antes el estudiante solo podía
  VERLO, nadie podía mantenerlo actualizado desde la app). Usa
  `DatePickerDialog` de Material3 (primer uso en el proyecto). Se agregan
  `CalendarRepository.createEvent()/updateEvent()/deleteEvent()` y
  `CalendarEvent.toMap()` en `FirestoreMappers.kt` (no existía).
- **`ui/staff/ManageLoansScreen.kt`** (nueva): registrar un préstamo nuevo a
  un estudiante (buscador en vivo + `DatePickerDialog` para la fecha de
  vencimiento), marcarlo devuelto, o borrarlo. Antes solo el estudiante
  podía ver/renovar SUS PROPIOS préstamos; no existía pantalla para que
  Biblioteca los cree o los cierre. Se agregan
  `LoanRepository.listAll()/create()/markReturned()/delete()` y
  `Loan.toMap()` en `FirestoreMappers.kt`.
- **`ui/staff/ContactMessagesScreen.kt`** (nueva) + **`domain/model/
  ContactMessage.kt`** + **`data/repository/ContactMessageRepository.kt`**
  (nuevos): buzón de los mensajes del formulario de contacto del sitio web
  (`appeafit-web-backend`), colección compartida `contact_messages` --
  el comentario en `firestore.rules` de esa colección literalmente decía
  *"para que Administrativos puedan revisarlos"* desde el 2026-09-17, pero
  nunca se construyó la pantalla. Permite marcar un mensaje como atendido
  (campo `resolved`, no existe en los documentos que crea el backend web,
  nace en `false` vía `getBoolean() ?: false`) o borrarlo.
- **Cableado**: 5 rutas nuevas en `Routes.kt`, 5 `composable()` en
  `StaffNavGraph.kt`, 5 `ServiceEntry` nuevas en `ServiceCatalog.kt`
  (grupos: "Académico" para cursos/matrículas/calendario, "Biblioteca" para
  préstamos, "Institucional" para el buzón de contacto), y
  `AppContainer.contactMessageRepository`. Strings nuevas en español e
  inglés para las 5 pantallas.
- **No se tocó `firestore.rules`**: las reglas de todas estas colecciones
  (`courses`, `enrollments`, `calendarEvents`, `loans`, `contact_messages`)
  ya estaban desplegadas y ya cubrían exactamente lo que estas 5 pantallas
  necesitan.

**Verificado en emulador real** (no solo compilación): se instaló el APK
debug, se inició sesión como `administrativo.demo@eafit.edu.co`, y se probó
cada pantalla de punta a punta contra Firestore real: crear/editar (incluido
asignar profesor y agregar un horario real)/borrar un curso; matricular y
desmatricular un estudiante real; crear y borrar un evento de calendario
real; crear un préstamo real, marcarlo devuelto, y borrarlo. Todos los datos
de prueba creados durante la verificación se borraron al terminar, dejando
el estado original de Firestore intacto.

**Parte B — Fusión de `JuanmaBranch` (rebranding EAFIT → IAFIC):**

`git fetch origin` reveló una rama nueva `JuanmaBranch` (2 commits: "Cambios
al nombre del proyecto" y "Cambios al color del theme y logo") no fusionada
a `main`. Se confirmó que es un rebranding real e intencional de la
Universidad EAFIT a **IAFIC** (Corporación Universitaria Regional del
Caribe, CURC-IAFIC, universidad real verificada en `https://www.iafic.edu.co/`,
"Vigilada Mineducación"), pedido explícito de Santiago Guerrero Parrado tras
descubrir que el proyecto se había construido para la universidad
equivocada. `git merge origin/JuanmaBranch` se fusionó sin conflictos reales
(auto-merge limpio en `strings.xml` porque cada rama tocaba renglones
distintos) y trajo: nueva paleta de colores en `ui/theme/Color.kt` y
`colors.xml` (azul marino `#002855`, gris oscuro `#111111`, verde `#009A44`
como acentos, en vez del azul/negro anterior -- son tokens de tema
GLOBALES, ya se aplican a los 4 roles sin trabajo adicional) y strings
`app_name`/`auth_welcome_title`/etc. cambiados a "IAFIC".

**Se completaron 3 cosas que `JuanmaBranch` dejó a medias** (cambios
cosméticos de strings, pero no de la lógica funcional real):

1. **`ui/auth/AuthViewModel.kt`**: `INSTITUTIONAL_DOMAIN` seguía en
   `"@eafit.edu.co"` en código Kotlin (Juanma solo cambió el STRING del
   mensaje de error a "@iafic.edu.co", no la validación real) -- con esto,
   ningún correo `@iafic.edu.co` real podía registrarse ni iniciar sesión.
   Corregido a `"@iafic.edu.co"`.
2. **`app/src/main/res/values-en/strings.xml`**: `auth_error_invalid_domain`
   en inglés seguía diciendo "@eafit.edu.co" (Juanma solo corrigió la
   versión en español). Corregido.
3. **`ui/carnet/CarnetScreen.kt`**: el carnet digital tenía "EAFIT" fijo en
   el contenido del QR (`"EAFIT-ID:..."`) y en el texto para compartir
   contacto (`"... · EAFIT"`), más el inicial de avatar por defecto ("E").
   Cambiados a "IAFIC-ID:...", "... · IAFIC", e inicial "I".
4. **`drawable/ic_launcher_background.xml`**: JuanmaBranch reemplazó por
   error el fondo del ícono adaptativo con el MISMO monograma "I" del
   foreground (copiado sin querer), dejando el ícono con fondo transparente
   en vez de un color sólido. Corregido a un relleno sólido real con el
   nuevo azul marino `#002855`.

**Ícono y logo reales de IAFIC** (pedido explícito: *"entres a chrome y
saques el logo de la IAFIC y lo pongas como logo e icono de la app"*): se
descargó en vivo `https://www.iafic.edu.co/logo.png` (logo oficial real,
"CURC-IAFIC", escudo con dos leones y corona) y se recortó/optimizó con
`sharp` (mismo proceso que ya se usa en `appeafit-web-frontend`) para
generar:
- `drawable/ic_launcher_foreground.png` (nuevo, reemplaza el monograma
  vector "I" de Juanma): el escudo real centrado en un lienzo transparente
  de 432×432, dentro de la zona segura del ícono adaptativo.
- `drawable/ic_splash_logo.png` (nuevo, reemplaza el vector): el escudo real
  para la pantalla de splash (`windowSplashScreenAnimatedIcon`).
- `drawable/ic_notification.png` (nuevo, reemplaza el vector): versión
  pequeña (96×96) del escudo para el ícono de notificaciones -- Android
  ignora el color RGB y usa solo el canal alfa para pintar el ícono de la
  barra de estado, así que un PNG a color funciona igual que un vector
  monocromático para este propósito.
- El ícono legacy cuadrado (escudo sobre tarjeta blanca, 512×512) se generó
  también pero no se integró a mipmaps porque `minSdk = 26` ya cubre el
  ícono adaptativo (`mipmap-anydpi-v26`) para el 100% de los dispositivos
  soportados -- no se necesitan buckets de densidad legacy.
- No se tocó `applicationId`/`namespace` (`co.edu.eafit.appeafit`): ese
  paquete está atado al `google-services.json` real del proyecto Firebase
  `appeafit-297d5`; cambiarlo rompería Auth/Firestore/Messaging a menos que
  se registre una app Android nueva en la consola de Firebase, que no se
  pidió y queda fuera del alcance de esta sesión.

**Verificado**: `./gradlew compileDebugKotlin` y `./gradlew assembleDebug`
exitosos (BUILD SUCCESSFUL) después de la fusión y de todos los cambios de
esta entrada. Por pedido explícito de Santiago Guerrero Parrado ("solo
compila, si todo compila has push sin comprobación visual porque tenemos
que pasar a exponer ya"), **no se hizo verificación visual del rebranding
en emulador** -- las 5 pantallas nuevas de Administrativo sí se verificaron
visualmente antes de este pedido (ver Parte A).

**Pendiente para la próxima sesión**: revisar si faltan más menciones de
"EAFIT" fuera de código/strings (ej. `README.md`, nombre de
paquete/Firebase si algún día se decide migrar); `google-services.json`
sigue apuntando al proyecto Firebase original (`appeafit-297d5`) --
confirmar con el equipo si ese proyecto de Firebase también debe
renombrarse/migrarse o se mantiene igual mientras cambia solo la marca
visible.

---

### 2026-09-18 — Santiago Guerrero Parrado

**Parte C: verificación visual del rebranding pendiente de la Parte B**
(pedido explícito: *"bueno ahora si prueba la nueva version en el
emmulador"*). Se instaló el APK en el emulador y se recorrió splash, Home,
ID Card, Perfil y Edit personal data con capturas reales.

**Hallazgo 1 -- corregido**: el fondo del ícono adaptativo
(`ic_launcher_background.xml`) había quedado en azul marino sólido
(`#002855`), el mismo tono que domina el escudo real de la IAFIC, así que
el escudo casi desaparecía sobre su propio fondo (reportado por Santiago:
*"el logo de la IAFIC le cambiaste los colores... por qué esta verde y
azul?"* -- se confirmó descargando de nuevo `iafic.edu.co/logo.png` en vivo
que el escudo real SÍ es azul marino + verde, sin ningún recoloreo de por
medio; el problema real era solo el fondo del ícono). Cambiado a fondo
blanco (`#FFFFFF`) para dar contraste. Verificado con zoom sobre captura
del launcher tras reinstalar: escudo nítido y legible.

**Hallazgo 2 -- no es bug de código, es dato de prueba**: en Carnet/Perfil,
el avatar de respaldo (círculo con inicial) de la cuenta demo
"Administrativo Demo" se veía en blanco. Se confirmó leyendo
`CarnetScreen.kt` y `Theme.kt` que la lógica y los colores son correctos
(`MaterialTheme.colorScheme.primary` es azul marino oscuro en ambos temas,
nunca blanco sobre blanco). La causa real: el campo `photoUrl` de esa
cuenta demo apunta a una imagen real subida por error durante pruebas
anteriores de esta misma sesión (una captura de pantalla del estado vacío
de notificaciones, "You're all caught up, no new notifications", quedó
seleccionada sin querer en el selector de fotos del emulador). Se
confirmó reproduciendo el mismo contenido exacto en tres pantallas
distintas (Carnet, Perfil, Edit personal data), lo que descarta un glitch
de renderizado y confirma que es el contenido real de la imagen. No se
logró reemplazar la foto de forma confiable por automatización (el picker
de fotos del emulador reordena su grilla cada vez que se toma una captura
nueva, lo que hace la selección por coordenadas poco confiable) -- queda
pendiente corregirla manualmente desde la app (Perfil → Editar datos
personales → tocar el círculo → elegir otra foto) o limpiar el campo
`photoUrl` directamente en Firestore para esa cuenta. No afecta cuentas
reales ni ninguna otra cuenta demo.

**Verificado**: splash con escudo real, Home con "IAFIC News" y colores
correctos, ID Card y Perfil con paleta y textos correctos (solo el avatar
de esa cuenta específica queda con la foto de prueba equivocada).
`./gradlew assembleDebug` exitoso tras el fix del ícono (hubo que limpiar
manualmente `app/build/intermediates/incremental/debug/mergeDebugResources`
por un lock de archivos de OneDrive que hacía fallar el merge de recursos
-- no relacionado con el cambio en sí).

**Pendiente para la próxima sesión**: limpiar el `photoUrl` de la cuenta
"Administrativo Demo" (Hallazgo 2); mismos pendientes de la Parte B
(revisar menciones de "EAFIT" fuera de código, decidir si
`appeafit-297d5` se migra).

---

### 2026-09-18 — Santiago Guerrero Parrado

**Parte D: unificar las dos pantallas de arranque + logo más grande**
(pedido explícito, con capturas propias del usuario adjuntas: *"cuando se
esta abriendo la app, aparece un fondo blanco y el icono de la app, quiero
que esa pagina desaparesca y mejor en la parte que viene despues... pon el
icono de la app en vez de esa E con el circulo blanco y el background de
la pagina con ese azul degradado que tiene"*, seguido de *"lo que quiero
es que solo aparesca esta [captura del splash de Compose] y que el logo
este un poco más grande"*).

Contexto técnico: el arranque de la app tiene dos pantallas de splash en
secuencia, inevitables en Android 12+ (no se puede saltar la del sistema):
1. El splash del **sistema operativo** (`Theme.AppEafit.Splash`,
   `windowSplashScreenAnimatedIcon`), que se dibuja antes de que el
   proceso de la app termine de arrancar.
2. El splash **de la propia app** (`SplashScreen.kt`, un composable en
   Compose), que se muestra mientras se resuelve el estado de sesión
   (Firebase Auth) antes de navegar a Login/Home.

Lo que el usuario reportó como "fondo blanco con el ícono" era en
realidad un salto entre 3 fondos distintos en 3 pasos del arranque: splash
del sistema (azul marino sólido) → fondo blanco por defecto de
`Theme.Material.Light` (el tema post-splash, visible una fracción de
segundo antes de que Compose dibuje su primer frame) → splash de Compose
(degradado azul). Y `SplashScreen.kt` todavía tenía una "E" de placeholder
en vez del escudo real (quedó así de antes del rebranding a IAFIC).

**Cambios**:
- `themes.xml`: `Theme.AppEafit` (tema post-splash) ahora define
  `android:windowBackground = @color/eafit_navy` -- mismo azul marino que
  el splash del sistema, así que no hay flash blanco entre ambos splashes.
- `themes.xml`: `Theme.AppEafit.Splash` ahora usa un ícono nuevo,
  `ic_splash_os_icon.png` (escudo real ya con un círculo blanco horneado
  detrás, generado con `sharp`), en vez del escudo simple sobre fondo
  transparente. Se probó primero `windowSplashScreenIconBackgroundColor`
  (el atributo "oficial" para esto) pero no se veía en pruebas reales en
  el emulador -- posible limitación de la librería
  `androidx.core:core-splashscreen` en este dispositivo/versión -- así
  que se optó por hornear el círculo directamente en el PNG, que sí
  funciona siempre sin depender de esa API.
- `SplashScreen.kt`: los círculos y la imagen del escudo se agrandaron
  (anillo exterior 112dp→148dp, círculo blanco 92dp→124dp, escudo
  72dp→102dp) para que el logo se vea más grande, como pidió Santiago.

Con esto, el splash del sistema y el de Compose ahora se ven como una
sola pantalla continua (círculo blanco con el escudo sobre fondo azul
marino/degradado), sin ningún flash blanco de por medio -- verificado con
capturas en ráfaga (cada ~150ms) durante el arranque en frío en el
emulador.

**Verificado**: `./gradlew assembleDebug` exitoso; arranque en frío
verificado con múltiples capturas en ráfaga en el emulador, tanto del
splash del sistema (círculo blanco + escudo grande sobre azul marino)
como de la transición al splash de Compose (degradado azul, mismo círculo
y escudo, spinner de carga) -- sin flash blanco entre ninguna de las
pantallas.

**Corrección posterior (mismo día)**: Santiago reportó que en la primera
pantalla (splash del sistema) el escudo no quedaba centrado dentro del
círculo, y que al agrandar el círculo el escudo se había quedado chico.
Causa raíz: el recorte original del escudo (`extract({left:0, top:0,
width:230, height:267})` sobre `iafic_logo_original.png`, 718x267) no era
un recorte ajustado -- dejaba ~39px de margen vacío a la izquierda y
además CORTABA ~7px del borde derecho del escudo (el escudo real ocupa
de x=39 a x=237, no de x=0 a x=230), lo que producía el desalineamiento.
Y como ese PNG con margen desparejo se usaba tal cual dentro de los
círculos en Compose y en el ícono del splash del sistema, agrandar el
contenedor solo agrandaba el margen vacío, no el escudo en sí.

Fix: se escaneó pixel por pixel `iafic_logo_original.png` para encontrar
el cuadro delimitador exacto del escudo (sin el logotipo de texto al
lado): x de 39 a 237, y de 5 a 249. Se regeneraron `ic_splash_logo.png` y
`ic_splash_os_icon.png` a partir de ese recorte exacto, con el escudo
ocupando ahora la mayor parte de su propio lienzo (antes tenía margen
interno desparejo) -- esto corrige el desalineamiento Y hace que el
escudo se vea notablemente más grande en ambas pantallas de splash sin
tocar el código de `SplashScreen.kt`. Verificado de nuevo con capturas en
ráfaga: escudo centrado y grande tanto en el splash del sistema como en
el de Compose.
