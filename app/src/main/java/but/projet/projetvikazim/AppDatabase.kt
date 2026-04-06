package but.projet.projetvikazim

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserData::class, SettingsData::class, PendingRequestData::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): IUserDao
    abstract fun settingsDao(): ISettingsDAO
    abstract fun pendingRequestData(): IPendingFetchData


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "g6_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}