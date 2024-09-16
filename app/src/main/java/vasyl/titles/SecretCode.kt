package vasyl.titles

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SecretCode : BroadcastReceiver() {
    
    private val notificationListener = NotificationListener()

    override fun onReceive(context: Context, intent: Intent) {
        if ("android.provider.Telephony.SECRET_CODE" == intent.action) {
            notificationListener.DISPLAY_UI = true
            val settings = context.getSharedPreferences("savedInts", 0)
            val editor = settings.edit()
            editor.putBoolean("UI", true)
            editor.apply()
            try {
                val i = Intent()
                i.setClassName("vasyl.titles", "vasyl.titles.MainActivity")
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}