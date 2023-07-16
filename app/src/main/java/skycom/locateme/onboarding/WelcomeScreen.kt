package skycom.locateme.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import skycom.locateme.MainActivity
import skycom.locateme.R
import skycom.locateme.ui.theme.LocateMeTheme

private const val LOCATION_PERMISSION_REQUEST_CODE = 1
private const val PERMISSION_REQUEST_SEND_SMS = 123

class WelcomeScreen : ComponentActivity() {

    var onClickFun: () -> Unit = {}
    var continued = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(checkIfLocationGranted() && checkIfSMSGranted()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        onClickFun = {
            continued = true
            onClickFun = ::requestLocationPermissions
            setContent {
                LocateMeTheme {
                    PermissionLayout(
                        stringResource(R.string.permission_gps),
                        stringResource(R.string.gps_explanation),
                        stringResource(R.string.grant_permission),
                        onClickFun
                    )
                }
            }
        }
        setContent {
            LocateMeTheme {
                PermissionLayout(
                    stringResource(R.string.welcome_to_locateme),
                    stringResource(R.string.welcome_explanation),
                    stringResource(R.string.common_continue),
                    onClickFun
                )
            }
        }
    }

    override fun onResume() {
        if(continued) {
            if (checkIfLocationGranted()) {
                if (checkIfSMSGranted()) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    onClickFun = ::requestSMSPermission
                    setContent {
                        LocateMeTheme {
                            PermissionLayout(
                                stringResource(R.string.permission_sms),
                                stringResource(R.string.sms_explanation),
                                stringResource(R.string.grant_permission),
                                onClickFun
                            )
                        }
                    }
                }
            } else {
                onClickFun = ::requestLocationPermissions
                setContent {
                    LocateMeTheme {
                        PermissionLayout(
                            stringResource(R.string.permission_gps),
                            stringResource(R.string.gps_explanation),
                            stringResource(R.string.grant_permission),
                            onClickFun
                        )
                    }
                }
            }
        }
        super.onResume()
    }

    private fun requestLocationPermissions() {
        this.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun checkIfLocationGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSMSPermission() {
        this.requestPermissions(
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS
            ),
            PERMISSION_REQUEST_SEND_SMS
        )
    }

    private fun checkIfSMSGranted(): Boolean{
        return !(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun GreetingPreview() {
    LocateMeTheme {
        PermissionLayout("Welcome to LocateMe!",
            stringResource(R.string.welcome_explanation),
            stringResource(R.string.common_continue)
        ) { return@PermissionLayout }
    }
}