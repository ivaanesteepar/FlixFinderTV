package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.entities.PeliculasEntity
import com.example.flixfindertv.room.repository.MovieRepository
import com.google.android.gms.common.util.ProcessUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.fail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MoviesViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseApp: FirebaseApp
    private lateinit var viewModel: MoviesViewModel
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var repository: MovieRepository


    @Before
    fun setUp() {
        // Configurar Dispatcher de pruebas
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        // Configuración inicial de MockK
        MockKAnnotations.init(this)

        // Mockear FirebaseApp
        firebaseApp = mockk(relaxed = true)
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.initializeApp(any()) } returns firebaseApp
        every { FirebaseApp.getInstance() } returns firebaseApp

        // Mockear Process y ProcessUtils
        mockkStatic("android.os.Process")
        mockkStatic("com.google.android.gms.common.util.ProcessUtils")
        every { android.os.Process.myPid() } returns 1234
        every { ProcessUtils.getMyProcessName() } returns "testProcess"

        // Mockear FirebaseFirestore
        firestore = mockk(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore

        // Inicializar el ViewModel (sin modificar su constructor)
        viewModel = MoviesViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test obtenerPeliculasFamily exito`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock de la película
        val mockPeliculas = listOf(mockPelicula("Pelicula 1", "id1"))

        // Mock del género
        every {
            firestore.collection("generos").whereEqualTo("name", "Family").get()
        } returns Tasks.forResult(mockGenerosQuerySnapshot("Family", 1L))

        // Mock de las películas
        every {
            firestore.collection("peliculas").whereArrayContains("genre_ids", 1L).limit(20).get()
        } returns Tasks.forResult(mockPeliculasQuerySnapshot(mockPeliculas))

        // Ejecutar la función
        viewModel.obtenerPeliculasFamily()
        advanceUntilIdle()  // Esto asegura que los resultados estén completamente cargados

        // Imprimir el valor esperado y el obtenido
        println("Valor esperado: $mockPeliculas")
        println("Valor obtenido: ${viewModel.listaPeliculasFamily.value}")

        // Verificar que las películas sean las mismas que las mockeadas
        assertEquals(mockPeliculas, viewModel.listaPeliculasFamily.value)

        // Verificar que la carga haya terminado
        assertFalse(viewModel.isLoadingFamily.value == true)
    }

    @Test
    fun `test obtenerPeliculasFamily con error en Firestore`() = runTest {
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Simular error en la consulta de géneros
        every {
            firestore.collection("generos").whereEqualTo("name", "Family").get()
        } returns Tasks.forException(Exception("Firestore error"))

        // Ejecutar la función
        viewModel.obtenerPeliculasFamily()
        advanceUntilIdle()

        // Verificar que la lista de películas sigue vacía
        assertEquals(emptyList<Peliculas>(), viewModel.listaPeliculasFamily.value)

        // Verificar que la carga haya terminado
        assertFalse(viewModel.isLoadingFamily.value == true)
    }

    @Test
    fun `test insertPeliculasPopulares inserta correctamente en Room`() = runTest {
        // Crear un mock de Application, ya que OfflineViewModel lo necesita
        val mockApplication = mockk<Application>(relaxed = true)

        // Mockear dependencias
        val repository = mockk<MovieRepository>(relaxed = true)
        val genresViewModel = mockk<GenresViewModel>(relaxed = true)

        // Crear un mock de OfflineViewModel usando un constructor o método adecuado
        val viewModel = spyk(OfflineViewModel(mockApplication))

        // Usar un spyk para interceptar llamadas y así poder pasar mocks específicos cuando sea necesario
        every { viewModel.repository } returns repository
        every { viewModel.genresViewModel } returns genresViewModel

        // Crear lista de películas de prueba
        val peliculasList = listOf(
            mockPelicula("Pelicula 1", "id1"),
            mockPelicula("Pelicula 2", "id2")
        )

        println("Películas de prueba: $peliculasList") // Imprime las películas de prueba

        // Mockear el comportamiento de createPeliculaEntityWithGeneros
        peliculasList.forEach { pelicula ->
            every { genresViewModel.createPeliculaEntityWithGeneros(eq(pelicula), any()) } answers {
                val callback = arg<(PeliculasEntity) -> Unit>(1)
                val mockEntity = mockk<PeliculasEntity>(relaxed = true)
                println("Creando PeliculasEntity para: ${pelicula.title}") // Imprime cuando se está creando una entidad
                callback(mockEntity) // Devuelve un PeliculasEntity simulado
            }
        }

        // Llamar a la función insertPeliculasPopulares
        println("Llamando a insertPeliculasPopulares con las películas de prueba...")
        viewModel.insertPeliculasPopulares(peliculasList)

        // Avanzar las corutinas para que las operaciones asíncronas se completen
        advanceUntilIdle()

        // Capturar la lista de películas que se pasó al repository
        val captor = slot<List<PeliculasEntity>>()

        // Verificar que se llamó al método insertMoviesPopulares con las entidades correctas
        coVerify(exactly = 1) { repository.insertMoviesPopulares(capture(captor)) }

        // Imprimir lo que se pasó al repository
        println("Películas pasadas al repository: ${captor.captured}") // Imprime las películas capturadas

        // Verificar que el tamaño de la lista es correcto (en este caso, 2)
        assert(captor.captured.size == peliculasList.size)

        // Verificar el contenido de las películas insertadas
        captor.captured.forEachIndexed { index, peliculaEntity ->
            println("Pelicula ${index + 1}: $peliculaEntity") // Imprime cada PeliculasEntity insertada
        }
    }


    // Funciones auxiliares
    private fun mockGenerosQuerySnapshot(name: String, id: Long): QuerySnapshot {
        val doc = mockk<DocumentSnapshot>()
        every { doc.getLong("id") } returns id
        every { doc.getString("name") } returns name
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(doc)
        return querySnapshot
    }

    private fun mockPeliculasQuerySnapshot(mockPeliculas: List<Peliculas>): QuerySnapshot {
        val querySnapshot = mockk<QuerySnapshot>()
        val docs = mockPeliculas.map { mockPeliculaDocument(it) }
        every { querySnapshot.documents } returns docs
        return querySnapshot
    }

    private fun mockPeliculaDocument(pelicula: Peliculas): DocumentSnapshot {
        val doc = mockk<DocumentSnapshot>()
        every { doc.toObject(Peliculas::class.java) } returns pelicula
        return doc
    }

    private fun mockPelicula(titulo: String, id: String): Peliculas {
        return Peliculas(
            id = id,
            title = titulo,
            name = "Sample Movie",  // Aquí se asigna un valor para 'name'
            overview = "A great movie",
            release_date = "2025-04-18",
            release_date_series = "2025-04-18",
            poster_path = "/poster_path.jpg",
            vote_average = "8.5",
            vote_count = "1500",
            genre_ids = listOf(1),  // Asegúrate de que el ID de género esté presente
            adult = false,
            backdrop_path = "/backdrop_path.jpg",
            popularity = 95.5,
            esSerie = false,
            comentarios = emptyList(),
            original_language = "en",
            status = "Released",
            trailer = "https://trailer.url",
            director_name = "John Doe",
            director_photo_url = "https://directorphoto.url"
        )
    }
}
