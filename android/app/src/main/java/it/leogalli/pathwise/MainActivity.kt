package it.leogalli.pathwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import it.leogalli.pathwise.ui.navigation.PathWiseNavHost
import it.leogalli.pathwise.ui.theme.PathWiseTheme
import it.leogalli.pathwise.util.LocaleHelper

/**
 * Activity unica (single-activity): l'intera UI è Jetpack Compose.
 * Qui vengono gestiti anche i permessi runtime (posizione, notifiche).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* granted */ }

    /**
     * API < 33: ri-basa il context sulla lingua salvata PRIMA della creazione
     * delle risorse, così l'override è efficace senza riavviare lo smartphone.
     */
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.localeAwareContext(newBase, LocaleHelper.lastAppliedLanguage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestEssentialPermissions()
        setContent {
            PathWiseTheme {
                PathWiseNavHost()
            }
        }
    }

    private fun requestEssentialPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.filterNot {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
