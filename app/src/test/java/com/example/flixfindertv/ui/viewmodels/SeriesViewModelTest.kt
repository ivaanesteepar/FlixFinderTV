//package com.example.flixfindertv.ui.viewmodels
//
//import android.util.Log
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.example.flixfindertv.models.Peliculas
//import com.google.android.gms.tasks.Tasks
//import com.google.firebase.FirebaseApp
//import io.mockk.*
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.QuerySnapshot
//import com.google.firebase.firestore.DocumentSnapshot
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.TestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.setMain
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class SeriesViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var firestore: FirebaseFirestore
//    private lateinit var firebaseApp: FirebaseApp
//    private lateinit var viewModel: SeriesViewModel
//    private lateinit var testDispatcher: TestDispatcher
//
//    private val mockIdGeneroAnimacion = 1L // ID de ejemplo para el género Animación
//
//    @Before
//    fun setUp() {
//        // Configurar Dispatcher de pruebas
//        testDispatcher = StandardTestDispatcher()
//        Dispatchers.setMain(testDispatcher)
//
//        // Configuración inicial de MockK
//        MockKAnnotations.init(this)
//
//        // Mockear FirebaseApp
//        firebaseApp = mockk(relaxed = true)
//        mockkStatic(FirebaseApp::class)
//        every { FirebaseApp.initializeApp(any()) } returns firebaseApp
//        every { FirebaseApp.getInstance() } returns firebaseApp
//
//        // Mockear Firestore
//        firestore = mockk(relaxed = true)
//        mockkStatic(FirebaseFirestore::class)
//        every { FirebaseFirestore.getInstance() } returns firestore
//
//        // Inicializar el ViewModel
//        viewModel = SeriesViewModel()
//    }
//
//    @Test
//    fun `test obtenerSeriesAnimacion exito`() = runTest {
//        mockkStatic(Log::class)
//        every { Log.e(any(), any()) } returns 0
//        every { Log.e(any(), any(), any()) } returns 0
//
//        // Mock de las series de animación
//        val mockSeries = listOf(mockSerie("Serie Animada 1", "id1"))
//
//        // Mock del género (Animación)
//        every {
//            firestore.collection("generos").whereEqualTo("name", "Animation").get()
//        } returns Tasks.forResult(mockGenerosQuerySnapshot("Animation", 1L))
//
//        // Mock de las series
//        every {
//            firestore.collection("series").whereArrayContains("genre_ids", 1L).limit(20).get()
//        } returns Tasks.forResult(mockPeliculasQuerySnapshot(mockSeries))
//
//        // Ejecutar la función
//        viewModel.obtenerSeriesAnimacion()
//        advanceUntilIdle()  // Esto asegura que los resultados estén completamente cargados
//
//        // Imprimir el valor esperado y el obtenido
//        println("Valor esperado: $mockSeries")
//        println("Valor obtenido: ${viewModel.listaSeriesAnimacion.value}")
//
//        // Verificar que las series sean las mismas que las mockeadas
//        assertEquals(mockSeries, viewModel.listaSeriesAnimacion.value)
//
//        // Verificar que la carga haya terminado
//        assertFalse(viewModel.isLoadingAnimation.value == true)
//    }
//
//    @Test
//    fun `test obtenerSeriesAnimacion cuando el resultado esta vacio`() = runTest {
//        // Simular la consulta para obtener el ID del género "Animación"
//        val mockGeneroQuerySnapshot = mockk<QuerySnapshot>()
//        val mockGeneroDocument = mockk<DocumentSnapshot>()
//        every { mockGeneroDocument.getLong("id") } returns mockIdGeneroAnimacion
//        every { mockGeneroQuerySnapshot.documents } returns listOf(mockGeneroDocument)
//        coEvery { firestore.collection("generos").whereEqualTo("name", "Animation").get().await() } returns mockGeneroQuerySnapshot
//
//        // Simular una respuesta vacía de Firestore para las series
//        val mockSeriesQuerySnapshot = mockk<QuerySnapshot>()
//        every { mockSeriesQuerySnapshot.documents } returns emptyList()
//        coEvery { firestore.collection("series").whereArrayContains("genre_ids", mockIdGeneroAnimacion).limit(20).get().await() } returns mockSeriesQuerySnapshot
//
//        viewModel.obtenerSeriesAnimacion()
//
//        // Verificar que la lista de series está vacía
//        assertTrue(viewModel.listaSeriesAnimacion.value.isNullOrEmpty())
//    }
//
//    @Test
//    fun `test obtenerSeriesAnimacion cuando ocurre un error`() = runTest {
//        // Simular un error en la consulta para obtener el género "Animación"
//        coEvery { firestore.collection("generos").whereEqualTo("name", "Animation").get().await() } throws RuntimeException("Error al obtener el género")
//
//        try {
//            viewModel.obtenerSeriesAnimacion()
//        } catch (e: Exception) {
//            assertEquals("Error al obtener el género", e.message)
//        }
//    }
//
//    private fun mockPeliculasQuerySnapshot(mockSeries: List<Peliculas>): QuerySnapshot {
//        val querySnapshot = mockk<QuerySnapshot>()
//        val docs = mockSeries.map { mockPeliculaDocument(it) }
//        every { querySnapshot.documents } returns docs
//        return querySnapshot
//    }
//
//    private fun mockPeliculaDocument(serie: Peliculas): DocumentSnapshot {
//        val doc = mockk<DocumentSnapshot>()
//        every { doc.toObject(Peliculas::class.java) } returns serie
//        return doc
//    }
//
//    private fun mockGenerosQuerySnapshot(name: String, id: Long): QuerySnapshot {
//        val doc = mockk<DocumentSnapshot>()
//        (every { doc.getLong("id") } returns id)
//            ?: 0L // Si id es null, retornar un valor por defecto (0L)
//        every { doc.getString("name") } returns name
//        val querySnapshot = mockk<QuerySnapshot>()
//        every { querySnapshot.documents } returns listOf(doc)
//        return querySnapshot
//    }
//
//    private fun mockSerie(name: String, id: String): Peliculas {
//        return Peliculas(
//            id = id,
//            title = "",
//            name = name,  // Aquí se asigna un valor para 'name'
//            overview = "A great serie",
//            release_date = "2025-04-18",
//            release_date_series = "2025-04-18",
//            poster_path = "/poster_path.jpg",
//            vote_average = "8.5",
//            vote_count = "1500",
//            genre_ids = listOf(1),  // Asegúrate de que el ID de género esté presente
//            adult = false,
//            backdrop_path = "/backdrop_path.jpg",
//            popularity = 95.5,
//            esSerie = false,
//            comentarios = emptyList(),
//            original_language = "en",
//            status = "Released",
//            trailer = "https://trailer.url",
//            director_name = "John Doe",
//            director_photo_url = "https://directorphoto.url"
//        )
//    }
//}
