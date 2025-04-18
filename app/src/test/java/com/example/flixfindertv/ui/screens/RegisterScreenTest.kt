package com.example.flixfindertv.ui.screens

import com.example.flixfindertv.models.Usuarios
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import io.mockk.every
import kotlinx.coroutines.test.runTest


class RegisterLogicTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setUp() {
        // Mock FirebaseAuth
        mockAuth = mockk()
        mockFirestore = mockk()

        // Simula un usuario
        mockUser = mockk()
        every { mockAuth.currentUser } returns mockUser

        // Simula la creación de un nuevo usuario en Firebase Auth
        val mockAuthResult = mockk<AuthResult>()
        val task: Task<AuthResult> = Tasks.forResult(mockAuthResult)
        every { mockAuth.createUserWithEmailAndPassword(any(), any()) } returns task

        // Simula Firestore para agregar el usuario a la colección de "usuarios"
        every { mockFirestore.collection("usuarios").document(any()).set(any()) } returns mockk()
    }

    @Test
    fun `test user registration success`() = runTest { // Usa runTest para corrutinas
        // Datos de prueba
        val username = "testUser"
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password123"

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertTrue(isValid)

        if (isValid) {
            // Simula la creación del usuario
            val newUser = Usuarios(
                uid = "uid123",
                nombre = username,
                email = email,
                fotoPerfil = "",
                peliculasFavoritas = emptyList(),
                seriesFavoritas = emptyList(),
                seguidores = emptyList(),
                siguiendo = emptyList(),
                numComentarios = 0,
                admin = false
            )

            // Simula el Task de Firebase Auth
            val task: Task<AuthResult> = mockk()
            val authResult: AuthResult = mockk()

            // Configura los mocks
            every { task.isSuccessful } returns true
            every { task.result } returns authResult
            every { authResult.user } returns mockk {
                every { uid } returns "uid123"
            }

            // Configura el listener de éxito
            every { task.addOnSuccessListener(any()) } answers {
                val listener = firstArg<OnSuccessListener<AuthResult>>()
                listener.onSuccess(authResult)
                task
            }

            // Configura el mock de Auth
            every { mockAuth.createUserWithEmailAndPassword(email, password) } returns task

            // Mockea Firestore
            val documentReference: DocumentReference = mockk(relaxed = true) // Usa relaxed para simplificar
            every { mockFirestore.collection("usuarios").document("uid123") } returns documentReference

            // Configura el set() para capturar cualquier argumento
            val slot = slot<Usuarios>()
            every { documentReference.set(capture(slot)) } returns mockk {
                every { addOnSuccessListener(any()) } returns this
            }

            // Ejecuta el registro
            mockAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    mockFirestore.collection("usuarios").document("uid123").set(newUser)
                }

            // Espera un poco para que se complete la operación asíncrona
            //advanceUntilIdle() // Avanza el tiempo en las corrutinas de prueba

            // Verificaciones
            verify { mockAuth.createUserWithEmailAndPassword(email, password) }
            verify { documentReference.set(any()) }

            // Verifica los datos del usuario si es necesario
            assertEquals(newUser, slot.captured)
        }
    }


    @Test
    fun `test registration error when passwords do not match`() {
        // Datos de prueba
        val username = "testUser"
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "differentPassword"

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertFalse(isValid) // Las contraseñas no coinciden, debe fallar

        // Verifica que no se intente registrar un usuario con contraseñas no coincidentes
        verify(exactly = 0) { mockAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test all fields are required`() {
        // Datos de prueba
        val username = ""
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password123"

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertFalse(isValid)  // Como el campo de 'username' está vacío, debe fallar

        // Verifica que no se intente registrar un usuario si los campos no son válidos
        verify(exactly = 0) { mockAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test all fields are required with empty email`() {
        // Datos de prueba
        val username = "testUser"
        val email = ""
        val password = "password123"
        val confirmPassword = "password123"

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertFalse(isValid)  // Como el campo de 'email' está vacío, debe fallar

        // Verifica que no se intente registrar un usuario si los campos no son válidos
        verify(exactly = 0) { mockAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test all fields are required with empty password`() {
        // Datos de prueba
        val username = "testUser"
        val email = "test@example.com"
        val password = ""
        val confirmPassword = "password123"

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertFalse(isValid)  // Como el campo de 'password' está vacío, debe fallar

        // Verifica que no se intente registrar un usuario si los campos no son válidos
        verify(exactly = 0) { mockAuth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `test all fields are required with empty confirmPassword`() {
        // Datos de prueba
        val username = "testUser"
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = ""

        // Lógica de validación
        val isValid = username.isNotEmpty() && email.isNotEmpty() && password == confirmPassword
        assertFalse(isValid)  // Como el campo de 'confirmPassword' está vacío, debe fallar

        // Verifica que no se intente registrar un usuario si los campos no son válidos
        verify(exactly = 0) { mockAuth.createUserWithEmailAndPassword(any(), any()) }
    }

}
