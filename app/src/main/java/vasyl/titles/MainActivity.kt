package vasyl.titles

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.text.InputFilter
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("DEPRECATION", "UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    private lateinit var runnableHandler: Handler
    private lateinit var handlerBtn: Handler

    private lateinit var mAvailableMargin: TextView
    private lateinit var mSetMarginButton: Button
    private lateinit var editMargin: EditText

    private lateinit var mAvailableWidth: TextView
    private lateinit var mSetWidthButton: Button
    private lateinit var editWidth: EditText

    private lateinit var mDisplayUI: CheckBox

    private lateinit var mPhoneStateButton: Button
    private lateinit var mOutgoingCalls: Button
    private lateinit var mBatteryButton: Button
    private lateinit var mNotificationButton: Button
    private lateinit var mDrawOverAppsButton: Button
    private lateinit var settings: SharedPreferences

    private var screenWidth: Int = 0
    private var availableMargin: Int = 0
    private var availableWidth: Int = 0
    private var allPermissionsGranted: Boolean = false
    private val atomicInitialized = AtomicBoolean(false)
    private val notificationListener = NotificationListener()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences
        settings = getSharedPreferences("savedInts", 0)
        if (settings.getInt("margin", 0) != 0) {
            notificationListener.MARGIN_LEFT = settings.getInt("margin", 0)
        }
        if (settings.getInt("width", 0) != 0) {
            notificationListener.WIDTH = settings.getInt("width", 0)
        }
        notificationListener.DISPLAY_UI = settings.getBoolean("UI", true)

        // Display values
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        availableMargin = screenWidth - notificationListener.WIDTH
        availableWidth = screenWidth - notificationListener.MARGIN_LEFT

        setContentView(R.layout.activity_main)

        // Caption margin, available margin and according button
        editMargin = findViewById(R.id.edit_margin)
        editMargin.setText(notificationListener.MARGIN_LEFT.toString())
        editMargin.filters = arrayOf(InputFilterMinMax("1", availableMargin.toString()))
        mAvailableMargin = findViewById(R.id.available_margin)
        mAvailableMargin.text = " (available: $availableMargin):"
        mSetMarginButton = findViewById(R.id.set_margin_button)

        // Caption width, available width and according button
        editWidth = findViewById(R.id.edit_width)
        editWidth.setText(notificationListener.WIDTH.toString())
        editWidth.filters = arrayOf(InputFilterMinMax("1", availableWidth.toString()))
        mAvailableWidth = findViewById(R.id.available_width)
        mAvailableWidth.text = " (available: $availableWidth):"        
        mSetWidthButton = findViewById(R.id.set_width_button)

        // Display UI CheckBox
        mDisplayUI = findViewById(R.id.display_ui)
        mDisplayUI.isChecked = notificationListener.DISPLAY_UI

        // Required permissions' buttons
        mPhoneStateButton = findViewById(R.id.read_phone_state_button)
        mOutgoingCalls = findViewById(R.id.outgoing_calls_button)
        mBatteryButton = findViewById(R.id.battery_optimization_button)
        mNotificationButton = findViewById(R.id.notification_listener_button)
        mDrawOverAppsButton = findViewById(R.id.draw_over_apps_button)

        runnableHandler = Handler()
        handlerBtn = Handler()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), 2)
            }
        }

        runnableHandler.post(runTask)
        handlerBtn.post(checkBtns)
    }

    private fun saveInt(string: String?, value: Int) {
        val editor = settings.edit()
        editor.putInt(string, value)
        editor.apply()
    }

    @SuppressLint("SetTextI18n")
    fun setMarginButton(v: View?) {
        if (availableMargin >= editMargin.text.toString().toInt()) {
            notificationListener.MARGIN_LEFT = editMargin.text.toString().toInt()
            saveInt("margin", editMargin.text.toString().toInt())
            availableWidth = screenWidth - notificationListener.MARGIN_LEFT
            mAvailableWidth.text = "(available: $availableWidth):"
            editWidth.filters =
                arrayOf<InputFilter>(InputFilterMinMax("1", availableWidth.toString()))

            // Show toast
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
            val text = layout.findViewById<TextView>(R.id.text)
            val message =
                "Margin has been set! \nPause/play media or change \ncurrent track to see the result."
            text.text = message
            val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
            toast.view = layout
            toast.show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setWidthButton(v: View?) {
        if (availableWidth >= editWidth.text.toString().toInt()) {
            notificationListener.WIDTH = editWidth.text.toString().toInt()
            saveInt("width", editWidth.text.toString().toInt())
            availableMargin = screenWidth - notificationListener.WIDTH
            mAvailableMargin.text = "(available: $availableMargin):"
            editMargin.filters =
                arrayOf<InputFilter>(InputFilterMinMax("1", availableMargin.toString()))

            // Show toast
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
            val text = layout.findViewById<TextView>(R.id.text)
            val message =
                "Width has been set! \nPause/play media or change \ncurrent track to see the result."
            text.text = message
            val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
            toast.view = layout
            toast.show()
        }
    }

    fun setDisplayUI(v: View?) {
        val editor = settings.edit()
        if (!mDisplayUI.isChecked) {
            notificationListener.DISPLAY_UI = false
            Log.i("UI", (notificationListener.DISPLAY_UI).toString())
            editor.putBoolean("UI", false)
            editor.apply()
            mDisplayUI.isChecked = false
        } else {
            notificationListener.DISPLAY_UI = true
            Log.i("UI", (notificationListener.DISPLAY_UI).toString())
            editor.putBoolean("UI", true)
            editor.commit()
            mDisplayUI.isChecked = true
        }
    }

    fun phoneStateButton(v: View?) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            goToSettings()
        }
    }

    fun outgoingCallsButton(v: View?) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            goToSettings()
        }
    }

    private fun goToSettings() {
        val intentPhone = Intent()
        intentPhone.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intentPhone.addCategory(Intent.CATEGORY_DEFAULT)
        intentPhone.setData(Uri.parse("package:$packageName"))
        intentPhone.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentPhone.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intentPhone.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        startActivity(intentPhone)
    }

    fun batteryButton(v: View?) {
        checkBatteryPermission()
    }

    fun notificationButton(v: View?) {
        checkNotificationPermission()
    }

    fun drawOverAppsButton(v: View?) {
        checkOverlayPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), 2)
            }

            2 -> {
                checkBatteryPermission()
                checkNotificationPermission()
                checkOverlayPermission()
            }

            else -> {}
        }
    }

    override fun onDestroy() {
        runnableHandler.removeCallbacks(runTask)
        handlerBtn.removeCallbacks(checkBtns)
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (allPermissionsGranted) onBackPressed()
    }

    @SuppressLint("BatteryLife")
    fun checkBatteryPermission() {
        // Do not optimize battery permission  
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intentBattery = Intent()
            intentBattery.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intentBattery.setData(Uri.parse("package:$packageName"))
            intentBattery.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentBattery.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intentBattery.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intentBattery)
        }
    }

    private fun checkNotificationPermission() {
        // Notifications access permission
        val notificationListenerString =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        if (notificationListenerString == null || !notificationListenerString.contains(packageName)) {
            val intentNoti = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intentNoti.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentNoti.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intentNoti.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intentNoti)
        }
    }

    private fun checkOverlayPermission() {
        // Draw over other apps permission
        if (!Settings.canDrawOverlays(this)) {
            val intentOverlays = Intent()
            intentOverlays.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intentOverlays.setData(Uri.parse("package:$packageName"))
            intentOverlays.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intentOverlays.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intentOverlays.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intentOverlays)
        }
    }

    private val runTask = object : Runnable {
        override fun run() {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val notificationListenerString = Settings.Secure.getString(this@MainActivity.contentResolver, "enabled_notification_listeners")
            if ((pm.isIgnoringBatteryOptimizations(packageName)
                && Settings.canDrawOverlays(this@MainActivity)) 
                && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && notificationListenerString.contains(packageName))
            {
                allPermissionsGranted = true
            } 

            if (allPermissionsGranted && !notificationListener.DISPLAY_UI) {
                // Run only once
                if (atomicInitialized.compareAndSet(false, true)) {
                    minimize()
                }                
            } 

            if (!atomicInitialized.get()) {
                runnableHandler.postDelayed(this, 300)
            }
        }
    }

    private fun minimize() {
        handlerBtn.removeCallbacks(checkBtns)
        finish()
    }

    private val checkBtns = object : Runnable {
        override fun run() {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val notificationListenerString = Settings.Secure.getString(this@MainActivity.contentResolver, "enabled_notification_listeners")
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                mPhoneStateButton.setBackgroundColor(Color.RED)
            } else {
                mPhoneStateButton.setBackgroundColor(Color.GREEN)
                mPhoneStateButton.isEnabled = false
            }

            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                mOutgoingCalls.setBackgroundColor(Color.RED)
            } else {
                mOutgoingCalls.setBackgroundColor(Color.GREEN)
                mOutgoingCalls.isEnabled = false
            }

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                mBatteryButton.setBackgroundColor(Color.RED)
            } else {
                mBatteryButton.setBackgroundColor(Color.GREEN)
                mBatteryButton.isEnabled = false
            }

            if (!notificationListenerString.contains(packageName)) {
                mNotificationButton.setBackgroundColor(Color.RED)
            } else {
                mNotificationButton.setBackgroundColor(Color.GREEN)
                mNotificationButton.isEnabled = false
            }

            if (!Settings.canDrawOverlays(this@MainActivity)) {
                mDrawOverAppsButton.setBackgroundColor(Color.RED)
            } else {
                mDrawOverAppsButton.setBackgroundColor(Color.GREEN)
                mDrawOverAppsButton.isEnabled = false
            }
            handlerBtn.postDelayed(this, 50)
        }
    }
}