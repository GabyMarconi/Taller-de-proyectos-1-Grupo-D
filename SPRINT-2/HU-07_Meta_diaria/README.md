# HU7 - Act 1 y 3 - Creación de la pantalla Meta Diaria
- Realizado por Elizabeth Carina Lavilla Pillco
## Aportes realizados
- Implementación de interfaz para selección de meta diaria (5, 10, 15 minutos).
- Lógica para calcular progreso diario basado en tiempo estudiado vs meta.

## Tecnología
- Android Studio (Java)

## Estado
- Listo para integración con Firebase (pendiente de Alejandro).

## Notas
- Código ubicado en `src/`.
- No incluye persistencia ni conexión a BD.

## Añadir lo siguiente en Build Gradle .kts
## A nivel Project
- plugins {
- id("org.jetbrains.kotlin.android") version "1.9.0" apply false
- id("com.google.gms.google-services") version "4.4.3" apply false
  - ...
- }


## A nivel Module :app
- plugins {
  - id("org.jetbrains.kotlin.android")
  - id("com.google.gms.google-services")
  - ...
- }
- 
- dependencies {
  - ...
  - // Material Design 3
  - implementation("com.google.android.material:material:1.11.0")
  - 
  - // Lottie para animaciones
  - implementation("com.airbnb.android:lottie:6.3.0")
  - 
  - // Firebase
  - implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
  - implementation("com.google.firebase:firebase-auth")
  - implementation("com.google.firebase:firebase-firestore")
  - implementation("com.google.firebase:firebase-database")
  - 
  - testImplementation("junit:junit:4.13.2")
  - androidTestImplementation("androidx.test.ext:junit:1.1.5")
  - androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  - 
  - //google services
  - implementation ("com.google.android.gms:play-services-location:21.3.0")
- }