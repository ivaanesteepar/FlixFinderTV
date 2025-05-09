package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConexionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCapabilities: NetworkCapabilities
    private lateinit var viewModel: ConexionViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        application = mockk()
        connectivityManager = mockk()
        networkCapabilities = mockk()

        every { application.getSystemService(Application.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns mockk()
        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true

        Dispatchers.setMain(testDispatcher)

        viewModel = spyk(ConexionViewModel(application))
        coEvery { viewModel.isInternetAvailable() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `checkConnection deberia actualizar conexionEstablecida cuando se llama`() = runTest {
        // Act
        viewModel.checkConnection()

        // Assert
        assertTrue(viewModel.conexionEstablecida.value)
    }

    @Test
    fun `monitorConnection deberia llamar checkConnection periodicamente`() = runTest {
        // Arrange
        val spyViewModel = spyk(viewModel)
        coEvery { spyViewModel.checkConnection() } returns Unit

        // Act - lanzamos monitorConnection en una corrutina
        val job = launch {
            spyViewModel.monitorConnection()
        }

        // Avanzamos el tiempo para simular el paso del tiempo
        advanceTimeBy(5000) // Primer intervalo
        coVerify(exactly = 1) { spyViewModel.checkConnection() }

        advanceTimeBy(5000) // Segundo intervalo
        coVerify(exactly = 2) { spyViewModel.checkConnection() }

        // Limpieza
        job.cancel()
    }

    @Test
    fun `isInternetAvailable deberia devolver falso cuando la conexion falle`() = runTest {
        // Arrange
        val spyViewModel = spyk(viewModel)
        coEvery { spyViewModel.isInternetAvailable() } returns false

        // Act
        spyViewModel.checkConnection()

        // Assert
        assertFalse(spyViewModel.conexionEstablecida.value)
    }
}


