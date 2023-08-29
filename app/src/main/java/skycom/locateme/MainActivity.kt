package skycom.locateme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import skycom.locateme.database.GeoData
import skycom.locateme.database.PhoneNumber
import skycom.locateme.database.PhoneNumberViewModel
import skycom.locateme.database.RoomDb
import skycom.locateme.database.RoomDbProvider
import skycom.locateme.ui.theme.LocateMeTheme


lateinit var fusedLocationClient: FusedLocationProviderClient
private const val LOCATION_PERMISSION_REQUEST_CODE = 1
private const val REQUEST_CONTACT = 12
private const val PERMISSION_REQUEST_SEND_SMS = 123

class MainActivity : ComponentActivity() {
    lateinit var db : RoomDb
    private val phoneNumberViewModel: PhoneNumberViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = RoomDbProvider.getDatabase(this)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        val serviceIntent = Intent(this, SmsService::class.java)
        startService(serviceIntent)
        setContent {
            LocateMeTheme {
                MainScreen()
            }
        }
    }

//    @Preview(
//        uiMode = Configuration.UI_MODE_NIGHT_YES,
//        showBackground = true,
//        name = "Dark Mode"
//    )
//    @Composable
//    fun PreviewContactTile() {
//        LocateMeTheme {
//            Surface() {
//                ContactTile(PhoneNumber("+48531234203", "Adam Badam", true))
//            }
//        }
//    }

    @Composable
    fun ContactTile(phoneNumber: PhoneNumber, ctx: Context){
        var isExpanded by remember {
            mutableStateOf(true)
        }
        val surfaceColor by animateColorAsState(
            if (isExpanded)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
        Surface(
            color = surfaceColor,
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(1.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 100.dp else 40.dp)
                    .clickable { isExpanded = !isExpanded }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val icon: Painter
                    val color: ColorFilter
                    if (phoneNumber.isSharingData) {
                        icon = painterResource(id = R.drawable.location_enabled)
                        color = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    } else {
                        icon = painterResource(id = R.drawable.location_disabled)
                        color = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                    }
                    Image(
                        painter = icon,
                        contentDescription = null,
                        colorFilter = color,
                        modifier = Modifier
                            .scale(1.2f)
                            .padding(start = 16.dp)
                    )
                    Text(
                        text = phoneNumber.number,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        fontSize = (18.sp)
                    )
                    Text(
                        text = phoneNumber.name,
                        modifier = Modifier.padding(end = 16.dp),
                        fontSize = (18.sp)
                    )
                }
                Row(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                phoneNumberViewModel.deleteNumber(phoneNumber)
                            },
                            Modifier
                                .padding(start = 16.dp)
                                .width(40.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                        ) {
                            Icon(painter = painterResource(
                                id = R.drawable.remove_contact),
                                contentDescription = "",
                                Modifier.size(28.dp)
                            )
                        }
                        Button(
                            onClick = {
                                val updatedPhoneNumber = phoneNumber.copy(isSharingData = !phoneNumber.isSharingData)
                                phoneNumberViewModel.updateNumber(updatedPhoneNumber)
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Disable",
                                color = MaterialTheme.colorScheme.background
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.location_enabled),
                                contentDescription = "",
                                Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                        Button(
                            onClick = { SmsHandler.sendSMS(phoneNumber.number,
                            "LocateMe!\nYour location has been requested by this number!\n",
                                ctx) },
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Request",
                                color = MaterialTheme.colorScheme.background
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.request_location),
                                contentDescription = "",
                                Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                        Button(
                            onClick = {
                                GeoData.getAndSendLocation(phoneNumber.number, ctx)
                            },
                            Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Send",
                                color = MaterialTheme.colorScheme.background
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.quick_location_send),
                                contentDescription = null,
                                Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
        }
    }

    @Composable
    fun ContactList(ctx: Context) {
        val numbers: List<PhoneNumber> by phoneNumberViewModel.numbers.observeAsState(emptyList())

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ){
                items(items = numbers, key = {it.number}) { number ->
                    ContactTile(phoneNumber = number, ctx)
                }
            }
            Button(
                onClick = {
                    pickContact()
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Add new contact")
            }
        }
    }

    fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, Phone.CONTENT_URI)
        startActivityForResult(intent, REQUEST_CONTACT)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK) {
            val contactUri = data?.data

            contactUri?.let { uri ->
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(
                        Phone.DISPLAY_NAME,
                        Phone.NUMBER
                    ),
                    null,
                    null,
                    null
                )

                cursor?.let {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(Phone.DISPLAY_NAME)
                        val numberIndex = it.getColumnIndex(Phone.NUMBER)

                        val name = it.getString(nameIndex)
                        val phoneNumber = it.getString(numberIndex)

                        phoneNumberViewModel.addNumber(
                            PhoneNumber(phoneNumber, name, true)
                        )
                        Log.d("ContactPicker", "Selected Contact - Name: $name, Phone Number: $phoneNumber")

                        // Do something with the selected contact's name and phone number
                        // For example, display it in a TextView
                    } else {
                        Log.d("ContactPicker", "No data found for the selected contact.")
                    }
                    it.close()
                }
            } ?: run {
                Log.d("ContactPicker", "No contact URI found for the selected contact.")
            }
        }
    }

    @Composable
    fun WelcomeAndExplanation(){
        Text(
            text = stringResource(R.string.welcome_to_locateme),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        var isExpanded by remember {
            mutableStateOf(false)
        }
        val text = if(isExpanded){
            stringResource(R.string.help_welcome_screen)
        } else {
            stringResource(R.string.press_to_see_help)
        }
        Surface(
            onClick = {isExpanded = !isExpanded},
        ){
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp),
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }

    @Composable
    fun MainScreen() {
        val ctx = LocalContext.current
        Surface() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                WelcomeAndExplanation()
                ContactList(ctx)
            }
        }
    }
//
//    @Preview(
//        uiMode = Configuration.UI_MODE_NIGHT_YES,
//        showBackground = true,
//        name = "Dark Mode"
//    )
//    @Preview(name = "Light Mode")
//    @Composable
//    fun PreviewConversation() {
//        LocateMeTheme {
//            MainScreen()
//        }
//    }
}

