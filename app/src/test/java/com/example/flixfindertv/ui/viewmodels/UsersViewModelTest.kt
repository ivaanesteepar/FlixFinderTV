package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import org.junit.Before
import org.junit.Test
import android.content.res.Resources
import android.os.Looper
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class UsersViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var movieRepository: MovieRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Mock del contexto y la aplicación
        context = mockk()
        application = mockk()
        every { application.applicationContext } returns context

        // Mock de Resources
        val resources = mockk<Resources>()
        every { context.resources } returns resources
        every { resources.getResourcePackageName(any()) } returns "com.example.app"

        // Mock de Firebase
        mockkStatic(FirebaseApp::class)
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)

        val firebaseAppMock = mockk<FirebaseApp>()
        every { FirebaseApp.initializeApp(any()) } returns firebaseAppMock
        every { FirebaseApp.getInstance() } returns firebaseAppMock

        // Mock de FirebaseAuth
        auth = mockk()
        every { FirebaseAuth.getInstance() } returns auth

        // Mock del usuario actual
        val firebaseUserMock = mockk<FirebaseUser>()
        every { auth.currentUser } returns firebaseUserMock
        every { firebaseUserMock.uid } returns "testUserId" // o el uid que necesites

        // Mock de FirebaseFirestore
        firestore = mockk()
        every { FirebaseFirestore.getInstance() } returns firestore

        movieRepository = mockk()

        mockkStatic(Looper::class)
        val looperMock = mockk<Looper>()
        every { Looper.getMainLooper() } returns looperMock
        every { looperMock.thread } returns Thread.currentThread()

        // Inicializamos el ViewModel
        usersViewModel = UsersViewModel(application)
    }

    @org.junit.After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        unmockkStatic(Toast::class)
        unmockkStatic(FirebaseApp::class)
        unmockkStatic(FirebaseAuth::class)
        unmockkStatic(FirebaseFirestore::class)
    }

    @Test
    fun `saveToFavorites cuando el usuario es nulo no debe hacer nada`() {
        // Mock del usuario nulo
        every { auth.currentUser } returns null

        // Crear un objeto Peliculas dummy para pasarle a la función
        val peliculaDummy = Peliculas(
            id = "123",
            title = "Movie Title",
            poster_path = "http://example.com/poster.jpg"
        )

        // Ejecutar la función
        usersViewModel.saveToFavorites(context, peliculaDummy)

        // Verificar que firestore.collection no se llamó
        verify(exactly = 0) { firestore.collection(any()) }
    }


    @Test
    fun test_saveToFavorites_actualizaFavoritos_siUsuarioNoNulo() {
        // Usamos los mocks ya creados en setUp: auth, firestore, etc.

        val favoritesCollection = mockk<DocumentReference>()
        every { firestore.collection("usuarios").document("testUserId") } returns favoritesCollection

        val documentSnapshot = mockk<DocumentSnapshot>()
        val mockTask = mockk<Task<DocumentSnapshot>>()
        every { favoritesCollection.get() } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<DocumentSnapshot>>()
            listener.onSuccess(documentSnapshot)
            mockTask
        }
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.get("peliculasFavoritas") } returns emptyList<Map<String, Any>>()

        val updateTask = mockk<Task<Void>>()
        every { favoritesCollection.update(eq("peliculasFavoritas"), any()) } returns updateTask
        every { updateTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<Void>>()
            listener.onSuccess(null)
            updateTask
        }

        val pelicula = Peliculas(
            id = "123",
            title = "Movie Title",
            poster_path = "http://example.com/poster.jpg",
            esSerie = false
        )

        // Llamamos al método que queremos probar
        usersViewModel.saveToFavorites(application, pelicula)

        // Verificamos que se haya llamado a update con la lista de favoritos actualizada
        verify { favoritesCollection.update(eq("peliculasFavoritas"), any()) }
    }



    @Test
    fun `saveToFavorites cuando se alcanza el máximo de favoritos no debe actualizar los favoritos`() {
        val userId = "testUserId"
        val currentUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns currentUser
        every { currentUser.uid } returns userId

        val favoritesCollection = mockk<DocumentReference>()
        every { firestore.collection("usuarios").document(userId) } returns favoritesCollection

        val documentSnapshot = mockk<DocumentSnapshot>()
        val mockTask = mockk<Task<DocumentSnapshot>>()
        every { favoritesCollection.get() } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<DocumentSnapshot>>()
            listener.onSuccess(documentSnapshot)
            mockTask // Para permitir chaining
        }

        // Documento existe y tiene 20 favoritos (simulamos 20 películas con distintos ids)
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.get("peliculasFavoritas") } returns List(20) { index ->
            mapOf("id" to "id$index")
        }

        // Mockeamos Toast.makeText y show para evitar que falle
        mockkStatic(Toast::class)
        val toast = mockk<Toast>()
        every { Toast.makeText(any<Context>(), any<String>(), any<Int>()) } returns toast
        every { toast.show() } just Runs

        // Creamos un objeto Peliculas para pasar a la función
        val pelicula = Peliculas(
            id = "123",
            title = "Movie Title",
            poster_path = "http://example.com/poster.jpg",
            esSerie = false
        )

        // Ejecutamos la función
        usersViewModel.saveToFavorites(context, pelicula)

        // Verificamos que NO se llamó update para guardar la película
        verify(exactly = 0) { favoritesCollection.update(eq("peliculasFavoritas"), any()) }

        // Verificamos que sí se mostró el Toast
        verify { toast.show() }
    }


}
