package com.example.flixfindertv.ui.viewmodels

import com.example.flixfindertv.models.Comentarios
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CommentsViewModelTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var viewModel: CommentsViewModel

    @Before
    fun setUp() {
        // Configura todos los mocks estáticos primero
        mockkStatic("com.google.firebase.firestore.FirebaseFirestore")
        mockkStatic("android.os.Process")
        mockkStatic("com.google.android.gms.common.util.ProcessUtils")

        // Configura los mocks para Firebase
        firestore = mockk(relaxed = true)
        collectionReference = mockk(relaxed = true)
        documentReference = mockk(relaxed = true)

        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("comentarios") } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference

        // Configura los mocks para Process y ProcessUtils
        every { android.os.Process.myPid() } returns 1234
        every { com.google.android.gms.common.util.ProcessUtils.getMyProcessName() } returns "testProcess"

        // Inicializa el ViewModel después de configurar todos los mocks
        viewModel = CommentsViewModel()
    }

    @Test
    fun `sendComment should successfully add a comment`() = runTest {
        // 1. Configura jerarquía de Firestore
        val comentariosCollection = mockk<CollectionReference>(relaxed = true)
        val contenidoDocument = mockk<DocumentReference>(relaxed = true)
        val subComentariosCollection = mockk<CollectionReference>(relaxed = true)
        val comentarioDocument = mockk<DocumentReference>(relaxed = true)

        every { firestore.collection("comentarios") } returns comentariosCollection
        every { comentariosCollection.document(any()) } returns contenidoDocument
        every { contenidoDocument.collection("comentarios") } returns subComentariosCollection
        every { subComentariosCollection.document(any()) } returns comentarioDocument

        // 2. Configura Task de Firestore
        val task = mockk<Task<Void>>(relaxed = true)
        every { comentarioDocument.set(any<Comentarios>()) } returns task
        every { task.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null) // Simula la ejecución exitosa
            task
        }

        // 3. Define un valor fijo para el Timestamp
        val fixedTimestamp = Timestamp(1745054651, 846000000) // Valor fijo de Timestamp

        // 4. Crea el comentario esperado con un Timestamp fijo
        val expectedComment = Comentarios(
            id = "5b5491c0-d9fa-466d-82c1-b18350274b61",
            usuario = "user1",
            puntuacion = 5,
            comentario = "Great movie",
            respuestas = emptyList(),
            idContenido = "content1",
            fechaPublicacion = fixedTimestamp, // Utiliza el valor fijo
            likes = 0,
            nombreLikes = emptyList(),
            revision = true
        )

        // 5. Mockea el Timestamp para asegurarte de que siempre sea el mismo valor
        mockkObject(Timestamp)
        every { Timestamp.now() } returns fixedTimestamp

        // Ejecuta la función
        viewModel.sendComment("content1", "user1", 5, "Great movie", true)

        // 6. Verificaciones
        verify {
            // Imprime el Timestamp esperado y el Timestamp real antes de la comparación
            println("Timestamp esperado: ${expectedComment.fechaPublicacion}")

            // Verifica la comparación de los Timestamps
            comentarioDocument.set(match { comment: Comentarios ->
                comment.usuario == expectedComment.usuario && comment.puntuacion == expectedComment.puntuacion && comment.comentario == expectedComment.comentario && comment.idContenido == expectedComment.idContenido && comment.revision == expectedComment.revision && comment.fechaPublicacion.seconds == expectedComment.fechaPublicacion.seconds && comment.fechaPublicacion.nanoseconds == expectedComment.fechaPublicacion.nanoseconds
            })
        }

        // Limpiar el mock
        unmockkObject(Timestamp)
    }

    @Test
    fun `sendComment should handle Firestore operations`() = runTest {
        // 1. Configura jerarquía de Firestore
        val comentariosCollection = mockk<CollectionReference>()
        val contenidoDocument = mockk<DocumentReference>()
        val subComentariosCollection = mockk<CollectionReference>()
        val comentarioDocument = mockk<DocumentReference>()

        every { firestore.collection("comentarios") } returns comentariosCollection
        every { comentariosCollection.document(any()) } returns contenidoDocument
        every { contenidoDocument.collection("comentarios") } returns subComentariosCollection
        every { subComentariosCollection.document(any()) } returns comentarioDocument

        // 2. Mock para .get()
        val mockTask = mockk<Task<QuerySnapshot>>(relaxed = true)
        val mockSnapshot = mockk<QuerySnapshot>(relaxed = true)

        every { mockTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockSnapshot)
            mockTask
        }

        every { mockTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(Exception("Error al obtener la colección"))
            mockTask
        }

        every { subComentariosCollection.get() } returns mockTask

        // 3. Mock Timestamp
        val fixedTimestamp = Timestamp(1745054651, 846000000)
        mockkObject(Timestamp)
        every { Timestamp.now() } returns fixedTimestamp

        // 4. Mock para .set()
        val mockSetTask = mockk<Task<Void>>(relaxed = true)

        every { comentarioDocument.set(any()) } returns mockSetTask

        every { mockSetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockSetTask
        }

        every { mockSetTask.addOnFailureListener(any()) } returns mockSetTask

        // 5. Ejecuta la función (caso éxito)
        viewModel.sendComment("content1", "user1", 5, "Great movie", true)

        // 6. Verifica que se configuraron ambos listeners
        verify {
            mockTask.addOnSuccessListener(any())
            mockTask.addOnFailureListener(any())
            mockSetTask.addOnSuccessListener(any())
            mockSetTask.addOnFailureListener(any())
        }

        // 7. Verifica que se llamó a set() con un comentario válido
        verify {
            comentarioDocument.set(match {
                val comentario = it as Comentarios
                comentario.idContenido == "content1" &&
                        comentario.usuario == "user1" &&
                        comentario.puntuacion == 5 &&
                        comentario.comentario == "Great movie" &&
                        comentario.fechaPublicacion == fixedTimestamp &&
                        comentario.revision
            })
        }

        // 8. Simula fallo: onSuccess no es llamado
        every { mockSetTask.addOnSuccessListener(any()) } answers {
            mockSetTask // no ejecuta onSuccess
        }

        // 10. Limpieza
        unmockkObject(Timestamp)
    }

}
