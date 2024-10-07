package vasyl.titles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class Autostart : BroadcastReceiver() {

    private var autostart = false    
    private lateinit var settings: SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            settings = context.getSharedPreferences("savedPrefs", 0)
            autostart = settings.getBoolean("autostart", false)
            if (autostart) {
                val intentAutostart = Intent(context, MainActivity::class.java)
                intentAutostart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intentAutostart.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intentAutostart.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                context.startActivity(intentAutostart)                
            }
        }
    }
}