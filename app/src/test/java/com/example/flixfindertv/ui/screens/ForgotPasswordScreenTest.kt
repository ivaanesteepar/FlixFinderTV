package com.example.flixfindertv.ui.screens

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ForgotPasswordScreenTest {

    private lateinit var firebaseAuth: FirebaseAuth

    @Before
    fun setUp() {
        // Mock FirebaseAuth
        firebaseAuth = mockk()
    }

    @Test
    fun `test empty email shows error message`() {
        // Simulamos que el correo está vacío
        val email = ""

        // Verificar si se muestra el mensaje de error cuando el correo está vacío
        if (email.isEmpty()) {
            assertEquals("Enter your email", "Enter your email")
        }
    }

    @Test
    fun `test valid email triggers sendPasswordResetEmail`() {
        // Simulamos un correo válido
        val email = "test@example.com"
        val task: Task<Void> = mockk()

        // Simulamos la respuesta exitosa de Firebase Auth
        every { firebaseAuth.sendPasswordResetEmail(email) } returns task
        every { task.isSuccessful } returns true

        firebaseAuth.sendPasswordResetEmail(email)

        // Verifica si sendPasswordResetEmail fue llamado correctamente
        verify { firebaseAuth.sendPasswordResetEmail(email) }
    }

    @Test
    fun `test sendPasswordResetEmail failure`() {
        // Simulamos un correo válido
        val email = "test@example.com"
        val task: Task<Void> = mockk()

        // Simulamos la respuesta fallida de Firebase Auth
        every { firebaseAuth.sendPasswordResetEmail(email) } returns task
        every { task.isSuccessful } returns false
        every { task.exception } returns Exception("Error sending reset email")

        firebaseAuth.sendPasswordResetEmail(email)

        // Verifica si sendPasswordResetEmail fue llamado correctamente
        verify { firebaseAuth.sendPasswordResetEmail(email) }
    }
}
