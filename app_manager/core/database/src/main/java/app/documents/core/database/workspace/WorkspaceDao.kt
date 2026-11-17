package app.documents.core.database.workspace

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDocumentDao {
    @Query("SELECT * FROM workspace_documents WHERE documentId = :id")
    suspend fun getDocument(id: String): WorkspaceDocumentEntity?

    @Query("SELECT * FROM workspace_documents WHERE canonicalSourceKey = :key")
    suspend fun getDocumentByCanonicalKey(key: String): WorkspaceDocumentEntity?

    @Query("SELECT * FROM workspace_documents")
    fun getAllDocumentsFlow(): Flow<List<WorkspaceDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(doc: WorkspaceDocumentEntity)

    @Update
    suspend fun update(doc: WorkspaceDocumentEntity)

    @Delete
    suspend fun delete(doc: WorkspaceDocumentEntity)
}

@Dao
interface WorkspaceSessionDao {
    @Query("SELECT * FROM workspace_sessions WHERE sessionId = :id")
    suspend fun getSession(id: String): WorkspaceSessionEntity?

    @Query("SELECT * FROM workspace_sessions ORDER BY tabOrder ASC")
    fun getAllSessionsFlow(): Flow<List<WorkspaceSessionEntity>>

    @Query("SELECT * FROM workspace_sessions ORDER BY tabOrder ASC")
    suspend fun getAllSessions(): List<WorkspaceSessionEntity>

    @Query("SELECT COUNT(*) FROM workspace_sessions")
    suspend fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(session: WorkspaceSessionEntity)

    @Update
    suspend fun update(session: WorkspaceSessionEntity)

    @Delete
    suspend fun delete(session: WorkspaceSessionEntity)

    @Query("DELETE FROM workspace_sessions WHERE sessionId = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WorkspaceSnapshotDao {
    @Query("SELECT * FROM workspace_snapshots WHERE snapshotId = :id")
    suspend fun getSnapshot(id: String): WorkspaceSnapshotEntity?

    @Query("SELECT * FROM workspace_snapshots WHERE documentId = :documentId ORDER BY targetDirtyGeneration DESC")
    suspend fun getSnapshotsForDocument(documentId: String): List<WorkspaceSnapshotEntity>

    @Query("SELECT * FROM workspace_snapshots WHERE state = :state")
    suspend fun getSnapshotsByState(state: String): List<WorkspaceSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(snapshot: WorkspaceSnapshotEntity)

    @Update
    suspend fun update(snapshot: WorkspaceSnapshotEntity)

    @Delete
    suspend fun delete(snapshot: WorkspaceSnapshotEntity)

    @Query("DELETE FROM workspace_snapshots WHERE state = 'SUPERSEDED' AND documentId = :documentId")
    suspend fun deleteSuperseded(documentId: String)
}
