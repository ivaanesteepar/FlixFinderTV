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
        // Configura el mock para devolver usuario null
        every { auth.currentUser } returns null

        usersViewModel.saveToFavorites(context, "123", "Movie Title", "http://example.com/poster.jpg", false)

        // Verifica que no se llamó a Firestore
        verify(exactly = 0) { firestore.collection(any<String>()) }
    }

    @Test
    fun `saveToFavorites cuando el usuario no es nulo debe actualizar los favoritos`() {
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
            mockk() // Regresamos el mockTask
        }

        // Simulamos que el documento existe y no tiene favoritos aún
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.get("peliculasFavoritas") } returns listOf<Map<String, Any>>()

        val updateTask = mockk<Task<Void>>()
        every { favoritesCollection.update(eq("peliculasFavoritas"), any()) } returns updateTask
        every { updateTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<Void>>()
            listener.onSuccess(null)
            updateTask
        }

        usersViewModel.saveToFavorites(context, "123", "Movie Title", "http://example.com/poster.jpg", false)

        // Verificación de que se ha llamado a update en Firestore
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
            mockk() // Regresamos el mockTask
        }

        // Simulamos que el documento existe y tiene 20 favoritos
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.get("peliculasFavoritas") } returns List(20) {
            mapOf("id" to "id$it")
        }

        // Simulamos el comportamiento de Toast.makeText() y mockeamos el show()
        mockkStatic(Toast::class)
        val toast = mockk<Toast>()
        every { Toast.makeText(any<Context>(), any<String>(), any<Int>()) } returns toast
        every { toast.show() } returns Unit // Mockeamos show() para que no haga nada

        // Intentamos guardar una película cuando ya se alcanzó el máximo de 20
        usersViewModel.saveToFavorites(context, "123", "Movie Title", "http://example.com/poster.jpg", false)

        // Verificación de que no se realice la operación de update (guardar la película)
        verify(exactly = 0) { favoritesCollection.update(eq("peliculasFavoritas"), any()) }

        // Verificación de que se llamó a show() en el Toast
        verify { toast.show() }
    }


}
