package app.documents.core.database.workspace

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkspaceDocumentEntity::class,
        WorkspaceSessionEntity::class,
        WorkspaceSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {

    abstract fun documentDao(): WorkspaceDocumentDao
    abstract fun sessionDao(): WorkspaceSessionDao
    abstract fun snapshotDao(): WorkspaceSnapshotDao

    companion object {
        const val NAME: String = "workspace_database"
    }
}
