package but.projet.projetvikazim

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_requests")
data class PendingFetchData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String,
    val url: String,
    val bodyJson: String
)