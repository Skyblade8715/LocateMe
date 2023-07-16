package skycom.locateme.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Entity(tableName = "phone_numbers")
data class PhoneNumber(
    @PrimaryKey
    val number: String,
    val name: String,
    @ColumnInfo(name = "isSharingData")
    val isSharingData: Boolean
)

class PhoneNumberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RoomDbProvider.getDatabase(application.baseContext).phoneNumberDao()

    var numbers: LiveData<List<PhoneNumber>> =
        db.getAllPhoneNumbers()

    fun addNumber(number: PhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            db.addPhoneNumber(number)
        }
    }
    fun deleteNumber(number: PhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            db.removePhoneNumber(number)
        }
    }
    fun updateNumber(number: PhoneNumber) {
        viewModelScope.launch(Dispatchers.IO) {
            db.updatePhoneNumber(number)
        }
    }

}
