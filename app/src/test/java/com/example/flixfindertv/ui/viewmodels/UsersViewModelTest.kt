package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.entities.FavoritoEntity
import com.example.flixfindertv.room.repository.MovieRepository
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsersViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var favoritesCollection: CollectionReference
    private lateinit var application: Application
    private lateinit var movieRepository: MovieRepository
    private lateinit var movieDao: MovieDao

    private val pelicula1 = Peliculas(
        id = "p1",
        title = "Título de prueba",
        overview = "Una película de prueba.",
        release_date = "2023-01-01",
        poster_path = "/imagen.jpg",
        vote_average = "8.5",
        genre_ids = listOf(28, 12),
        esSerie = false,
        original_language = "es",
        status = "Released",
        trailer = "https://youtu.be/trailer123",
        director_name = "Juan Pérez",
        director_photo_url = "https://foto-director.jpg"
    )

    private val pelicula2 = Peliculas(
        id = "p1",
        title = "Título de prueba",
        overview = "Una película de prueba.",
        release_date = "2023-01-01",
        poster_path = "/imagen.jpg",
        vote_average = "8.5",
        genre_ids = listOf(28, 12),
        esSerie = false,
        original_language = "es",
        status = "Released",
        trailer = "https://youtu.be/trailer123",
        director_name = "Juan Pérez",
        director_photo_url = "https://foto-director.jpg"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // Mockear todas las clases estáticas necesarias
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        mockkStatic(Toast::class)

        movieDao = mockk(relaxed = true)
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        // Configurar mocks básicos
        context = mockk(relaxed = true) {
            every { packageName } returns "com.example.flixfindertv.test"
        }

        application = mockk(relaxed = true) {
            every { applicationContext } returns context
        }

        movieRepository = mockk<MovieRepository> {
            coEvery { getPeliculasFavoritas() } returns emptyList()
            coEvery { insertFavorito(any()) } just Runs
        }

        // Configuración de tu repositorio mock
        coEvery { movieRepository.getPeliculasFavoritas() } returns emptyList()
        coEvery { movieRepository.insertFavorito(any()) } just Runs

        // Configurar FirebaseApp mock
        val mockFirebaseApp = mockk<FirebaseApp>(relaxed = true)
        every { FirebaseApp.initializeApp(any()) } returns mockFirebaseApp
        every { FirebaseApp.getInstance() } returns mockFirebaseApp

        // Configurar FirebaseAuth mock
        auth = mockk<FirebaseAuth>(relaxed = true)
        val currentUser = mockk<FirebaseUser>(relaxed = true) {
            every { uid } returns "testUserId"
            every { email } returns "test@example.com"
        }
        every { auth.currentUser } returns currentUser
        every { FirebaseAuth.getInstance() } returns auth

        // Configurar Firestore mock
        firestore = mockk<FirebaseFirestore>(relaxed = true)
        favoritesCollection = mockk<CollectionReference>(relaxed = true)
        val documentReference = mockk<DocumentReference>(relaxed = true)

        // Asegurarse de que collection("usuarios") devuelva la colección mockeada
        every { firestore.collection("usuarios") } returns favoritesCollection
        // Configurar que collectionReference.document() devuelva un documento mockeado
        every { favoritesCollection.document(eq("testUserId")) } returns documentReference

        // Mockear el método update() de DocumentReference usando TaskCompletionSource
        val taskCompletionSource = TaskCompletionSource<Void>()
        taskCompletionSource.setResult(null)  // Simula una respuesta exitosa
        every { documentReference.update("peliculasFavoritas", any()) } returns taskCompletionSource.task

        // Mockear el método addOnSuccessListener de Task
        val task = mockk<Task<Void>>()
        every { task.addOnSuccessListener(any()) } answers {
            val listener = arg<(Void?) -> Unit>(0)
            listener.invoke(null)  // Pasamos null ya que es un valor válido para Void
            task
        }

        // Devolver el Task mockeado
        every { documentReference.update(eq("peliculasFavoritas"), any()) } returns task

        every { FirebaseFirestore.getInstance() } returns firestore

        // Crear el ViewModel usando el contexto mockeado
        usersViewModel = spyk(UsersViewModel(application))

        // Configuración del dispatcher para pruebas
        Dispatchers.setMain(dispatcher) // Usamos el dispatcher de prueba para las corrutinas.
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test agregar pelicula cuando hay espacio en favoritos`() = runTest {
        // 1. Configurar mocks
        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<FirebaseUser>()
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockDocument = mockk<DocumentSnapshot>()
        val mockUpdateTask = mockk<Task<Void>>(relaxed = true)

        // Configurar comportamiento de autenticación
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "testUserId"
        every { auth.currentUser } returns mockUser

        // Configurar comportamiento del documento
        val currentMoviesList = List(19) { // 19 movies in favorites (less than 20)
            mapOf(
                "id" to "movieId$it",
                "title" to "Movie $it",
                "posterUrl" to "http://test.com/poster.jpg",
                "esSerie" to false
            )
        }
        every { mockDocument.exists() } returns true
        every { mockDocument.get("peliculasFavoritas") } returns currentMoviesList
        every { mockTask.result } returns mockDocument
        every { mockTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocument)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        every { mockUpdateTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockUpdateTask
        }

        // Configurar Firestore
        val mockDocumentReference = mockk<DocumentReference>()
        every { firestore.collection("usuarios").document("testUserId") } returns mockDocumentReference
        every { mockDocumentReference.get() } returns mockTask
        every { mockDocumentReference.update("peliculasFavoritas", any<List<Map<String, Any>>>()) } returns mockUpdateTask

        // Mockear Toast.makeText() para evitar la llamada real
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast
        every { mockToast.show() } just Runs

        // Llamamos a la función que estamos testeando
        usersViewModel.saveToFavorites(context, "movieId20", "Test Movie", "http://test.com/poster.jpg", false)

        // Esperar a que las operaciones asíncronas se completen
        advanceUntilIdle()

        // Verifica que se haya actualizado la colección, ya que hay hueco en la lista
        verify(exactly = 1) {
            mockDocumentReference.update(
                "peliculasFavoritas",
                withArg<List<Map<String, Any>>> { list ->
                    assert(list.size == 20) // Asegura que la lista tiene ahora 20 elementos
                    assert(list.any { it["id"] == "movieId20" }) // Verifica que la nueva película esté en la lista
                }
            )
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test agregar pelicula cuando se ha alcanzado el limite de favoritos`() = runTest {
        // 1. Configurar mocks
        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<FirebaseUser>()
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockDocument = mockk<DocumentSnapshot>()
        val mockUpdateTask = mockk<Task<Void>>(relaxed = true)

        // Configurar comportamiento de autenticación
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "testUserId"
        every { auth.currentUser } returns mockUser

        // Configurar comportamiento del documento
        val currentMoviesList = List(20) { // 20 movies in favorites
            mapOf(
                "id" to "movieId${it}",
                "title" to "Movie $it",
                "posterUrl" to "http://test.com/poster.jpg",
                "esSerie" to false
            )
        }
        every { mockDocument.exists() } returns true
        every { mockDocument.get("peliculasFavoritas") } returns currentMoviesList
        every { mockTask.result } returns mockDocument
        every { mockTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocument)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        every { mockUpdateTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockUpdateTask
        }

        // Configurar Firestore
        val mockDocumentReference = mockk<DocumentReference>()
        every { firestore.collection("usuarios").document("testUserId") } returns mockDocumentReference
        every { mockDocumentReference.get() } returns mockTask
        every { mockDocumentReference.update("peliculasFavoritas", any<List<Map<String, Any>>>()) } returns mockUpdateTask

        // Mockear Toast.makeText() para evitar la llamada real
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast
        every { mockToast.show() } just Runs

        // Llamamos a la función que estamos testeando
        usersViewModel.saveToFavorites(context, "movieId21", "Test Movie", "http://test.com/poster.jpg", false)

        // Esperar a que las operaciones asíncronas se completen
        advanceUntilIdle()

        // Verificamos que el mensaje de límite sea mostrado
        verify(exactly = 1) {
            mockToast.show()
        }

        // Verifica que no se haya actualizado la colección si ya hay 20 películas en favoritos
        verify(exactly = 0) {
            mockDocumentReference.update("peliculasFavoritas", any<List<Map<String, Any>>>())
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test agregar serie cuando hay espacio en favoritos`() = runTest {
        // 1. Configurar mocks
        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<FirebaseUser>()
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
        val mockDocument = mockk<DocumentSnapshot>()
        val mockUpdateTask = mockk<Task<Void>>(relaxed = true)

        // Configurar comportamiento de autenticación
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "testUserId"
        every { auth.currentUser } returns mockUser

        // Configurar comportamiento del documento
        val currentSeriesList = List(19) { // 19 series en favoritos (menos de 20)
            mapOf(
                "id" to "seriesId${it}",
                "title" to "Series ${it}",
                "posterUrl" to "http://test.com/poster.jpg",
                "esSerie" to true
            )
        }
        every { mockDocument.exists() } returns true
        every { mockDocument.get("seriesFavoritas") } returns currentSeriesList
        every { mockTask.result } returns mockDocument
        every { mockTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocument)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        every { mockUpdateTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockUpdateTask
        }

        // Configurar Firestore
        val mockDocumentReference = mockk<DocumentReference>()
        every { firestore.collection("usuarios").document("testUserId") } returns mockDocumentReference
        every { mockDocumentReference.get() } returns mockTask
        every { mockDocumentReference.update("seriesFavoritas", any<List<Map<String, Any>>>()) } returns mockUpdateTask

        // Mockear Toast.makeText() para evitar la llamada real
        mockkStatic(Toast::class)
        val mockToast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast
        every { mockToast.show() } just Runs

        // Llamamos a la función que estamos testeando
        usersViewModel.saveToFavorites(context, "seriesId20", "Test Series", "http://test.com/poster.jpg", true)

        // Esperar a que las operaciones asíncronas se completen
        advanceUntilIdle()

        // Verifica que se haya actualizado la colección, ya que hay hueco en la lista
        verify(exactly = 1) {
            mockDocumentReference.update(
                "seriesFavoritas",
                withArg<List<Map<String, Any>>> { list ->
                    assert(list.size == 20) // Asegura que la lista tiene ahora 20 elementos
                    assert(list.any { it["id"] == "seriesId20" }) // Verifica que la nueva serie esté en la lista
                }
            )
        }
    }


//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `test agregar serie cuando se ha alcanzado el limite de favoritos`() = runTest {
//        // Mock para obtener y verificar la cantidad de elementos en seriesFavoritas
//        val mockAuth = mockk<FirebaseAuth>()
//        val mockUser = mockk<FirebaseUser>()
//        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)
//        val mockDocument = mockk<DocumentSnapshot>()
//        val mockUpdateTask = mockk<Task<Void>>(relaxed = true)
//
//        // Configurar comportamiento de autenticación
//        every { mockAuth.currentUser } returns mockUser
//        every { mockUser.uid } returns "testUserId"
//        every { auth.currentUser } returns mockUser
//
//        // Configurar comportamiento del documento
//        val currentSeriesList = List(20) { // 20 series en favoritos
//            mapOf(
//                "id" to "seriesId$it",
//                "title" to "Series $it",
//                "posterUrl" to "http://test.com/poster.jpg",
//                "esSerie" to true
//            )
//        }
//        every { mockDocument.exists() } returns true
//        every { mockDocument.get("seriesFavoritas") } returns currentSeriesList
//        every { mockTask.result } returns mockDocument
//        every { mockTask.addOnSuccessListener(any()) } answers {
//            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocument)
//            mockTask
//        }
//        every { mockTask.addOnFailureListener(any()) } returns mockTask
//
//        every { mockUpdateTask.addOnSuccessListener(any()) } answers {
//            firstArg<OnSuccessListener<Void>>().onSuccess(null)
//            mockUpdateTask
//        }
//
//        // Configurar Firestore
//        val mockDocumentReference = mockk<DocumentReference>()
//        every { firestore.collection("usuarios").document("testUserId") } returns mockDocumentReference
//        every { mockDocumentReference.get() } returns mockTask
//        every { mockDocumentReference.update("seriesFavoritas", any<List<Map<String, Any>>>()) } returns mockUpdateTask
//
//        // Datos de la serie esperada
//        val expectedSeriesData = mapOf(
//            "id" to "seriesId21",
//            "title" to "Test Series",
//            "posterUrl" to "http://test.com/poster.jpg",
//            "esSerie" to true
//        )
//
//        // Mockear Toast.makeText() para evitar la llamada real
//        mockkStatic(Toast::class)
//        val mockToast = mockk<Toast>(relaxed = true)
//        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns mockToast
//        every { mockToast.show() } just Runs
//
//        // Llamamos a la función que estamos testeando
//        usersViewModel.saveToFavorites(context, "seriesId21", "Test Series", "http://test.com/poster.jpg", true)
//
//        // Esperar a que las operaciones asíncronas se completen
//        advanceUntilIdle()
//
//        // Verificaciones
//        if (currentSeriesList.size < 20) {
//            // Verificamos que se actualice la colección
//            verify { mockDocumentReference.update("seriesFavoritas", listOf(expectedSeriesData)) }
//            verify { mockToast.show() }
//        } else {
//            // Verificamos que el mensaje de límite sea mostrado
//            verify { mockToast.show() }
//        }
//
//        // Verifica que no se haya actualizado la colección si ya están 20 series en favoritos
//        verify(exactly = 0) {
//            mockDocumentReference.update("seriesFavoritas", any<List<Map<String, Any>>>())
//        }
//    }

    @Test
    fun `insertFavorito debe insertar el favorito correctamente en Room`() = runTest {
        // Crear el objeto FavoritoEntity que se va a insertar
        val favorito = FavoritoEntity(idMovieEntity = pelicula1.id, pelicula = pelicula1)

        // Mock de MovieDao
        val movieDao = mockk<MovieDao>(relaxed = true)

        // Configurar el MovieRepository usando el mock de MovieDao
        val movieRepository = MovieRepository(movieDao)

        // Configurar el mock de insertFavorito (asegura que no haga nada en la base de datos)
        coEvery { movieDao.insertFavorito(any()) } just Runs

        // Ejecuta la inserción
        println("Ejecutando insertFavorito en movieRepository con favorito: $favorito")
        movieRepository.insertFavorito(favorito)

        // Verifica que insertFavorito fue llamado con los datos correctos
        println("Verificando si movieDao.insertFavorito fue llamado correctamente...")
        coVerify(exactly = 1) {
            movieDao.insertFavorito(withArg { favoritoInsertado ->
                println("Objeto recibido en insertFavorito: $favoritoInsertado")
                assertEquals(pelicula1.id, favoritoInsertado.idMovieEntity)
                assertEquals(pelicula1, favoritoInsertado.pelicula)
            })
        }
    }

    @Test
    fun `deleteFavorito debe eliminar el favorito correctamente en Room`() = runTest {
        val favorito = FavoritoEntity(idMovieEntity = pelicula2.id, pelicula = pelicula2)

        // Mock de MovieDao
        val movieDao = mockk<MovieDao>(relaxed = true)

        // Configurar el MovieRepository usando el mock de MovieDao
        val movieRepository = MovieRepository(movieDao)

        // Configurar el mock de deleteFavorito
        coEvery { movieDao.deleteFavorito(any()) } just Runs

        // Ejecuta la eliminación
        println("Ejecutando deleteFavorito en movieRepository con favorito: $favorito")
        movieRepository.deleteFavorito(favorito)

        // Verifica que deleteFavorito fue llamado con los datos correctos
        println("Verificando si movieDao.deleteFavorito fue llamado correctamente...")
        coVerify(exactly = 1) {
            movieDao.deleteFavorito(withArg { favoritoEliminado ->
                println("Objeto recibido en deleteFavorito: $favoritoEliminado")
                assertEquals(pelicula2.id, favoritoEliminado.idMovieEntity)
                assertEquals(pelicula2, favoritoEliminado.pelicula)
            })
        }
    }

}
