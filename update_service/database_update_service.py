
import requests
import firebase_admin
import time
import json
from firebase_admin import credentials, firestore
from cryptography.fernet import Fernet
import base64

# Configuración de la API de TMDb
BASE_URL = 'https://api.themoviedb.org/3'

# Inicializar Firebase
cred = credentials.Certificate('flixfindertv-1f381-firebase-adminsdk-fbsvc-b62fc4096c.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

# Clave de cifrado original
with open('config.json', 'r') as config_file:
    config = json.load(config_file)
    key = bytes(config['clave_encriptacion'], 'utf-8')

# Inicializa el objeto Fernet con la clave
cipher = Fernet(key)

# Recupera el documento de Firebase
doc_ref = db.collection('apiKeys').document('tmdbApiKey')
doc = doc_ref.get()

if doc.exists:
    # Obtén el valor del campo 'key' que está en base64
    encrypted_base64 = doc.to_dict().get('key')

    # Decodifica la base64 a bytes
    encrypted_bytes = base64.b64decode(encrypted_base64)

    # Desencripta la API key
    API_KEY = cipher.decrypt(encrypted_bytes).decode('utf-8')

    print("API Key recuperada")
else:
    print("No se encontró el documento o la clave API no está almacenada.")


# Colecciones de Firestore
peliculas_collection = db.collection('peliculas')
series_collection = db.collection('series')
config_collection = db.collection('config')
eliminadas_collection = db.collection('eliminadas')
generos_collection = db.collection('generos')


def obtener_trailer(tipo, id_contenido):
    url = f"{BASE_URL}/{tipo}/{id_contenido}/videos?api_key={API_KEY}&language=en-US"
    respuesta = requests.get(url)

    if respuesta.status_code == 200:
        videos = respuesta.json().get('results', [])
        for video in videos:
            if video.get('type') == 'Trailer' and video.get('site') == 'YouTube':
                return f"https://www.youtube.com/watch?v={video['key']}"

    return None


def obtener_director(tipo, id_contenido):
    url = f"{BASE_URL}/{tipo}/{id_contenido}/credits"
    params = {
        "api_key": API_KEY,
        "language": "en-US"
    }
    try:
        response = requests.get(url, params=params)
        if response.status_code == 200:
            data = response.json()
            crew = data.get("crew", [])

            # Buscar primero Director
            persona = next((p for p in crew if p.get("job") == "Director"), None)

            # Si no se encuentra Director, buscar Executive Producer
            if not persona:
                persona = next((p for p in crew if p.get("job") == "Executive Producer"), None)

            if persona:
                nombre_director = persona.get("name")
                foto_director = persona.get("profile_path")
                if foto_director:
                    foto_director_url = f"https://image.tmdb.org/t/p/w500{foto_director}"
                else:
                    foto_director_url = None
                return nombre_director, foto_director_url
        else:
            print(f"Error al obtener director para {tipo} ID {id_contenido}: {response.status_code}")
    except Exception as e:
        print(f"Excepción al obtener director: {e}")

    return None, None


def obtener_generos(api_key):
    # Obtener géneros de películas
    url_movies = f"{BASE_URL}/genre/movie/list?api_key={api_key}&language=en-US"
    respuesta_movies = requests.get(url_movies)

    # Obtener géneros de series
    url_tv = f"{BASE_URL}/genre/tv/list?api_key={api_key}&language=en-US"
    respuesta_tv = requests.get(url_tv)

    # Verificar que las respuestas sean exitosas
    if respuesta_movies.status_code == 200 and respuesta_tv.status_code == 200:
        generos_movies = respuesta_movies.json()['genres']
        generos_tv = respuesta_tv.json()['genres']
        return generos_movies + generos_tv  # Combinamos los géneros de películas y series
    else:
        print("Error al obtener los géneros.")
        return []


def guardar_generos(generos):
    # Colección de géneros en Firestore
    generos_collection = db.collection('generos')

    for genero in generos:
        # Comprobar si el género ya existe en Firestore
        doc_ref = generos_collection.document(str(genero['id']))
        doc = doc_ref.get()

        if not doc.exists:  # Si el género no existe, lo guardamos
            generos_collection.document(str(genero['id'])).set({
                'id': genero['id'],
                'name': genero['name']
            })
            print(f"Género {genero['name']} guardado correctamente.")
        else:
            print(f"Género {genero['name']} ya existe, no se duplicará.")


def obtener_y_guardar_generos(api_key):
    generos = obtener_generos(api_key)
    if generos:
        guardar_generos(generos)
    else:
        print("No se obtuvieron géneros.")


def obtener_5_peliculas_recientes(paginas=20):
    resultados = []
    for pagina in range(1, paginas + 1):
        url = f"{BASE_URL}/movie/now_playing?api_key={API_KEY}&page={pagina}"
        respuesta = requests.get(url)

        if respuesta.status_code == 200:
            resultados_pagina = respuesta.json()['results']
            resultados.extend(resultados_pagina)
        else:
            print(f"Error al obtener películas en la página {pagina}")

    # Ordenar por fecha de estreno (release_date), descendente
    resultados_ordenados = sorted(resultados, key=lambda x: x.get('release_date', ''), reverse=True)
    for pelicula in resultados_ordenados[:5]:
        print(f"ID: {pelicula.get('id')}, Fecha de estreno: {pelicula.get('release_date')}")

    return resultados_ordenados[:5]


def obtener_5_series_recientes(paginas=20):
    resultados = []
    for pagina in range(1, paginas + 1):
        url = f"{BASE_URL}/tv/on_the_air?api_key={API_KEY}&page={pagina}"
        respuesta = requests.get(url)

        if respuesta.status_code == 200:
            resultados_pagina = respuesta.json()['results']
            resultados.extend(resultados_pagina)
        else:
            print(f"Error al obtener series en la página {pagina}")

    # Ordenar por fecha de primer episodio emitido (first_air_date), descendente
    resultados_ordenados = sorted(resultados, key=lambda x: x.get('first_air_date', ''), reverse=True)
    for serie in resultados_ordenados[:5]:
        print(f"ID: {serie.get('id')}, Fecha de estreno: {serie.get('first_air_date')}")

    return resultados_ordenados[:5]


def obtener_datos():
    todas_las_peliculas = []
    todas_las_series = []

    ultima_pagina_peliculas = 0
    ultima_pagina_series = 0

    # Obtener todas las películas de las primeras 50 páginas
    for pagina in range(1, 51):
        print(f"Obteniendo películas de la página {pagina}...")
        url = f"{BASE_URL}/movie/popular?api_key={API_KEY}&page={pagina}"
        respuesta = requests.get(url)

        if respuesta.status_code == 200:
            peliculas = respuesta.json().get('results', [])
            todas_las_peliculas.extend(peliculas)
            ultima_pagina_peliculas = pagina  # Actualizar la última página exitosa
        else:
            print(f"Error al obtener películas de la página {pagina}")
            break  # Detenerse si hay un error

    # Obtener todas las series de las primeras 50 páginas
    for pagina in range(1, 51):
        print(f"Obteniendo series de la página {pagina}...")
        url = f"{BASE_URL}/tv/popular?api_key={API_KEY}&page={pagina}"
        respuesta = requests.get(url)

        if respuesta.status_code == 200:
            series = respuesta.json().get('results', [])
            todas_las_series.extend(series)
            ultima_pagina_series = pagina  # Actualizar la última página exitosa
        else:
            print(f"Error al obtener series de la página {pagina}")
            break  # Detenerse si hay un error

    print(f"Última página de películas procesada: {ultima_pagina_peliculas}")
    print(f"Última página de series procesada: {ultima_pagina_series}")

    return todas_las_peliculas, todas_las_series


def obtener_status_pelicula(movie_id):
    # Obtener el estado de una película desde TMDb
    url = f"{BASE_URL}/movie/{movie_id}?api_key={API_KEY}&language=en-US"
    respuesta = requests.get(url)

    if respuesta.status_code == 200:
        datos = respuesta.json()
        return datos.get('status', 'Desconocido')  # Devuelve el status de la película
    else:
        print(f"Error al obtener el estado de la película con ID {movie_id}")
        return 'Desconocido'


def obtener_status_serie(tv_id):
    # Obtener el estado de una serie desde TMDb
    url = f"{BASE_URL}/tv/{tv_id}?api_key={API_KEY}&language=en-US"
    respuesta = requests.get(url)

    if respuesta.status_code == 200:
        datos = respuesta.json()
        return datos.get('status', 'Desconocido')  # Devuelve el status de la serie
    else:
        print(f"Error al obtener el estado de la serie con ID {tv_id}")
        return 'Desconocido'


def obtener_numero_temporadas_serie(tv_id):
    url = f"{BASE_URL}/tv/{tv_id}?api_key={API_KEY}&language=es-ES"
    respuesta = requests.get(url)
    if respuesta.status_code == 200:
        datos = respuesta.json()
        return datos.get('number_of_seasons', 0)  # Devuelve 0 si no existe
    else:
        print(f"Error al obtener número de temporadas para serie {tv_id}: {respuesta.status_code}")
        return 0


def guardar_en_db(datos, coleccion, peliculas_eliminadas):
    contador = 0

    for item in datos:
        if str(item['id']) in peliculas_eliminadas:
            print(f"La {coleccion.id[:-1]} con ID {item['id']} ya fue eliminada y no se guardará.")
            continue

        if 'title' in item:  # Película
            status = obtener_status_pelicula(item['id'])
            trailer_url = obtener_trailer('movie', item['id'])
            director_nombre, director_foto_url = obtener_director('movie', item['id'])
            numero_temporadas = None

        elif 'name' in item:  # Serie
            status = obtener_status_serie(item['id'])
            trailer_url = obtener_trailer('tv', item['id'])
            director_nombre, director_foto_url = obtener_director('tv', item['id'])
            numero_temporadas = obtener_numero_temporadas_serie(item['id'])

        else:
            status = 'Desconocido'
            trailer_url = None
            director_nombre, director_foto_url = None, None
            numero_temporadas = None

        doc_ref = coleccion.document(str(item['id']))
        pelicula = {
            "id": str(item['id']),
            "title": item.get('title', None),
            "name": item.get('name', None),
            "overview": item.get('overview', ''),
            "release_date": item.get('release_date', None),
            "release_date_series": item.get('first_air_date', None),
            "poster_path": item.get('poster_path', ''),
            "vote_average": str(item.get('vote_average', '0.0')),
            "vote_count": str(item.get('vote_count', '0')),
            "genre_ids": item.get('genre_ids', []),
            "adult": item.get('adult', False),
            "original_language": item.get('original_language', 'en'),
            "backdrop_path": item.get('backdrop_path', ''),
            "popularity": item.get('popularity', 0.0),
            "status": status,
            "esSerie": 'name' in item,
            "trailer": trailer_url,
            "director_name": director_nombre,
            "director_photo_url": director_foto_url,
            "seasons": numero_temporadas
        }

        doc_ref.set(pelicula, merge=True)
        print(f"Guardando {coleccion.id[:-1]} con ID {item['id']}...")

        contador += 1

    print(f"Se han guardado {contador} elementos.")


# Función para obtener todas las películas y series de Firebase
def obtener_todas_las_peliculas_y_series_firebase():
    # Obtener todas las películas
    peliculas_docs = peliculas_collection.stream()
    todas_las_peliculas = [doc.to_dict() for doc in peliculas_docs]

    # Obtener todas las series
    series_docs = series_collection.stream()
    todas_las_series = [doc.to_dict() for doc in series_docs]

    # Mostrar el número de películas y series obtenidas
    print(f"Se han obtenido {len(todas_las_peliculas)} películas y {len(todas_las_series)} series de Firebase.")

    return todas_las_peliculas, todas_las_series


# Función para obtener las películas y series eliminadas previamente
def obtener_eliminadas():
    peliculas_eliminadas_ref = eliminadas_collection.document('peliculas')
    series_eliminadas_ref = eliminadas_collection.document('series')

    peliculas_eliminadas_doc = peliculas_eliminadas_ref.get()
    series_eliminadas_doc = series_eliminadas_ref.get()

    peliculas_eliminadas = peliculas_eliminadas_doc.to_dict().get('eliminadas', []) if peliculas_eliminadas_doc.exists else []
    series_eliminadas = series_eliminadas_doc.to_dict().get('eliminadas', []) if series_eliminadas_doc.exists else []

    # Imprimir título (title de Firebase) y ID de las películas eliminadas
    print("Películas eliminadas:")
    for peli in peliculas_eliminadas:
        print(f"ID: {peli}")

    # Imprimir título (name de la API) y ID de las series eliminadas
    print("Series eliminadas:")
    for serie in series_eliminadas:
        print(f"ID: {serie}")

    return peliculas_eliminadas, series_eliminadas


def eliminar_5_antiguas(coleccion, peliculas_o_series_ordenadas):
    # Obtener las primeras 5 películas o series más antiguas
    peliculas_o_series_antiguas = peliculas_o_series_ordenadas[:5]

    # Verificar si hay IDs duplicados en los elementos a eliminar
    ids = [item['id'] for item in peliculas_o_series_antiguas]
    print(f"IDs a eliminar: {ids}")
    print(f"IDs únicos a eliminar: {set(ids)}")
    if len(ids) != len(set(ids)):
        print("¡Alerta! Hay IDs duplicados en la lista de eliminación.")

    # Inicializar un contador de eliminaciones
    contador_eliminaciones = 0

    print(f"Se van a eliminar {len(peliculas_o_series_antiguas)} elementos")

    for item in peliculas_o_series_antiguas:
        print(f"ID de documento a eliminar: {item['id']}")
        # Verificar si es una película o una serie
        if 'title' in item and item['title'] is not None:  # Es una película
            titulo = item.get('title', 'No disponible')
            fecha = item.get('release_date', 'Fecha no disponible')

            # Imprimir información de la película a eliminar
            print(f"Eliminando película: {titulo} - Fecha: {fecha}")

            # Eliminar el documento de la película
            doc_ref = coleccion.document(str(item['id']))
            doc_ref.delete()

            # Guardar el ID de la película eliminada en la colección 'eliminadas' (peliculas)
            eliminadas_collection.document('peliculas').set({
                'eliminadas': firestore.ArrayUnion([str(item['id'])])
            }, merge=True)

            # Aumentar el contador de eliminaciones
            contador_eliminaciones += 1

        elif 'name' in item and item['name'] is not None:  # Es una serie
            titulo = item.get('name', 'No disponible')
            fecha = item.get('release_date_series', item.get('first_air_date', 'Fecha no disponible'))

            # Imprimir información de la serie a eliminar
            print(f"Eliminando serie: {titulo} - Fecha: {fecha}")

            # Eliminar el documento de la serie
            doc_ref = coleccion.document(str(item['id']))
            doc_ref.delete()

            # Guardar el ID de la serie eliminada en la colección 'eliminadas' (series)
            eliminadas_collection.document('series').set({
                'eliminadas': firestore.ArrayUnion([str(item['id'])])
            }, merge=True)

            # Aumentar el contador de eliminaciones
            contador_eliminaciones += 1
        
        time.sleep(1)

    # Al final, imprimir cuántos elementos fueron eliminados
    print(f"Se eliminaron {contador_eliminaciones} elementos.")


def contar_duplicados(coleccion):
    # Obtener todos los documentos de la colección
    documentos = coleccion.get()

    # Usar un diccionario para contar los duplicados por 'id'
    ids = {}
    for doc in documentos:
        doc_data = doc.to_dict()
        doc_id = doc_data.get('id')
        
        if doc_id in ids:
            ids[doc_id] += 1
        else:
            ids[doc_id] = 1
    
    # Contar cuántos duplicados hay (es decir, aquellos que tienen más de 1 ocurrencia)
    duplicados = {key: value for key, value in ids.items() if value > 1}
    print(f"Se encontraron {len(duplicados)} duplicados en la colección.")
    for doc_id, count in duplicados.items():
        print(f"ID duplicado: {doc_id} - Contador: {count}")


def obtener_y_guardar():
    peliculas_eliminadas = []
    series_eliminadas = []

    # Guardamos los géneros si no estaban guardados
    generos_count = len(generos_collection.get())

    if generos_count == 0:
        print("La colección de géneros está vacía. Guardando los géneros...")
        obtener_y_guardar_generos(API_KEY)
    else:
        print("La colección de géneros ya tiene datos. No se guardarán los géneros.")

    # Obtener el número de documentos en la colección de películas y series
    peliculas_count = len(peliculas_collection.get())
    series_count = len(series_collection.get())

    # Imprimir las cantidades de películas y series en las colecciones
    print(f"Cantidad de películas en Firestore inicialmente: {peliculas_count}")
    print(f"Cantidad de series en Firestore inicialmente: {series_count}")

    # Si las colecciones están vacías, procederemos a guardar las 50 primeras páginas (no hay eliminadas asi que se meten todas)
    if peliculas_count == 0 and series_count == 0:
        print("Las colecciones están vacías. Guardando las primeras 50 páginas de películas y series...")

        # No hay ni peliculas ni series eliminadas aqui
        peliculas_eliminadas, series_eliminadas = [],[]

        # Obtener todas las películas y series de las primeras 50 páginas de la API
        todas_las_peliculas, todas_las_series = obtener_datos()

        # Guardar todas las películas y series en la base de datos, excluyendo las eliminadas
        guardar_en_db(todas_las_peliculas, peliculas_collection, peliculas_eliminadas)
        guardar_en_db(todas_las_series, series_collection, series_eliminadas)

        print("Películas y Series de las primeras 50 páginas guardadas correctamente.")

        contar_duplicados(peliculas_collection)
        contar_duplicados(series_collection)
    else:
        # Obtener todas las películas y series de firebase
        todas_las_peliculas, todas_las_series = obtener_todas_las_peliculas_y_series_firebase()

    # Continuar con el proceso de obtener y guardar las películas y series más recientes

    peliculas_eliminadas, series_eliminadas = obtener_eliminadas()

    # Imprimir el tamaño de ambas listas
    print(f"Tamaño de peliculas_eliminadas: {len(peliculas_eliminadas)}")
    print(f"Tamaño de series_eliminadas: {len(series_eliminadas)}")

    # Obtener las 5 películas y series más recientes de la página actual
    peliculas_recientes = obtener_5_peliculas_recientes()
    series_recientes = obtener_5_series_recientes()

    # Ordenar las películas y series obtenidas por fecha de lanzamiento (más antiguas primero)
    peliculas_ordenadas = sorted(todas_las_peliculas, key=lambda x: x.get('release_date', ''), reverse=False)
    series_ordenadas = sorted(todas_las_series, key=lambda x: x.get('release_date_series', x.get('first_air_date', '')), reverse=False)

    # Para las películas
    print(f"Eliminando películas más antiguas: {len(peliculas_ordenadas)} películas disponibles")
    eliminar_5_antiguas(peliculas_collection, peliculas_ordenadas)
    
    peliculas_count = len(peliculas_collection.get())
    print(f"Cantidad de películas en Firestore despues de eliminar 5 peliculas: {peliculas_count}")

    # Para las series
    print(f"Eliminando series más antiguas: {len(series_ordenadas)} series disponibles")
    eliminar_5_antiguas(series_collection, series_ordenadas)

    series_count = len(series_collection.get())
    print(f"Cantidad de series en Firestore despues de eliminar 5 series: {series_count}")

    # Guardar las 5 películas y series más recientes en la base de datos
    guardar_en_db(peliculas_recientes, peliculas_collection, peliculas_eliminadas)
    guardar_en_db(series_recientes, series_collection, series_eliminadas)

    print("Películas y Series actualizadas correctamente.")

    # Obtener el número de documentos en la colección de películas y series
    peliculas_count = len(peliculas_collection.get())
    series_count = len(series_collection.get())

    # Imprimir las cantidades de películas y series en las colecciones
    print(f"Cantidad de películas en Firestore al final: {peliculas_count}")
    print(f"Cantidad de series en Firestore al final: {series_count}")

    contar_duplicados(peliculas_collection)
    contar_duplicados(series_collection)

    
if __name__ == "__main__":
    print("Ejecutando script...")
    obtener_y_guardar()
    print("Script finalizado.")