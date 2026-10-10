# Bolilla · Práctica Calificada 01

Aplicación Android en Kotlin y Jetpack Compose para autenticación por correo y contraseña, registro de una jugada de seis números y seguimiento en vivo de las bolillas con Firebase Authentication y Cloud Firestore.

## Datos para la entrega

- Integrante(s): completar nombre(s) y código(s) antes de entregar.
- Repositorio: `codigo-apellido-pc01-dsm202602-loteria`.

## Preparación de Firebase

1. El archivo `app/google-services.json` debe corresponder al `applicationId` de la app: `dev.PC22200322y22200350.pc1`.
2. En Firebase Authentication, habilita el proveedor **Correo electrónico/Contraseña**.
3. Crea una base de datos de Cloud Firestore y publica las reglas incluidas en `firestore.rules`.
4. Desde Firestore, crea este documento:

   ```text
   sorteos/pc01_loteria_2026
   ```

   Con campos:

   ```text
   id       string     "pc01_loteria_2026"
   nombre   string     "Sorteo de prueba"
   fecha    string     "Fecha del sorteo"
   estado   string     "abierto"
   ```

   El campo `estado` admite `abierto`, `en curso` o `finalizado`.

5. Instala y abre la app en dos emuladores. Crea una cuenta distinta en cada uno y registra las jugadas del enunciado:
   - Usuario A: `5, 12, 18, 23, 34, 41`
   - Usuario B: `5, 12, 18, 23, 34, 42`
6. Cambia el estado a `en curso` desde la consola. Crea cada documento de bolilla, de uno en uno, en:

   ```text
   sorteos/pc01_loteria_2026/bolillas/01
   ```

   Cada documento debe tener `numero` (number) y `orden` (number), ambos enteros. Continúa con IDs `02` a `06` y órdenes consecutivos del 1 al 6; espera a que las dos pantallas reflejen cada bolilla antes de publicar la siguiente. Al terminar, cambia `estado` a `finalizado`.

## Estructura principal

```text
data/model/              Modelos de sorteo, bolilla y jugada
data/remote/             Repositorios Firebase Auth y Firestore
presentation/auth/       Login y registro de cuenta
presentation/home/       Selección y guardado de la jugada
presentation/realtime/   Seguimiento con listeners de Firestore
presentation/navigation/ Flujo de autenticación y navegación
```

La pantalla de seguimiento retira sus listeners al salir. El resultado no se guarda en Firestore: se calcula localmente con la jugada del usuario y las bolillas recibidas.
