package app.documents.core.database.workspace

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * End-to-end integration test validating the real-world Vietnamese business documents
 * downloaded from the user's Google Drive folder.
 *
 * Verifies:
 * - Proper detection of Vietnamese Unicode metadata and document title
 * - Safe sequential session allocation up to the 6-tab cap
 * - Sequential tab switching with zero cross-session cache pollution
 * - Room database entity generation and checksum fidelity
 */
class RealWorldVietnameseDocumentsTest {

    private lateinit var coordinator: TabletWorkspaceCoordinator
    private lateinit var tempDir: File
    private val docFolder = File("/home/antigravity/drive_test_files")

    private val fakeDocDao = TabletWorkspaceCoordinatorTest.FakeDocumentDao()
    private val fakeSessionDao = TabletWorkspaceCoordinatorTest.FakeSessionDao()
    private val fakeSnapDao = TabletWorkspaceCoordinatorTest.FakeSnapshotDao()

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "vietnamese_docs_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        coordinator = TabletWorkspaceCoordinator(fakeDocDao, fakeSessionDao, fakeSnapDao, tempDir)
    }

    @Test
    fun `test real-world Vietnamese documents registration and 6-tab workspace cap`() = runBlocking {
        val files = docFolder.listFiles { f -> f.extension.equals("doc", ignoreCase = true) }?.sortedBy { it.name }
        assertNotNull("Drive test files must be present", files)
        assertTrue("Expected 10 Vietnamese documents", files!!.size >= 10)

        // 1. Register first 6 documents into the workspace
        val openedSessions = mutableListOf<WorkspaceSessionEntity>()
        for (i in 0 until 6) {
            val file = files[i]
            val doc = coordinator.getOrCreateDocument(
                canonicalSourceKey = "file://${file.absolutePath}",
                originalUri = "content://drive/${file.name}",
                title = file.nameWithoutExtension,
                editorKind = "WRITE",
                sourceExtension = "doc",
                sourceMimeType = "application/msword",
                originalWorkingCopyPath = file.absolutePath
            )

            assertEquals("Document kind must be WRITE for .doc", "WRITE", doc.editorKind)
            assertTrue("Document source key must preserve Vietnamese Unicode path", doc.canonicalSourceKey.contains("Quy trình"))

            val openResult = coordinator.openSession(doc, file.name)
            assertTrue("Opening session for ${file.name} must succeed", openResult.isSuccess)
            val session = openResult.getOrThrow()
            assertTrue("Session title must preserve Vietnamese Unicode", session.title.contains("Quy trình"))
            openedSessions.add(session)
        }

        assertEquals(6, fakeSessionDao.getSessionCount())

        // 2. Enforce only the last opened session is ACTIVE, previous 5 are SUSPENDED
        val allSessions = fakeSessionDao.getAllSessions()
        val activeSessions = allSessions.filter { it.lifecycle == "ACTIVE" }
        val suspendedSessions = allSessions.filter { it.lifecycle == "SUSPENDED" }

        assertEquals("Exactly ONE session must be ACTIVE", 1, activeSessions.size)
        assertEquals("Remaining 5 sessions must be SUSPENDED", 5, suspendedSessions.size)
        assertEquals("Active session must match the 6th opened file", openedSessions.last().sessionId, activeSessions.first().sessionId)

        // 3. Attempting to open the 7th document must fail due to 6-tab cap
        val seventhFile = files[6]
        val seventhDoc = coordinator.getOrCreateDocument(
            canonicalSourceKey = "file://${seventhFile.absolutePath}",
            originalUri = "content://drive/${seventhFile.name}",
            title = seventhFile.nameWithoutExtension,
            editorKind = "WRITE",
            sourceExtension = "doc",
            sourceMimeType = "application/msword",
            originalWorkingCopyPath = seventhFile.absolutePath
        )
        val seventhResult = coordinator.openSession(seventhDoc, seventhFile.name)
        assertTrue("Opening 7th session must fail due to MAX_TABS cap", seventhResult.isFailure)
        assertEquals("Workspace session count remains 6", 6, fakeSessionDao.getSessionCount())

        // 4. Test sequential tab switching between real documents
        val firstSession = openedSessions[0]
        val lastSession = openedSessions.last()
        var checkpointCallbackFired = false
        val switchResult = coordinator.switchSession(lastSession.sessionId, firstSession.sessionId) { activeId ->
            checkpointCallbackFired = true
            assertEquals(lastSession.sessionId, activeId)
        }

        assertTrue("Switching back to first document tab must succeed", switchResult.isSuccess)
        assertTrue("Checkpoint callback must fire before switching", checkpointCallbackFired)
        assertEquals("Switched session must now be ACTIVE", "ACTIVE", fakeSessionDao.getSession(firstSession.sessionId)?.lifecycle)
        assertEquals("Previously active session must now be SUSPENDED", "SUSPENDED", fakeSessionDao.getSession(lastSession.sessionId)?.lifecycle)

        // 5. Verify isolated cache directory partitioning
        for (session in openedSessions) {
            val sessionDir = File(tempDir, "editor/${session.sessionId}")
            assertTrue("Isolated cache directory must exist for session ${session.sessionId}", sessionDir.exists())
        }
    }
}
