package vasyl.titles

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.text.InputFilter
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.getAbsolutePath
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("DEPRECATION", "UNUSED_PARAMETER", "unused")
class MainActivity : AppCompatActivity() {
   
    private lateinit var runnableHandler: Handler
    private lateinit var handlerBtn: Handler

    private lateinit var mAvailableMargin: TextView
    private lateinit var mSetMarginButton: Button
    private lateinit var editMargin: EditText

    private lateinit var mAvailableWidth: TextView
    private lateinit var mSetWidthButton: Button
    private lateinit var editWidth: EditText

    private lateinit var mSetColorButton: Button
    private lateinit var mResetColorButton: Button

    private lateinit var mSetBgColorButton: Button
    private lateinit var mResetBgColorButton: Button

    private lateinit var mUpButton: Button
    private lateinit var mCenterButton: Button
    private lateinit var mDownButton: Button

    private lateinit var mSizeButton: Button
    private lateinit var editSize: EditText

    private lateinit var mNormalButton: Button
    private lateinit var mItalicButton: Button
    private lateinit var mBoldButton: Button
    private lateinit var mTtfButton: Button
    private lateinit var mTtfUpButton: Button
    private lateinit var mTtfCenterButton: Button
    private lateinit var mTtfDownButton: Button
    private lateinit var typefaceTextView: TextView  
    private lateinit var typefaceLinearLayout: LinearLayout

    private lateinit var mFytMetaButton: Button
    private lateinit var mFytFileButton: Button

    private lateinit var mDisplayUI: CheckBox
    private lateinit var mAutostart: CheckBox
    private lateinit var mDisplayArtist: CheckBox
    private lateinit var mDoubleViewTxt: TextView 
    private lateinit var mDoubleView: CheckBox

    private lateinit var mPhoneStateButton: Button
    private lateinit var mOutgoingCalls: Button
    private lateinit var mBatteryButton: Button
    private lateinit var mNotificationButton: Button
    private lateinit var mDrawOverAppsButton: Button
    private lateinit var mStoragePermissionsButton: Button
    private lateinit var settings: SharedPreferences
    private lateinit var colorPicker: ColorPicker
    private lateinit var bgColorPicker: ColorPicker

    private var screenWidth: Int = 0
    private var margin: Int = 255
    private var availableMargin: Int = 0
    private var width: Int = 900
    private var availableWidth: Int = 0
    private var numUp: Int = 0
    private var numDown: Int = 0
    private var ttfNumUp: Int = 0
    private var ttfNumDown: Int = 0
    private var size: Int = 16
    private var defaultColorR: Int = 255
    private var defaultColorG: Int = 255
    private var defaultColorB: Int = 255
    private var defaultBgColorR: Int = 255
    private var defaultBgColorG: Int = 255
    private var defaultBgColorB: Int = 255  
    private var typeface: Int = 0
    private var fytData: Int = 1
    private var statusButtonColor = "#FFFFFF"
    private var statusBgButtonColor = "transparent"
    private var displayUi: Boolean = true
    private var displayArtist: Boolean = true
    private var doubleView: Boolean = true
    private var autostart: Boolean = false
    private var allPermissionsGranted: Boolean = false
    private val atomicInitialized = AtomicBoolean(false)
    private val storageHelper = SimpleStorageHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)   
        
        // Display values
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        // SharedPreferences
        settings = getSharedPreferences("savedPrefs", 0)
        val marginPercentage = (screenWidth*0.1275).toInt()
        saveInt("marginPercentage", marginPercentage)
        val widthPercentage = (screenWidth*0.45).toInt()
        saveInt("widthPercentage", widthPercentage)
        margin = settings.getInt("margin", marginPercentage)
        width = settings.getInt("width", widthPercentage)
        numUp = settings.getInt("up", 0)
        numDown = settings.getInt("down", 0)
        ttfNumUp = settings.getInt("ttf_up", 0)
        ttfNumDown = settings.getInt("ttf_down", 0)
        size = settings.getInt("size", 16)
        settings.getString("color", "#FFFFFF")?.let { color ->
            statusButtonColor = color
        }
        settings.getString("bg_color", "transparent")?.let { color ->
            statusBgButtonColor = color
        }
        defaultColorR = settings.getInt("red", 255)
        defaultColorG = settings.getInt("green", 255)
        defaultColorB = settings.getInt("blue", 255)
        defaultBgColorR = settings.getInt("bg_red", 255)
        defaultBgColorG = settings.getInt("bg_green", 255)
        defaultBgColorB = settings.getInt("bg_blue", 255)
        typeface = settings.getInt("typeface", 0)
        fytData = settings.getInt("fytData", 1)
        displayUi = settings.getBoolean("UI", true)
        autostart = settings.getBoolean("autostart", false)
        displayArtist = settings.getBoolean("artist_box", true)
        doubleView = settings.getBoolean("double_view", false)
        val filePath = settings.getString("typeface_ttf", "empty")
        val file = File(filePath)

        setContentView(R.layout.activity_main)

        typefaceTextView = findViewById(R.id.ttf_position)
        typefaceLinearLayout = findViewById(R.id.ttf_buttons)
        if (typeface == 3) {
            if (!file.exists()) {
                typefaceTextView.visibility = View.GONE
                typefaceLinearLayout.visibility = View.GONE
                typeface = 0
                val editor = settings.edit()
                editor.putInt("typeface", 0)
                editor.apply()
            } 
        } else {
            typefaceTextView.visibility = View.GONE
            typefaceLinearLayout.visibility = View.GONE      
        }

        // Caption margin, available margin and according button
        availableMargin = screenWidth - width
        editMargin = findViewById(R.id.edit_margin)
        editMargin.setText(String.format(Locale.US, "%d", margin))
        editMargin.filters = arrayOf(InputFilterMinMax("1", availableMargin.toString()))
        editMargin.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setMarginButton(v)
            }
            false
        }
        mAvailableMargin = findViewById(R.id.available_margin)
        mAvailableMargin.text= getString(R.string.available_margin, " ", "$availableMargin")
        mSetMarginButton = findViewById(R.id.set_margin_button)

        // Caption width, available width and according button
        availableWidth = screenWidth - margin
        editWidth = findViewById(R.id.edit_width)
        editWidth.setText(String.format(Locale.US, "%d", width))
        editWidth.filters = arrayOf(InputFilterMinMax("1", availableWidth.toString()))
        editWidth.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setWidthButton(v)
            }
            false
        }
        mAvailableWidth = findViewById(R.id.available_width)
        mAvailableWidth.text = getString(R.string.available_width, " ", "$availableWidth")     
        mSetWidthButton = findViewById(R.id.set_width_button)

        // Caption height
        mUpButton = findViewById(R.id.up_button)        
        mCenterButton = findViewById(R.id.center_button)
        mDownButton = findViewById(R.id.down_button)
        if (numUp > 0) {
            mUpButton.text = getString(R.string.up_var, "$numUp")
            mDownButton.setText(R.string.down)           
        } else if (numDown > 0){
            mDownButton.text = getString(R.string.down_var, "$numDown") 
            mUpButton.setText(R.string.up)
        } else if (numUp == 0 && numDown == 0) {
            mUpButton.text = "0"
            mDownButton.text = "0"   
        }

        // Font size
        editSize = findViewById(R.id.edit_size)
        editSize.setText(String.format(Locale.US, "%d", size))
        editSize.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "30"))
        editSize.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setSizeButton(v)
            }
            false
        }  
        mSizeButton = findViewById(R.id.set_size_button)     

        // Color picker
        mSetColorButton = findViewById(R.id.set_color)
        mSetColorButton.setBackgroundColor(Color.parseColor(statusButtonColor))
        mResetColorButton = findViewById(R.id.reset_color)

        // Background color picker
        mSetBgColorButton = findViewById(R.id.set_bg_color)
        mSetBgColorButton.setBackgroundColor(returnColor(statusBgButtonColor))
        mResetBgColorButton = findViewById(R.id.reset_bg_color)

        // Typeface
        mNormalButton = findViewById(R.id.normal_button)        
        mItalicButton = findViewById(R.id.italic_button)
        mBoldButton = findViewById(R.id.bold_button)
        mTtfButton = findViewById(R.id.ttf_button)
        when (typeface) {
            0 -> {
                typefaceButtons(Color.GREEN, Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"))
            }
            2 -> {
                typefaceButtons(Color.parseColor("#D6C08A"), Color.GREEN, Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"))
            }
            1 -> {
                typefaceButtons(Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.GREEN, Color.parseColor("#D6C08A"))
            }
            3 -> {
                typefaceButtons(Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.GREEN)
            }           
        }

        storageHelper.onFileSelected = { requestCode, files ->            
            typefaceTextView.visibility = View.VISIBLE
            typefaceLinearLayout.visibility = View.VISIBLE 
            saveInt("typeface", 3)
            typefaceButtons(Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.GREEN)
            val editor = settings.edit()
            editor.putString("typeface_ttf", (files[0].getAbsolutePath(this)).toString())
            editor.apply()
        }

        // ttf height
        mTtfUpButton = findViewById(R.id.ttf_up_button)        
        mTtfCenterButton = findViewById(R.id.ttf_center_button)
        mTtfDownButton = findViewById(R.id.ttf_down_button)
        if (ttfNumUp > 0) {
            mTtfUpButton.text = getString(R.string.ttf_up_var, "$ttfNumUp")
            mTtfDownButton.setText(R.string.ttf_down)           
        } else if (ttfNumDown > 0){
            mTtfDownButton.text = getString(R.string.ttf_down_var, "$ttfNumDown")
            mTtfUpButton.setText(R.string.ttf_up)
        } else if (ttfNumUp == 0 && ttfNumDown == 0) {
            mTtfUpButton.text = "0"
            mTtfDownButton.text = "0"   
        }

        // Fyt title type
        mFytMetaButton = findViewById(R.id.fyt_meta_button)  
        mFytFileButton = findViewById(R.id.fyt_file_button) 
        if (fytData == 1) {
            mFytMetaButton.setBackgroundColor(Color.GREEN)
            mFytFileButton.setBackgroundColor(Color.parseColor("#D6C08A"))
        } else if (fytData == 2) {
            mFytFileButton.setBackgroundColor(Color.GREEN)
            mFytMetaButton.setBackgroundColor(Color.parseColor("#D6C08A"))
        }

        // Display UI CheckBox
        mDisplayUI = findViewById(R.id.display_ui_box)
        mDisplayUI.isChecked = displayUi
        
        // Start app on boot
        mAutostart = findViewById(R.id.autostart_app)
        mAutostart.isChecked = autostart

        // Display artist
        mDisplayArtist = findViewById(R.id.display_artist_box)
        mDisplayArtist.isChecked = displayArtist

        // Double view
        mDoubleViewTxt = findViewById(R.id.double_view_text)
        mDoubleView = findViewById(R.id.double_view_box)
        mDoubleView.isChecked = doubleView
        if (isAppSystem(this)) {
            mDoubleViewTxt.visibility = View.GONE
            mDoubleView.visibility = View.GONE
            val editor = settings.edit()
            editor.putBoolean("double_view", false)
            editor.apply()
        }

        // Required permissions' buttons
        mPhoneStateButton = findViewById(R.id.read_phone_state_button)
        mOutgoingCalls = findViewById(R.id.outgoing_calls_button)
        mBatteryButton = findViewById(R.id.battery_optimization_button)
        mNotificationButton = findViewById(R.id.notification_listener_button)
        mDrawOverAppsButton = findViewById(R.id.draw_over_apps_button)
        mStoragePermissionsButton = findViewById(R.id.storage_permissions_button)

        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), 2)
            }
        }

        // Handlers
        runnableHandler = Handler()
        handlerBtn = Handler()
        runnableHandler.post(runTask)
        handlerBtn.post(checkBtns)
    } 

    private fun isAppSystem(context: Context): Boolean {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(context.packageName, 0)
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    private fun saveInt(string: String?, value: Int) {
        val editor = settings.edit()
        editor.putInt(string, value)
        editor.apply()
    }

    fun setMarginButton(v: View?) {
        hideKeyboard(v)
        if (availableMargin >= editMargin.text.toString().toInt()) {
            margin = editMargin.text.toString().toInt()
            saveInt("margin", editMargin.text.toString().toInt())
            availableWidth = screenWidth - margin
            mAvailableWidth.text = getString(R.string.available_width, " ", "$availableWidth")
            editWidth.filters = arrayOf<InputFilter>(InputFilterMinMax("1", availableWidth.toString()))

            // Show toast
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
            val text = layout.findViewById<TextView>(R.id.text)
            val message = "Margin has been set! \nPause/play media or change \ncurrent track to see the result."
            text.text = message
            val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
            toast.view = layout
            toast.show()
        }
    }

    fun setWidthButton(v: View?) {
        hideKeyboard(v)
        if (availableWidth >= editWidth.text.toString().toInt()) {
            width = editWidth.text.toString().toInt()
            saveInt("width", editWidth.text.toString().toInt())
            availableMargin = screenWidth - width
            mAvailableMargin.text = getString(R.string.available_margin, " ", "$availableMargin")
            editMargin.filters = arrayOf<InputFilter>(InputFilterMinMax("1", availableMargin.toString()))

            // Show toast
            val inflater = layoutInflater
            val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
            val text = layout.findViewById<TextView>(R.id.text)
            val message = "Width has been set! \nPause/play media or change \ncurrent track to see the result."
            text.text = message
            val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
            toast.view = layout
            toast.show()
        }
    }

    fun setColorButton(v: View?) {
        colorPicker = ColorPicker(this, defaultColorR, defaultColorG, defaultColorB)       
        colorPicker.enableAutoClose()
        colorPicker.setCallback(object : ColorPickerCallback {
            override fun onColorChosen(@ColorInt color: Int) {
                defaultColorR = Color.red(color)
                defaultColorG = Color.green(color)
                defaultColorB = Color.blue(color)
                val editor = settings.edit()
                editor.putString("color", String.format("#%06X", (0xFFFFFF and color)))
                editor.putInt("red", Color.red(color))
                editor.putInt("green", Color.green(color))
                editor.putInt("blue", Color.blue(color))
                editor.apply()
                mSetColorButton.setBackgroundColor(color)

                // Show toast
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
                val text = layout.findViewById<TextView>(R.id.text)
                val message = "Status color has been set! \nPause/play media or change \ncurrent track to see the result."
                text.text = message
                val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                toast.view = layout
                toast.show()
            }
        })
        colorPicker.show()
    }

    fun resetColorButton(v: View?) {
        mSetColorButton.setBackgroundColor(Color.parseColor("#FFFFFF"))
        defaultColorR = 255
        defaultColorG = 255
        defaultColorB = 255
        val editor = settings.edit()
        editor.putString("color", "#FFFFFF")
        editor.putInt("red", 255)
        editor.putInt("green", 255)
        editor.putInt("blue", 255)
        editor.apply()
    }

    fun setBgButton(v: View?) {
        bgColorPicker = ColorPicker(this, defaultBgColorR, defaultBgColorG, defaultBgColorB)
        bgColorPicker.enableAutoClose()
        bgColorPicker.setCallback(object : ColorPickerCallback {
            override fun onColorChosen(@ColorInt color: Int) {
                defaultBgColorR = Color.red(color)
                defaultBgColorG = Color.green(color)
                defaultBgColorB = Color.blue(color)
                val editor = settings.edit()
                editor.putString("bg_color", String.format("#%06X", (0xFFFFFF and color)))
                editor.putInt("bg_red", Color.red(color))
                editor.putInt("bg_green", Color.green(color))
                editor.putInt("bg_blue", Color.blue(color))
                editor.apply()
                mSetBgColorButton.setBackgroundColor(color)

                // Show toast
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
                val text = layout.findViewById<TextView>(R.id.text)
                val message = "Background color has been set! \nPause/play media or change \ncurrent track to see the result."
                text.text = message
                val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                toast.view = layout
                toast.show()
            }
        })
        bgColorPicker.show()
    }

    fun resetBgButton(v: View?) {
        mSetBgColorButton.setBackgroundColor(Color.TRANSPARENT)
        defaultBgColorR = 255
        defaultBgColorG = 255
        defaultBgColorB = 255
        val editor = settings.edit()
        editor.putString("bg_color", "transparent")
        editor.putInt("bg_red", 255)
        editor.putInt("bg_green", 255)
        editor.putInt("bg_blue", 255)
        editor.apply()
    }

    private fun returnColor(colorString: String): Int {
        return if (colorString == "transparent") {
            Color.TRANSPARENT
        } else Color.parseColor(colorString)
    }

    fun upButton(v: View?) {
        if (settings.getInt("up", 0) != 0) {
            numUp = settings.getInt("up", 0)
        }
        if (numDown > 0) {
            numDown--
            mDownButton.text = getString(R.string.down_var, "$numDown")
        } else if (numDown == 0 && numUp < 100) {
            numUp++
            mUpButton.text = getString(R.string.up_var, "$numUp")
            mDownButton.setText(R.string.down)
        }
        val editor = settings.edit()
        editor.putInt("up", numUp)
        editor.putInt("down", numDown)
        editor.apply()
    }

    fun centerButton(v: View?) {
        numUp = 0
        numDown = 0
        val editor = settings.edit()
        editor.putInt("up", numUp)
        editor.putInt("down", numDown)
        editor.apply()
        mUpButton.text = "0"
        mDownButton.text = "0"
    }

    fun downButton(v: View?) {
        if (settings.getInt("down", 0) != 0) {
            numDown = settings.getInt("down", 0)
        }
        if (numUp > 0) {
            numUp--
            mUpButton.text = getString(R.string.up_var, "$numUp")
        } else if (numUp == 0 && numDown < 100) {
            numDown++
            mDownButton.text = getString(R.string.down_var, "$numDown")
            mUpButton.setText(R.string.up)
        }
        val editor = settings.edit()
        editor.putInt("up", numUp)
        editor.putInt("down", numDown)
        editor.apply()
    }

    fun setSizeButton(v: View?) {
        hideKeyboard(v)
        size = editSize.text.toString().toInt()
        saveInt("size", editSize.text.toString().toInt())

        // Show toast
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast, findViewById(R.id.toast_layout))
        val text = layout.findViewById<TextView>(R.id.text)
        val message = "Font size has been set! \nPause/play media or change \ncurrent track to see the result."
        text.text = message
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.view = layout
        toast.show()
    }

    private fun hideKeyboard(v: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v?.windowToken, 0)
    }

    fun normalButton(v: View?) {
        saveInt("typeface", 0)
        typefaceButtons(Color.GREEN, Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"))            
        typefaceTextView.visibility = View.GONE
        typefaceLinearLayout.visibility = View.GONE  
    }

    fun italicButton(v: View?) {
        saveInt("typeface", 2)
        typefaceButtons(Color.parseColor("#D6C08A"), Color.GREEN, Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"))            
        typefaceTextView.visibility = View.GONE
        typefaceLinearLayout.visibility = View.GONE  
    }

    fun boldButton(v: View?) {
        saveInt("typeface", 1)
        typefaceButtons(Color.parseColor("#D6C08A"), Color.parseColor("#D6C08A"), Color.GREEN, Color.parseColor("#D6C08A"))            
        typefaceTextView.visibility = View.GONE
        typefaceLinearLayout.visibility = View.GONE  
    }

    fun ttfButton(v: View?) {
        if (isStoragePermissionsGranted()) {
            storageHelper.openFilePicker(filterMimeTypes = arrayOf("font/ttf"))
        } else {
            checkStoragePermissions()
        }
    }

    private fun typefaceButtons(normal: Int, italic: Int, bold: Int, ttf: Int) {
        mNormalButton.setBackgroundColor(normal)
        mItalicButton.setBackgroundColor(italic)
        mBoldButton.setBackgroundColor(bold)
        mTtfButton.setBackgroundColor(ttf)
    }

    fun ttfUpButton(v: View?) {
        if (settings.getInt("ttf_up", 0) != 0) {
            ttfNumUp = settings.getInt("ttf_up", 0)
        }
        if (ttfNumDown > 0) {
            ttfNumDown--
            mTtfDownButton.text = getString(R.string.ttf_down_var, "$ttfNumDown")
        } else if (ttfNumDown == 0 && ttfNumUp < 100) {
            ttfNumUp++
            mTtfUpButton.text = getString(R.string.ttf_up_var, "$ttfNumUp")
            mTtfDownButton.setText(R.string.ttf_down)
        }
        val editor = settings.edit()
        editor.putInt("ttf_up", ttfNumUp)
        editor.putInt("ttf_down", ttfNumDown)
        editor.apply()
    }

    fun ttfCenterButton(v: View?) {
        ttfNumUp = 0
        ttfNumDown = 0
        val editor = settings.edit()
        editor.putInt("ttf_up", ttfNumUp)
        editor.putInt("ttf_down", ttfNumDown)
        editor.apply()
        mTtfUpButton.text = "0"
        mTtfDownButton.text = "0"
    }

    fun ttfDownButton(v: View?) {
        if (settings.getInt("ttf_down", 0) != 0) {
            ttfNumDown = settings.getInt("ttf_down", 0)
        }
        if (ttfNumUp > 0) {
            ttfNumUp--
            mTtfUpButton.text = getString(R.string.ttf_up_var, "$ttfNumUp")
        } else if (ttfNumUp == 0 && ttfNumDown < 100) {
            ttfNumDown++
            mTtfDownButton.text = getString(R.string.ttf_down_var, "$ttfNumDown")
            mTtfUpButton.setText(R.string.ttf_up)
        }
        val editor = settings.edit()
        editor.putInt("ttf_up", ttfNumUp)
        editor.putInt("ttf_down", ttfNumDown)
        editor.apply()
    }

    fun setFytMetaButton(v: View?) {
        saveInt("fytData", 1)
        mFytMetaButton.setBackgroundColor(Color.GREEN)
        mFytFileButton.setBackgroundColor(Color.parseColor("#D6C08A"))
    }

    fun setFytFileButton(v: View?) {
        saveInt("fytData", 2)
        mFytFileButton.setBackgroundColor(Color.GREEN)
        mFytMetaButton.setBackgroundColor(Color.parseColor("#D6C08A"))        
    }

    fun setDisplayUI(v: View?) {
        val editor = settings.edit()
        if (!mDisplayUI.isChecked) {
            displayUi = false
            editor.putBoolean("UI", false)
            editor.apply()
            mDisplayUI.isChecked = false
        } else {
            displayUi = true
            editor.putBoolean("UI", true)
            editor.commit()
            mDisplayUI.isChecked = true
        }
    }    

    fun setAutostart(v: View?) {
        val editor = settings.edit()
        if (!mAutostart.isChecked) {
            autostart = false
            editor.putBoolean("autostart", false)
            editor.apply()
            mAutostart.isChecked = false
        } else {
            autostart = true
            editor.putBoolean("autostart", true)
            editor.commit()
            mAutostart.isChecked = true
        }
    }

    fun setDisplayArtist(v: View?) {
        val editor = settings.edit()
        if (!mDisplayArtist.isChecked) {
            displayArtist = false
            editor.putBoolean("artist_box", false)
            editor.apply()
            mDisplayArtist.isChecked = false
        } else {
            displayArtist = true
            editor.putBoolean("artist_box", true)
            editor.commit()
            mDisplayArtist.isChecked = true
        }
    }

    fun setDoubleView(v: View?) {
        val editor = settings.edit()
        if (!mDoubleView.isChecked) {
            doubleView = false
            editor.putBoolean("double_view", false)
            editor.apply()
            mDoubleView.isChecked = false
        } else {
            doubleView = true
            editor.putBoolean("double_view", true)
            editor.commit()
            mDoubleView.isChecked = true
        }
    }
    
    fun phoneStateButton(v: View?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            goToSettings()
        }
    }

    fun outgoingCallsButton(v: View?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
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

    fun storagePermissionsButton(v: View?) {
        checkStoragePermissions()
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
                checkStoragePermissions()
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

    /*override fun onUserLeaveHint() {
        if (allPermissionsGranted) onBackPressed()
        super.onUserLeaveHint()
    }*/

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
        val notificationListenerString = Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
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

    private fun isStoragePermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkStoragePermissions() {
        if (!isStoragePermissionsGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                        data = Uri.fromParts("package", packageName, null)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent().apply {
                        action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    }
                    startActivity(intent)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    3
                )
            }
        }
    }

    private val runTask = object : Runnable {
        override fun run() {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val notificationListenerString = Settings.Secure.getString(this@MainActivity.contentResolver, "enabled_notification_listeners")
            if ((pm.isIgnoringBatteryOptimizations(packageName)
                && Settings.canDrawOverlays(this@MainActivity)) 
                && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && notificationListenerString.contains(packageName)
                && isStoragePermissionsGranted())
            {
                allPermissionsGranted = true
            } 

            if (allPermissionsGranted && !displayUi) {
                // Run only once
                if (atomicInitialized.compareAndSet(false, true)) {
                    //Toast.makeText(context, "Type *#*#3368#*#* in the dialer to open the UI.", Toast.LENGTH_LONG).show()
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

            if (!isStoragePermissionsGranted()) {
                mStoragePermissionsButton.setBackgroundColor(Color.RED)
            } else {
                mStoragePermissionsButton.setBackgroundColor(Color.GREEN)
                mStoragePermissionsButton.isEnabled = false
            }
            handlerBtn.postDelayed(this, 50)
        }
    }
}

