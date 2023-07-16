package skycom.locateme.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

object RoomDbProvider {
    private var appDatabase: RoomDb? = null

    fun getDatabase(context: Context): RoomDb {
        return appDatabase ?: synchronized(this) {
            appDatabase ?: buildDatabase(context).also { appDatabase = it }
        }
    }

    private fun buildDatabase(context: Context): RoomDb {
        return Room.databaseBuilder(context, RoomDb::class.java, "app_database")
            .build()
    }
}

@Database(entities = [PhoneNumber::class], version = 1)
abstract class RoomDb : RoomDatabase() {
    abstract fun phoneNumberDao(): PhoneNumberDao
}

@Dao
interface PhoneNumberDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addPhoneNumber(phoneNumber: PhoneNumber)

    @Query("SELECT EXISTS(SELECT 1 FROM phone_numbers WHERE number = :phoneNumber)")
    fun containsPhoneNumber(phoneNumber: String): Boolean

    @Query("SELECT * FROM phone_numbers WHERE number = :phoneNumber")
    fun getPhoneNumber(phoneNumber: String): PhoneNumber

    @Query("SELECT * FROM phone_numbers")
    fun getAllPhoneNumbers(): LiveData<List<PhoneNumber>>

    @Query("SELECT isSharingData FROM phone_numbers WHERE number = :phoneNumber")
    fun isPhoneNumberEnabled(phoneNumber: String): Boolean

    @Update
    suspend fun updatePhoneNumber(phoneNumber: PhoneNumber)

    @Delete
    suspend fun removePhoneNumber(phoneNumber: PhoneNumber)
}

