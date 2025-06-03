package ru.transport.threeka.ui.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import io.appmetrica.analytics.AppMetrica
import ru.transport.threeka.R
import ru.transport.threeka.services.TokenManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: MaterialSwitch
    private lateinit var northSwitch: MaterialSwitch
    private lateinit var notifSwitch: MaterialSwitch
    private lateinit var sharedPref: SharedPreferences
    private lateinit var header: TextView
    private lateinit var enteringButtons: LinearLayout
    private lateinit var notificationGroup: ConstraintLayout
    private lateinit var buttonLogout: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonFeedback: Button
    private lateinit var tokenManager: TokenManager

    private val ifLogin =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data?.getStringExtra("login")
                if (resultData == "enter") {
                    if (tokenManager.isEmail()) {
                        header.text = tokenManager.getLogin()
                        enteringButtons.visibility = View.GONE
                        buttonLogout.visibility = View.VISIBLE
                        buttonEdit.visibility = View.VISIBLE
                        buttonFeedback.visibility = View.VISIBLE
                        notificationGroup.visibility = View.VISIBLE
                        val eventParameters = mapOf("method" to "email")
                        AppMetrica.reportEvent("ProfileLogin", eventParameters)
                    } else if (tokenManager.isVk()) {
                        header.text = tokenManager.getName()
                        enteringButtons.visibility = View.GONE
                        buttonLogout.visibility = View.VISIBLE
                        buttonEdit.visibility = View.VISIBLE
                        buttonFeedback.visibility = View.VISIBLE
                        notificationGroup.visibility = View.VISIBLE
                        val eventParameters = mapOf("method" to "vk")
                        AppMetrica.reportEvent("ProfileLogin", eventParameters)
                    }
                    val resultIntent = Intent()
                    resultIntent.putExtra("login", "enter")
                    setResult(Activity.RESULT_OK, resultIntent)
                }
            }
        }

    private fun showConfirmationDialog(
        context: Context,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Выход")
            .setMessage("Точно выйти из аккаунта?")
            .setPositiveButton("Да") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                onCancel()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val editor = sharedPref.edit()
            editor.putBoolean("notif", true)
            editor.apply()
            AppMetrica.reportEvent("NotificationsEnable")
        } else {
            val editor = sharedPref.edit()
            editor.putBoolean("notif", false)
            editor.apply()
        }
    }

    override fun onResume() {
        super.onResume()
        if (tokenManager.isVk()) {
            header.text = tokenManager.getName()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)


        val button1: Button = findViewById(R.id.button_back)
        button1.setOnClickListener {
            finish()
        }


        val button2: Button = findViewById(R.id.button_confidentional)
        button2.setOnClickListener {
            val url = getString(R.string.url_conf)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        val button3: Button = findViewById(R.id.button_report)
        button3.setOnClickListener {
            val url = getString(R.string.url_user_report)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        val button4: Button = findViewById(R.id.button_map)
        button4.setOnClickListener {
            val url = getString(R.string.url_yandex_map)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)


        themeSwitch = findViewById(R.id.themeSwitch)
        themeSwitch.isChecked = isDarkTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("dark_theme", true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("dark_theme", false)
            }
            editor.apply()
        }

        val button5: Button = findViewById(R.id.button_theme)
        button5.setOnClickListener {
            themeSwitch.isChecked = !themeSwitch.isChecked
        }

        val isNorth = sharedPref.getBoolean("north_upper", false)
        northSwitch = findViewById(R.id.northSwitch)
        northSwitch.isChecked = isNorth

        northSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            if (isChecked) {
                editor.putBoolean("north_upper", true)
            } else {
                editor.putBoolean("north_upper", false)
            }
            editor.apply()
        }

        val button6: Button = findViewById(R.id.button_north)
        button6.setOnClickListener {
            northSwitch.isChecked = !northSwitch.isChecked
        }

        val isNotif = sharedPref.getBoolean("notif", false)
        notifSwitch = findViewById(R.id.notificationsSwitch)
        notifSwitch.isChecked = isNotif

        notifSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            if (isChecked) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            editor.putBoolean("notif", true)
                        }

                        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    editor.putBoolean("notif", true)
                    AppMetrica.reportEvent("NotificationsEnable")
                }
            } else {
                editor.putBoolean("notif", false)
            }
            editor.apply()
        }

        val notifButton: Button = findViewById(R.id.button_notifiactions)
        notifButton.setOnClickListener {
            notifSwitch.isChecked = !notifSwitch.isChecked
        }

        val button7: Button = findViewById(R.id.button_login)
        button7.setOnClickListener {
            val eventParameters = mapOf("method" to "login")
            AppMetrica.reportEvent("TryLogin", eventParameters)
            val intent = Intent(this, LoginActivity::class.java)
            ifLogin.launch(intent)
        }

        val button8: Button = findViewById(R.id.button_registration)
        button8.setOnClickListener {
            val eventParameters = mapOf("method" to "registration")
            AppMetrica.reportEvent("TryLogin", eventParameters)
            val intent = Intent(this, RegActivity::class.java)
            ifLogin.launch(intent)
        }

        tokenManager = TokenManager(this)

        buttonLogout = findViewById(R.id.button_logout)
        buttonEdit = findViewById(R.id.button_edit)
        buttonFeedback = findViewById(R.id.button_feedback)

        buttonFeedback.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            showConfirmationDialog(this,
                onConfirm = {
                    tokenManager.clearTokens()
                    val resultIntent = Intent()
                    resultIntent.putExtra("login", "logout")
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                },
                onCancel = {
                }
            )
        }

        buttonEdit.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }

        header = findViewById(R.id.settings_header)
        enteringButtons = findViewById(R.id.entering_buttons)
        notificationGroup = findViewById(R.id.notifiaction_group)

        if (tokenManager.isEmail()) {
            header.text = tokenManager.getLogin()
            enteringButtons.visibility = View.GONE
        } else if (tokenManager.isVk()) {
            header.text = tokenManager.getName()
            enteringButtons.visibility = View.GONE
        } else {
            buttonLogout.visibility = View.GONE
            buttonEdit.visibility = View.GONE
            buttonFeedback.visibility = View.GONE
            notificationGroup.visibility = View.GONE
        }


    }
}