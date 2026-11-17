package app.documents.core.database.workspace

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TabletWorkspaceCoordinatorTest {

    private lateinit var coordinator: TabletWorkspaceCoordinator
    private lateinit var tempDir: File

    // In-memory fake DAOs for unit testing without full SQLite instrumentation
    class FakeDocumentDao : WorkspaceDocumentDao {
        val docs = mutableMapOf<String, WorkspaceDocumentEntity>()
        override suspend fun getDocument(id: String) = docs[id]
        override suspend fun getDocumentByCanonicalKey(key: String) = docs.values.firstOrNull { it.canonicalSourceKey == key }
        override fun getAllDocumentsFlow(): Flow<List<WorkspaceDocumentEntity>> = flowOf(docs.values.toList())
        override suspend fun insertOrUpdate(doc: WorkspaceDocumentEntity) { docs[doc.documentId] = doc }
        override suspend fun update(doc: WorkspaceDocumentEntity) { docs[doc.documentId] = doc }
        override suspend fun delete(doc: WorkspaceDocumentEntity) { docs.remove(doc.documentId) }
    }

    class FakeSessionDao : WorkspaceSessionDao {
        val sessions = mutableMapOf<String, WorkspaceSessionEntity>()
        override suspend fun getSession(id: String) = sessions[id]
        override fun getAllSessionsFlow(): Flow<List<WorkspaceSessionEntity>> = flowOf(sessions.values.sortedBy { it.tabOrder })
        override suspend fun getAllSessions(): List<WorkspaceSessionEntity> = sessions.values.sortedBy { it.tabOrder }
        override suspend fun getSessionCount() = sessions.size
        override suspend fun insertOrUpdate(session: WorkspaceSessionEntity) { sessions[session.sessionId] = session }
        override suspend fun update(session: WorkspaceSessionEntity) { sessions[session.sessionId] = session }
        override suspend fun delete(session: WorkspaceSessionEntity) { sessions.remove(session.sessionId) }
        override suspend fun deleteById(id: String) { sessions.remove(id) }
    }

    class FakeSnapshotDao : WorkspaceSnapshotDao {
        val snapshots = mutableMapOf<String, WorkspaceSnapshotEntity>()
        override suspend fun getSnapshot(id: String) = snapshots[id]
        override suspend fun getSnapshotsForDocument(documentId: String) = snapshots.values.filter { it.documentId == documentId }
        override suspend fun getSnapshotsByState(state: String) = snapshots.values.filter { it.state == state }
        override suspend fun insertOrUpdate(snapshot: WorkspaceSnapshotEntity) { snapshots[snapshot.snapshotId] = snapshot }
        override suspend fun update(snapshot: WorkspaceSnapshotEntity) { snapshots[snapshot.snapshotId] = snapshot }
        override suspend fun delete(snapshot: WorkspaceSnapshotEntity) { snapshots.remove(snapshot.snapshotId) }
        override suspend fun deleteSuperseded(documentId: String) {
            snapshots.entries.removeIf { it.value.documentId == documentId && it.value.state == "SUPERSEDED" }
        }
    }

    private val fakeDocDao = FakeDocumentDao()
    private val fakeSessionDao = FakeSessionDao()
    private val fakeSnapDao = FakeSnapshotDao()

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "workspace_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        coordinator = TabletWorkspaceCoordinator(fakeDocDao, fakeSessionDao, fakeSnapDao, tempDir)
    }

    @Test
    fun `enforces maximum 6 open sessions`() = runBlocking {
        // Open 6 documents: 3 Sheets, 2 Docs, 1 Presentation (the target mix from ADR-08)
        val testFiles = listOf(
            "Budget.xlsx" to "SHEET",
            "Sales.xlsx" to "SHEET",
            "Forecast.xlsx" to "SHEET",
            "Report.docx" to "WRITE",
            "Notes.docx" to "WRITE",
            "Pitch.pptx" to "PRESENTATION"
        )

        for ((name, kind) in testFiles) {
            val doc = coordinator.getOrCreateDocument(
                canonicalSourceKey = "file://$name",
                originalUri = "content://docs/$name",
                title = name,
                editorKind = kind,
                sourceExtension = name.substringAfterLast('.'),
                sourceMimeType = "application/octet-stream",
                originalWorkingCopyPath = "/data/$name"
            )
            val result = coordinator.openSession(doc, name)
            assertTrue("Expected session to open for $name", result.isSuccess)
        }

        // Attempting to open a 7th session must fail
        val extraDoc = coordinator.getOrCreateDocument(
            canonicalSourceKey = "file://Extra.docx",
            originalUri = "content://docs/Extra.docx",
            title = "Extra.docx",
            editorKind = "WRITE",
            sourceExtension = "docx",
            sourceMimeType = "application/octet-stream",
            originalWorkingCopyPath = "/data/Extra.docx"
        )
        val extraResult = coordinator.openSession(extraDoc, "Extra.docx")
        assertTrue("Opening 7th session must fail", extraResult.isFailure)
        assertEquals(TabletWorkspaceCoordinator.MAX_TABS, fakeSessionDao.getSessionCount())
    }

    @Test
    fun `ensures only ONE session is ACTIVE at a time in sequential mode`() = runBlocking {
        val docA = coordinator.getOrCreateDocument("key://A", "uri://A", "A.docx", "WRITE", "docx", "mime", "/path/A")
        val docB = coordinator.getOrCreateDocument("key://B", "uri://B", "B.xlsx", "SHEET", "xlsx", "mime", "/path/B")

        var checkpointCalledFor = ""
        val sessionA = coordinator.openSession(docA, "A.docx").getOrThrow()
        assertEquals("ACTIVE", fakeSessionDao.getSession(sessionA.sessionId)?.lifecycle)

        val sessionB = coordinator.openSession(docB, "B.xlsx") { activeSessionId ->
            checkpointCalledFor = activeSessionId
        }.getOrThrow()

        // Session A must be suspended, checkpoint callback invoked, Session B must be ACTIVE
        assertEquals(sessionA.sessionId, checkpointCalledFor)
        assertEquals("SUSPENDED", fakeSessionDao.getSession(sessionA.sessionId)?.lifecycle)
        assertEquals("ACTIVE", fakeSessionDao.getSession(sessionB.sessionId)?.lifecycle)
    }

    @Test
    fun `sequential tab switch checkpoints previous and activates target`() = runBlocking {
        val docA = coordinator.getOrCreateDocument("key://A", "uri://A", "A.docx", "WRITE", "docx", "mime", "/path/A")
        val docB = coordinator.getOrCreateDocument("key://B", "uri://B", "B.xlsx", "SHEET", "xlsx", "mime", "/path/B")

        val sessionA = coordinator.openSession(docA, "A.docx").getOrThrow()
        val sessionB = coordinator.openSession(docB, "B.xlsx").getOrThrow()

        var checkpointed = false
        val switchResult = coordinator.switchSession(sessionB.sessionId, sessionA.sessionId) {
            checkpointed = true
        }

        assertTrue(switchResult.isSuccess)
        assertTrue("Expected checkpoint callback on switch", checkpointed)
        assertEquals("ACTIVE", fakeSessionDao.getSession(sessionA.sessionId)?.lifecycle)
        assertEquals("SUSPENDED", fakeSessionDao.getSession(sessionB.sessionId)?.lifecycle)
    }

    @Test
    fun `closing active session activates next session and cleans directory`() = runBlocking {
        val docA = coordinator.getOrCreateDocument("key://A", "uri://A", "A.docx", "WRITE", "docx", "mime", "/path/A")
        val docB = coordinator.getOrCreateDocument("key://B", "uri://B", "B.xlsx", "SHEET", "xlsx", "mime", "/path/B")

        val sessionA = coordinator.openSession(docA, "A.docx").getOrThrow()
        val sessionB = coordinator.openSession(docB, "B.xlsx").getOrThrow()

        val nextActive = coordinator.closeSession(sessionB.sessionId)
        assertEquals(sessionA.sessionId, nextActive)
        assertEquals("ACTIVE", fakeSessionDao.getSession(sessionA.sessionId)?.lifecycle)
        assertNull(fakeSessionDao.getSession(sessionB.sessionId))

        // Session directory must be cleaned
        val sessionBDir = File(tempDir, "editor/${sessionB.sessionId}")
        assertTrue(!sessionBDir.exists())
    }
}
