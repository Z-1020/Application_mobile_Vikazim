package but.projet.projetvikazim

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IPendingFetchData {
    @Query("SELECT * FROM pending_requests ORDER BY id ASC")
    suspend fun getAll(): List<PendingFetchData>

    @Insert
    suspend fun insert(request: PendingFetchData)

    @Query("DELETE FROM pending_requests WHERE id = :id")
    suspend fun deleteById(id: Int)
}