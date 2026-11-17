package app.documents.core.database.workspace

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workspace_documents",
    indices = [
        Index(value = ["canonicalSourceKey"], unique = true)
    ]
)
data class WorkspaceDocumentEntity(
    @PrimaryKey
    val documentId: String,
    val canonicalSourceKey: String,
    val originalUri: String,
    val persistedPermission: Boolean,
    val editorKind: String, // WRITE, SHEET, PRESENTATION
    val sourceExtension: String,
    val sourceMimeType: String,
    val effectiveWorkingFormat: String,
    val originalWorkingCopyPath: String,
    val activeWriterSessionId: String? = null,
    val dirtyGeneration: Long = 0L,
    val lastCheckpointGeneration: Long = 0L,
    val localRevision: Long = 0L,
    val latestValidSnapshotId: String? = null,
    val previousValidSnapshotId: String? = null,
    val cloudProvider: String? = null,
    val cloudFileId: String? = null,
    val observedRemoteVersion: String? = null,
    val syncState: String = "LOCAL_ONLY"
)

@Entity(
    tableName = "workspace_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["tabOrder"])
    ]
)
data class WorkspaceSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val documentId: String,
    val tabOrder: Int,
    val title: String,
    val sessionEpoch: Long = 1L,
    val lifecycle: String = "SUSPENDED", // ACTIVE, CHECKPOINTING, SUSPENDED, FAILED
    val accessMode: String = "READ_WRITE", // READ_WRITE, READ_ONLY
    val lastActivatedAt: Long = System.currentTimeMillis(),
    val lastCheckpointId: String? = null,
    val viewStateJson: String? = null
)

@Entity(
    tableName = "workspace_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["documentId"]),
        Index(value = ["sessionId"])
    ]
)
data class WorkspaceSnapshotEntity(
    @PrimaryKey
    val snapshotId: String,
    val documentId: String,
    val sessionId: String,
    val sessionEpoch: Long,
    val targetDirtyGeneration: Long,
    val sourceRevision: Long,
    val stagedPath: String,
    val committedPath: String,
    val byteSize: Long,
    val sha256: String,
    val state: String = "STAGING", // STAGING, VALIDATED, FILE_COMMITTED, DB_COMMITTED, SUPERSEDED, FAILED
    val createdAt: Long = System.currentTimeMillis()
)
