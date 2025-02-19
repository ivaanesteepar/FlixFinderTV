# Recomendador de Películas y Series con Jetpack Compose y Firebase

**Trabajo de Fin de Grado**  
Grado de Ingeniería Informática - Universidad de Burgos

## Descripción

Este proyecto consiste en el desarrollo de una aplicación Android para la recomendación de películas y series, utilizando tecnologías modernas como **Jetpack Compose** para la interfaz de usuario y **Firebase** como backend. La app ofrece recomendaciones personalizadas basadas en un conjunto de datos de películas y series almacenados en Firebase, y utiliza un archivo CSV como fuente de datos.

La aplicación cuenta con una **interfaz de usuario intuitiva y agradable**, diseñada para ofrecer una experiencia fluida y moderna. Aprovechando las ventajas de Jetpack Compose, la navegación entre las pantallas es simple y accesible. Además, el diseño se adapta de manera eficiente a diferentes tamaños de pantalla, brindando una experiencia consistente en dispositivos móviles y tablets. 

El sistema de recomendaciones se basa en los datos almacenados en Firebase, lo que permite un acceso rápido y flexible a las sugerencias. La aplicación está pensada para que los usuarios puedan descubrir nuevas películas y series de forma cómoda y personalizada, según sus gustos y preferencias.


## Tecnologías utilizadas

- **Jetpack Compose**: Para el diseño y la creación de la interfaz de usuario de manera moderna y eficiente.
- **Firebase**: Para el almacenamiento en la nube de los datos de películas/series y para la autenticación de usuarios si es necesario.
- **Kotlin**: Lenguaje de programación utilizado para el desarrollo de la app Android.
- **CSV**: El dataset de películas/series está almacenado en formato CSV, que se importa una sola vez al ejecutar la app para luego almacenarse en Firebase.

## Funcionalidades principales

- **Recomendaciones personalizadas**: Ofrece sugerencias de películas y series basadas en un conjunto de datos.
- **Interfaz fluida y moderna**: Usando Jetpack Compose, se ha logrado una interfaz de usuario intuitiva y atractiva.
- **Almacenamiento en Firebase**: Los datos se almacenan en Firebase para facilitar su acceso y gestión desde cualquier dispositivo.

## Estructura del Proyecto

- **UI (Interfaz de usuario)**: La interfaz está estructurada con Jetpack Compose y se encuentra organizada en la carpeta `com.example.flixfindertv.ui.screens`.
- **Firebase**: Los datos son almacenados en Firebase y la integración con la base de datos se realiza a través de las librerías oficiales de Firebase para Android.
- **Navigation**: La navegación entre pantallas está manejada mediante un menú inferior, ubicado en la carpeta `com.example.flixfindertv.ui.screens`, con un código reutilizable en la carpeta `utils`.

## Cómo usar la aplicación

1. **Clona este repositorio**:
   ```bash
   git clone <url del repositorio>
2. Configura Firebase

   1. Crea un proyecto en Firebase Console.
   2. Añade el archivo `google-services.json` al proyecto de Android.
   3. Configura Firebase en el proyecto de Android siguiendo la [documentación oficial de Firebase para Android](https://firebase.google.com/docs/android/setup).

3. Importa el dataset

   - El dataset de películas y series debe ser importado a Firebase una sola vez al ejecutar la aplicación, a partir de un archivo CSV.

4. Ejecuta la app

   1. Abre el proyecto en Android Studio.
   2. Compila y ejecuta la aplicación en un dispositivo o emulador Android.
