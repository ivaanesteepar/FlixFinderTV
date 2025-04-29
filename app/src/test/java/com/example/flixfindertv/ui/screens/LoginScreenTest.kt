package com.example.flixfindertv.ui.screens

import android.content.Context
import com.example.flixfindertv.ui.viewmodels.UsersViewModelTest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*

@ExperimentalCoroutinesApi
class LoginScreenTest {

    private lateinit var usersViewModel: UsersViewModelTest
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var context: Context

    private val validEmail = "test@example.com"
    private val validPassword = "password123"

    @Before
    fun setup() {
        // Usando MockK para crear los mocks
        firebaseAuth = mockk()
        firebaseFirestore = mockk()
        usersViewModel = mockk()
        context = mockk()
    }

    @Test
    fun `test iniciar sesion con credenciales validas deberia retornar AuthResult`() = runTest {
        val authResult = mockk<AuthResult>()
        val task: Task<AuthResult> = Tasks.forResult(authResult)

        // Mocking Firebase Authentication usando MockK
        every { firebaseAuth.signInWithEmailAndPassword(validEmail, validPassword) } returns task

        // Simula el login
        val result = firebaseAuth.signInWithEmailAndPassword(validEmail, validPassword).await()

        // Verifica el resultado de la autenticación
        assertEquals(authResult, result)

        // Verifica que la funcion fue llamada
        verify { firebaseAuth.signInWithEmailAndPassword(validEmail, validPassword) }
    }

    @Test
    fun `test iniciar sesion con credenciales invalidas deberia lanzar excepcion`() = runTest {
        val invalidEmail = "invalid@example.com"
        val invalidPassword = "wrongpassword"

        // Mocking Firebase Authentication para lanzar una excepción
        every { firebaseAuth.signInWithEmailAndPassword(invalidEmail, invalidPassword) } throws Exception("Authentication failed")

        // Simula el login y captura la excepción
        try {
            firebaseAuth.signInWithEmailAndPassword(invalidEmail, invalidPassword).await()
        } catch (e: Exception) {
            // Verifica el mensaje de la excepción
            assertEquals("Authentication failed", e.message)
        }

        // Verifica que el método fue llamado
        verify { firebaseAuth.signInWithEmailAndPassword(invalidEmail, invalidPassword) }
    }

    @Test
    fun `test campos vacios deberian retornar mensaje de error`() {
        val emptyEmail = ""
        val emptyPassword = ""

        // Simulamos cómo el ViewModel maneja la validación de campos vacíos
        var errorMessage = ""
        if (emptyEmail.isEmpty() || emptyPassword.isEmpty()) {
            errorMessage = "All fields are required"
        }

        // Verifica que los campos vacíos devuelvan el mensaje de error adecuado
        assertEquals("All fields are required", errorMessage)
    }

    @Test
    fun `test todos los campos son requeridos para iniciar sesion`() {
        val emptyEmail = ""
        val emptyPassword = ""

        // Lógica de validación
        var errorMessage = ""
        if (emptyEmail.isEmpty() || emptyPassword.isEmpty()) {
            errorMessage = "All fields are required"
        }

        // Verifica que el mensaje de error sea el adecuado cuando los campos están vacíos
        assertEquals("All fields are required", errorMessage)

        // Verifica que no se intente iniciar sesión si los campos están vacíos
        verify(exactly = 0) { firebaseAuth.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test correo vacio deberia retornar error`() {
        val emptyEmail = ""
        val password = "password123"

        // Lógica de validación
        var errorMessage = ""
        if (emptyEmail.isEmpty()) {
            errorMessage = "Email is required"
        }

        // Verifica que el mensaje de error sea el adecuado cuando el campo de email está vacío
        assertEquals("Email is required", errorMessage)

        // Verifica que no se intente iniciar sesión si el email está vacío
        verify(exactly = 0) { firebaseAuth.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test contrasena vacia deberia retornar error`() {
        val email = "test@example.com"
        val emptyPassword = ""

        // Lógica de validación
        var errorMessage = ""
        if (emptyPassword.isEmpty()) {
            errorMessage = "Password is required"
        }

        // Verifica que el mensaje de error sea el adecuado cuando el campo de password está vacío
        assertEquals("Password is required", errorMessage)

        // Verifica que no se intente iniciar sesión si la contraseña está vacía
        verify(exactly = 0) { firebaseAuth.signInWithEmailAndPassword(any(), any()) }
    }
}
