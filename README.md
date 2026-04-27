# Pixel Bloom
Se trata de una aplicación donde los usuarios pueden encontrar inspiración en Pixel Art, crear y organizar tableros personalizados, y participar en retos diarios que le ayuden a inspirarse en la creación de obras originales y mejorar día a día su técnica.

# Características principales

## Autenticación de Usuarios
Desde esta pantalla los usuarios pueden registrarse e iniciar sesión de forma segura. La aplicación mantiene la sesión activa para facilitar el acceso rápido.

<img width="1080" height="2400" alt="00" src="https://github.com/user-attachments/assets/2093a7f5-f1c6-4e24-ae8d-b5e55f1e2523" />

## Feed de Inspiración
Desde esta pantalla los usuarios pueden explorar un feed constante de nuevas referencias e ideas de Pixel Art. Descubrir estilos y temáticas diferentes para nutrir su creatividad. 

![01]("https://github.com/user-attachments/assets/e3be6664-c242-49a8-abcb-ecb057facbce")

## Detalles de una Referencia
Al seleccionar una referencia, podrás ver sus detalles completos y guardarla en tus propios tableros.

![05]("https://github.com/user-attachments/assets/5d515b53-2c4d-4a41-a3c1-7bae49409316")

## Tableros Personalizados y retos del usuario
Desde aquí el usuario puede crear, organizar y gestionar tus propios tableros. Se podrán tener organizadas las referencias favoritas del feed o el historial de retos completados. 

![02]("https://github.com/user-attachments/assets/d94c802a-de9c-41d4-9e68-b895894b4683")

## Detalles de un reto de usuario
Al seleccionar un reto podrá acceder al detalle del mismo y completarlo. Una vez completado el usuario podrá acceder siempre que quiera a visualizarlo o editarlo.

![06]("https://github.com/user-attachments/assets/994abf6c-0724-409e-bfb5-7bb9324fd213")

Si el reto no está completado la pantalla mostrará una imagen por defecto e indicará que el reto aún no se ha completado.

![07]("https://github.com/user-attachments/assets/958baf16-f4d3-4c65-bfeb-a2e113624d47")

## Paletas de Colores Generativas
Para ayudar a los usuarios sus creaciones, la aplicación incluye una sección de retos diarios/semanales. Esta sección les permite acceder a una serie de propuestas inspiradoras para crear nuevos proyectos de Pixel Art.

![03](<img width="1080" height="2400" alt="03" src="https://github.com/user-attachments/assets/03846ddf-b5f6-4fa9-980b-9766b6cbc449" />)

## Ajustes y Personalización
Desde el menú de ajustes se podrá editar el tema de la aplicación, idioma, acceder a la información del desarrollador de la app y cerrar la sesión.

![04](<img width="1080" height="2400" alt="04" src="https://github.com/user-attachments/assets/582e8599-406f-4cc1-9fad-59df4b5e1658" />)

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
