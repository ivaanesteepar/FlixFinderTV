import requests
import firebase_admin
import time
from firebase_admin import credentials, firestore

# Configuración de la API de TMDb
API_KEY = '6ae1f349f576ac17daf45c3d7dfbae9e'
BASE_URL = 'https://api.themoviedb.org/3'

# Inicializar Firebase
cred = credentials.Certificate('C:\\Users\\Usuario\\Documents\\KeyFirebase\\flixfindertv-42323-firebase-adminsdk-fbsvc-927ed2eb0d.json')
firebase_admin.initialize_app(cred)
db = firestore.client()

# Colecciones de Firestore
peliculas_collection = db.collection('peliculas')
series_collection = db.collection('series')
config_collection = db.collection('config')
eliminadas_collection = db.collection('eliminadas')
generos_collection = db.collection('generos')

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


def obtener_pagina_actual():
    doc_ref = config_collection.document('pagina_actual')
    doc = doc_ref.get()
    
    if doc.exists:
        return doc.to_dict().get('pagina', 51)  # Empezar desde la página 51 si no existe
    else:
        # Si el documento no existe, crearlo con el valor predeterminado
        doc_ref.set({'pagina': 51})
        return 51  # Si no existe, comenzar desde la página 51


def obtener_5_recientes(tipo, pagina):
    url = f"{BASE_URL}/{tipo}/popular?api_key={API_KEY}&page={pagina}"
    respuesta = requests.get(url)
    
    if respuesta.status_code == 200:
        resultados = respuesta.json()['results']

        # Ajustamos el campo de fecha dependiendo del tipo (película o serie)
        if tipo == 'movie':
            return sorted(resultados, key=lambda x: x.get('release_date', ''), reverse=True)[:5]  # Películas por release_date
        elif tipo == 'tv':
            return sorted(resultados, key=lambda x: x.get('first_air_date', x.get('release_date_series', '')), reverse=True)[:5]  # Series por first_air_date
        else:
            print(f"Tipo desconocido: {tipo}")
            return []
    else:
        print(f"Error al obtener datos de {tipo} en la página {pagina}")
        return []


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


def guardar_en_db(datos, coleccion, peliculas_eliminadas):
    contador = 0  # Inicializamos el contador

    for item in datos:
        # Verificar si la película o serie ya fue eliminada
        if str(item['id']) in peliculas_eliminadas:
            print(f"La {coleccion.id[:-1]} con ID {item['id']} ya fue eliminada y no se guardará.")
            continue

        # Verificar si la película o serie ya existe en Firestore
        doc_ref = coleccion.document(str(item['id']))
        doc = doc_ref.get()

        if doc.exists:
            print(f"La {coleccion.id[:-1]} con ID {item['id']} ya existe en la base de datos. No se duplicará.")
            continue
        else:
            # Crear un diccionario con la información de la película o serie
            pelicula = {
                "id": str(item['id']),  # Asegurar que el ID sea un string
                "title": item.get('title', None),
                "name": item.get('name', None),
                "overview": item.get('overview', ''),
                "release_date": item.get('release_date', None),
                "release_date_series": item.get('first_air_date', None),
                "poster_path": item.get('poster_path', ''),
                "vote_average": str(item.get('vote_average', '0.0')),  # Convertir a string
                "vote_count": str(item.get('vote_count', '0')),  # Convertir a string
                "genre_ids": item.get('genre_ids', []),  # Guardar los IDs como enteros
                "adult": item.get('adult', False),
                "backdrop_path": item.get('backdrop_path', ''),
                "popularity": item.get('popularity', 0.0),
                "esSerie": 'name' in item,  # Si tiene "name", es serie
                "comentarios": []  # Lista vacía de comentarios
            }
            # Guardar en la colección de Firestore
            doc_ref.set(pelicula, merge=True)
            print(f"Guardando {coleccion.id[:-1]} con ID {item['id']}...")
            
            # Aumentamos el contador cada vez que se guarda un nuevo elemento
            contador += 1
    
    # Mostrar el número total de elementos guardados
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
    # Obtener la página actual
    pagina_actual = obtener_pagina_actual()

    # Obtener el número de documentos en la colección de películas y series
    peliculas_count = len(peliculas_collection.get())
    series_count = len(series_collection.get())

    # Imprimir las cantidades de películas y series en las colecciones
    print(f"Cantidad de películas en Firestore al principio: {peliculas_count}")
    print(f"Cantidad de series en Firestore al principio: {series_count}")

    peliculas_eliminadas, series_eliminadas = obtener_eliminadas()

    # Imprimir el tamaño de ambas listas
    print(f"Tamaño de peliculas_eliminadas: {len(peliculas_eliminadas)}")
    print(f"Tamaño de series_eliminadas: {len(series_eliminadas)}")

    # Obtener las 5 películas y series más recientes de la página actual
    peliculas_recientes = obtener_5_recientes('movie', pagina_actual)
    series_recientes = obtener_5_recientes('tv', pagina_actual)

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

    # Aumentar la página actual en Firestore
    pagina_actual += 1  # Incrementar la página en 1
    config_collection.document('pagina_actual').set({'pagina': pagina_actual})
    print(f"Página actual actualizada a {pagina_actual}.")

    
if __name__ == "__main__":
    print("Ejecutando script...")
    obtener_y_guardar()
    print("Script finalizado.")
