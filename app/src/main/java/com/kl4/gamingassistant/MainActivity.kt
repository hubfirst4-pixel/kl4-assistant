package com.kl4.gamingassistant

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF00E5FF),
                    background = Color(0xFF0A0E1A),
                    surface = Color(0xFF131A2B)
                )
            ) {
                Dashboard()
            }
        }
    }
}

@Composable
fun Dashboard() {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0L) }
    var statusMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            tick++
        }
    }

    val dev = remember(tick) { deviceInfo(ctx) }
    val bat = remember(tick) { batteryInfo(ctx) }
    val sto = remember(tick) { storageInfo() }
    val net = remember(tick) { networkInfo(ctx) }
    val ram = remember(tick) { ramInfo(ctx) }

    Surface(color = Color(0xFF0A0E1A), modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "KL4 Gaming Assistant",
                color = Color(0xFF00E5FF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            InfoCard("Device") {
                InfoRow("Model", dev.model)
                InfoRow("Android", "${dev.android} (SDK ${dev.sdk})")
                InfoRow("Cores", dev.cores.toString())
                InfoRow("Resolution", "${dev.w}x${dev.h}")
                InfoRow("Refresh", "${dev.refresh.toInt()} Hz")
            }

            Spacer(Modifier.height(12.dp))

            InfoCard("Resources") {
                InfoRow("RAM free", human(ram.first))
                InfoRow("RAM total", human(ram.second))
                InfoRow("Storage free", human(sto.first))
                InfoRow("Storage total", human(sto.second))
            }

            Spacer(Modifier.height(12.dp))

            InfoCard("Battery & Network") {
                InfoRow("Battery", "${bat.percent}%")
                InfoRow("Charging", bat.source)
                bat.temp?.let { InfoRow("Temp", "${"%.1f".format(it)} °C") }
                InfoRow("Health", bat.health)
                InfoRow("Network", net)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    statusMsg = launchFreeFire(ctx)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "START FREE FIRE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            if (statusMsg.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    statusMsg,
                    color = Color(0xFFFFC107),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "لا يستخدم التطبيق Root أو تعديل اللعبة.",
                color = Color(0xFF90A4AE),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2B))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFB0BEC5), fontSize = 14.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

// ============ FREE FIRE LAUNCHER (all known packages) ============

fun launchFreeFire(ctx: Context): String {
    val pm: PackageManager = ctx.packageManager

    val candidates = listOf(
        "com.dts.freefireth",
        "com.dts.freefiremax",
        "com.garena.game.kgth",
        "com.garena.game.kgvn",
        "com.dts.freefire"
    )

    for (pkg in candidates) {
        try {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
                return "Opening: $pkg"
            }
        } catch (_: Exception) { }
    }

    // Fallback: search all installed apps for Free Fire
    try {
        val all = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in all) {
            val name = app.packageName.lowercase()
            if (name.contains("freefire") || name.contains("dts.ff") ||
                name.contains("garena") && name.contains("ff")) {
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                    return "Opening: ${app.packageName}"
                }
            }
        }
    } catch (_: Exception) { }

    return "Free Fire not found. Verify it is installed."
}

// ============ DATA ============

data class DevInfo(
    val model: String, val android: String, val sdk: Int,
    val cores: Int, val w: Int, val h: Int, val refresh: Float
)

fun deviceInfo(ctx: Context): DevInfo {
    val dm = ctx.resources.displayMetrics
    return DevInfo(
        model = Build.MODEL,
        android = Build.VERSION.RELEASE,
        sdk = Build.VERSION.SDK_INT,
        cores = Runtime.getRuntime().availableProcessors(),
        w = dm.widthPixels,
        h = dm.heightPixels,
        refresh = ctx.display?.refreshRate ?: 60f
    )
}

data class BatInfo(val percent: Int, val source: String, val temp: Float?, val health: String)

fun batteryInfo(ctx: Context): BatInfo {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        ?: return BatInfo(0, "?", null, "?")
    val lvl = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scl = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
    val pct = if (lvl >= 0 && scl > 0) lvl * 100 / scl else 0
    val plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val src = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "No"
    }
    val t = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
    val temp = if (t > 0) t / 10f else null
    val health = when (i.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        else -> "Unknown"
    }
    return BatInfo(pct, src, temp, health)
}

fun storageInfo(): Pair<Long, Long> {
    val s = StatFs(Environment.getDataDirectory().path)
    return (s.availableBlocksLong * s.blockSizeLong) to
            (s.blockCountLong * s.blockSizeLong)
}

fun ramInfo(ctx: Context): Pair<Long, Long> {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    return mi.availMem to mi.totalMem
}

fun networkInfo(ctx: Context): String {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val n = cm.activeNetwork ?: return "None"
    val c = cm.getNetworkCapabilities(n) ?: return "None"
    return when {
        c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
        else -> "Other"
    }
}

fun human(b: Long): String {
    if (b <= 0) return "0 B"
    val u = arrayOf("B", "KB", "MB", "GB")
    var v = b.toDouble()
    var i = 0
    while (v >= 1024 && i < 3) {
        v /= 1024.0
        i++
    }
    return String.format(Locale.US, "%.2f %s", v, u[i])
}
