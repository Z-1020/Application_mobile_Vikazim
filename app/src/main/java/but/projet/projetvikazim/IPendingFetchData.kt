package but.projet.projetvikazim

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IPendingFetchData {
    @Query("SELECT * FROM pending_requests ORDER BY id ASC")
    suspend fun getAll(): List<PendingRequestData>

    @Insert
    suspend fun insert(request: PendingRequestData)

    @Query("DELETE FROM pending_requests WHERE id = :id")
    suspend fun deleteById(id: Int)
}