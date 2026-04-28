# Pixel Bloom
Se trata de una aplicación donde los usuarios pueden encontrar inspiración en Pixel Art, crear y organizar tableros personalizados, y participar en retos diarios que le ayuden a inspirarse en la creación de obras originales y mejorar día a día su técnica.

# Características principales
## Onboarding
Al ejecutar la aplicación por primera vez el usuario tendrá un onBoarding para iniciarse en Pixel Bloom. 

<img width="270" height="600" alt="09" src="https://github.com/user-attachments/assets/70fbbc86-6128-4bae-af9d-5786c671276e" />

## Pantalla de Autenticación de Usuarios
Desde esta pantalla los usuarios pueden registrarse e iniciar sesión de forma segura. La aplicación mantiene la sesión activa para facilitar el acceso rápido.

<img width="270" height="600" alt="00" src="https://github.com/user-attachments/assets/2093a7f5-f1c6-4e24-ae8d-b5e55f1e2523" />

## Guía interactiva
Una vez se registre o inicie sesión por primera vez se ejecutará una guía interactiva

<img width="270" height="600" alt="10" src="https://github.com/user-attachments/assets/8dfb17b9-5fc6-42a9-b915-63f6af7de4b9" />

## Pantalla de Feed de Inspiración
Desde esta pantalla los usuarios pueden explorar un feed constante de nuevas referencias e ideas de Pixel Art. Descubrir estilos y temáticas diferentes para nutrir su creatividad. 

<img width="270" height="600" alt="01" src="https://github.com/user-attachments/assets/72c7ba86-f53c-4fe3-8378-46df339f64f9" />

## Pantalla de Detalles de una Referencia
Al seleccionar una referencia, podrás ver sus detalles completos y guardarla en tus propios tableros.

<img width="270" height="600" alt="05" src="https://github.com/user-attachments/assets/8b19b165-86f4-43d0-a5e6-5fa5264642ed" />

## Pantalla de tableros y retos del usuario
Desde aquí el usuario puede crear, organizar y gestionar tus propios tableros. Se podrán tener organizadas las referencias favoritas del feed o el historial de retos completados. 

<img width="270" height="600" alt="02" src="https://github.com/user-attachments/assets/9e91ea95-0e60-45d5-a6f5-2533a77276a1" />

## Pantalla de detalles de retos de usuario
Al seleccionar un reto podrá acceder al detalle del mismo y completarlo. Una vez completado el usuario podrá acceder siempre que quiera a visualizarlo o editarlo.

<img width="270" height="600" alt="06" src="https://github.com/user-attachments/assets/f4805b09-889a-47b6-bdf8-cd18b7c954b8" />

Si el reto no está completado la pantalla mostrará una imagen por defecto e indicará que el reto aún no se ha completado.

<img width="270" height="600" alt="07" src="https://github.com/user-attachments/assets/d9ee128a-b494-4004-a150-2ce2556a1e95" />

## BottomSheet para mover las referencias entre tableros
El usuarios podrá mover y organizar las referencias entre sus tableros siempre que lo desee. También podrá crear nuevos tableros desde la pantalla de feed o su espacio personal. 

<img width="270" height="600" alt="08" src="https://github.com/user-attachments/assets/7a627820-7c28-4af0-b95d-b8c8d86f7aab" />

## Pantalla para aceptar un reto semanal
Para ayudar a los usuarios sus creaciones, la aplicación incluye una sección de retos semanales. Esta sección les permite acceder a una serie de propuestas inspiradoras para crear nuevos proyectos de Pixel Art.

<img width="270" height="600" alt="03" src="https://github.com/user-attachments/assets/95ddc614-deb7-4fb9-a40e-b7999d7dccc7" />

## Menú de ajustes
Desde el menú de ajustes se podrá editar el tema de la aplicación, idioma, acceder a la información del desarrollador de la app y cerrar la sesión.

<img width="270" height="600" alt="04" src="https://github.com/user-attachments/assets/565c45e6-a190-4ceb-8333-69820f1fed6b" />

# Tecnologías utilizadas

Para el desarrollo de esta aplicación se han empleado las siguientes tecnologías:
* Uso de **Firebase Authentication** para la gestión segura de usuarios.
* Uso de **Firebase Firestore** como base de datos en la nube para guardar tableros, referencias y retos.
* Uso de **Firebase Storage** para el almacenamiento de imágenes.
* **Retrofit** para el consumo de la API externa utilizada en la generación de paletas de color.
* **Picasso** para la carga eficiente de imágenes desde URLs de internet.
* **RecyclerView** y **CardView** para la visualización fluida de listas (feed, tableros, retos).
* **Navigation Component** para una navegación robusta y estructurada entre los diferentes fragmentos.
* **Corrutinas** y **LiveData** para el manejo asíncrono y reactivo de los datos.
* **Photo Picker** para el acceso a la galería del dispositivo de forma segura. 
* **JUnit y MockK** para la ejecución de pruebas unitarias.
* **Espresso ** Para la ejecución de pruebas integrales de UI.

# Instrucciones de uso

Para probar la aplicación:
1. Copia el enlace del repositorio: `https://github.com/AnaRoGon/PixelBloomApp-V1-PFDAM.git`
2. En Android Studio, selecciona: `File -> New -> Project from Version Control` y pega el enlace.
3. Asegúrate de configurar el archivo `google-services.json` necesario para que Firebase funcione correctamente en tu entorno local.
4. Ejecuta el proyecto en tu emulador o dispositivo físico.
