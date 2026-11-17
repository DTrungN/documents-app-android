package app.documents.core.database.workspace

import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class TabletWorkspaceCoordinator(
    private val documentDao: WorkspaceDocumentDao,
    private val sessionDao: WorkspaceSessionDao,
    private val snapshotDao: WorkspaceSnapshotDao,
    private val baseCacheDir: File
) {

    constructor(database: WorkspaceDatabase, baseCacheDir: File) : this(
        database.documentDao(),
        database.sessionDao(),
        database.snapshotDao(),
        baseCacheDir
    )

    companion object {
        const val MAX_TABS = 6
    }

    fun observeSessions(): Flow<List<WorkspaceSessionEntity>> {
        return sessionDao.getAllSessionsFlow()
    }

    suspend fun getActiveSession(): WorkspaceSessionEntity? {
        return sessionDao.getAllSessions().firstOrNull { it.lifecycle == "ACTIVE" }
    }

    /**
     * Registers a document or retrieves its existing record by canonical key to prevent duplicate writers.
     */
    suspend fun getOrCreateDocument(
        canonicalSourceKey: String,
        originalUri: String,
        title: String,
        editorKind: String,
        sourceExtension: String,
        sourceMimeType: String,
        originalWorkingCopyPath: String
    ): WorkspaceDocumentEntity {
        val existing = documentDao.getDocumentByCanonicalKey(canonicalSourceKey)
        if (existing != null) {
            return existing
        }

        val docId = UUID.randomUUID().toString()
        val newDoc = WorkspaceDocumentEntity(
            documentId = docId,
            canonicalSourceKey = canonicalSourceKey,
            originalUri = originalUri,
            persistedPermission = true,
            editorKind = editorKind,
            sourceExtension = sourceExtension,
            sourceMimeType = sourceMimeType,
            effectiveWorkingFormat = sourceExtension.lowercase(),
            originalWorkingCopyPath = originalWorkingCopyPath
        )
        documentDao.insertOrUpdate(newDoc)
        return newDoc
    }

    /**
     * Opens a new session tab. Enforces MAX_TABS (6).
     * In SEQUENTIAL_ONLY mode, previously active session is suspended before the new session becomes ACTIVE.
     */
    suspend fun openSession(
        document: WorkspaceDocumentEntity,
        title: String,
        checkpointActiveSession: suspend (activeSessionId: String) -> Unit = {}
    ): Result<WorkspaceSessionEntity> {
        val currentSessions = sessionDao.getAllSessions()
        if (currentSessions.size >= MAX_TABS) {
            return Result.failure(IllegalStateException("Maximum of $MAX_TABS workspace tabs reached."))
        }

        // Suspend currently active session if one exists
        val currentActive = currentSessions.firstOrNull { it.lifecycle == "ACTIVE" }
        if (currentActive != null) {
            checkpointActiveSession(currentActive.sessionId)
            sessionDao.update(currentActive.copy(lifecycle = "SUSPENDED"))
        }

        val sessionId = UUID.randomUUID().toString()
        val session = WorkspaceSessionEntity(
            sessionId = sessionId,
            documentId = document.documentId,
            tabOrder = currentSessions.size,
            title = title,
            lifecycle = "ACTIVE",
            accessMode = "READ_WRITE",
            lastActivatedAt = System.currentTimeMillis()
        )
        sessionDao.insertOrUpdate(session)

        // Ensure session-isolated temp directory exists
        val sessionDir = File(baseCacheDir, "editor/$sessionId")
        sessionDir.mkdirs()

        return Result.success(session)
    }

    /**
     * Transitions from [fromSessionId] to [toSessionId] sequentially:
     * 1. Checkpoints and flushes fromSession
     * 2. Sets fromSession to SUSPENDED
     * 3. Sets toSession to ACTIVE
     */
    suspend fun switchSession(
        fromSessionId: String,
        toSessionId: String,
        onCheckpoint: suspend (sessionId: String) -> Unit
    ): Result<WorkspaceSessionEntity> {
        val from = sessionDao.getSession(fromSessionId)
        val to = sessionDao.getSession(toSessionId) ?: return Result.failure(IllegalArgumentException("Target session not found: $toSessionId"))

        if (from != null && from.lifecycle == "ACTIVE") {
            onCheckpoint(fromSessionId)
            sessionDao.update(from.copy(lifecycle = "SUSPENDED"))
        }

        val updatedTo = to.copy(
            lifecycle = "ACTIVE",
            lastActivatedAt = System.currentTimeMillis()
        )
        sessionDao.update(updatedTo)
        return Result.success(updatedTo)
    }

    /**
     * Closes a tab session and cleans up its isolated directory.
     */
    suspend fun closeSession(sessionId: String): String? {
        val session = sessionDao.getSession(sessionId) ?: return null
        val wasActive = session.lifecycle == "ACTIVE"

        sessionDao.delete(session)

        // Clean isolated session cache directory
        val sessionDir = File(baseCacheDir, "editor/$sessionId")
        sessionDir.deleteRecursively()

        // If closed session was active, activate next available session
        if (wasActive) {
            val remaining = sessionDao.getAllSessions()
            val nextActive = remaining.firstOrNull()
            if (nextActive != null) {
                sessionDao.update(nextActive.copy(lifecycle = "ACTIVE"))
                return nextActive.sessionId
            }
        }
        return null
    }

    /**
     * Records a newly committed snapshot for crash resilience.
     */
    suspend fun recordSnapshot(
        snapshotId: String,
        documentId: String,
        sessionId: String,
        sessionEpoch: Long,
        dirtyGeneration: Long,
        stagedPath: String,
        committedPath: String,
        byteSize: Long,
        sha256: String
    ) {
        val snapshot = WorkspaceSnapshotEntity(
            snapshotId = snapshotId,
            documentId = documentId,
            sessionId = sessionId,
            sessionEpoch = sessionEpoch,
            targetDirtyGeneration = dirtyGeneration,
            sourceRevision = dirtyGeneration,
            stagedPath = stagedPath,
            committedPath = committedPath,
            byteSize = byteSize,
            sha256 = sha256,
            state = "DB_COMMITTED"
        )
        snapshotDao.insertOrUpdate(snapshot)

        // Advance document snapshot pointers
        val doc = documentDao.getDocument(documentId)
        if (doc != null) {
            documentDao.update(
                doc.copy(
                    previousValidSnapshotId = doc.latestValidSnapshotId,
                    latestValidSnapshotId = snapshotId,
                    lastCheckpointGeneration = dirtyGeneration
                )
            )
        }
    }
}
