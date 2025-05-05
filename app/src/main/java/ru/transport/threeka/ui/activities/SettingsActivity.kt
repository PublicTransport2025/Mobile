package ru.transport.threeka.ui.activities


import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.materialswitch.MaterialSwitch
import ru.transport.threeka.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: MaterialSwitch
    private lateinit var northSwitch: MaterialSwitch
    private lateinit var sharedPref: SharedPreferences


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

        val button7: Button = findViewById(R.id.button_login)
        button7.setOnClickListener {
            Toast.makeText(this@SettingsActivity, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        val button8: Button = findViewById(R.id.button_registration)
        button8.setOnClickListener {
            Toast.makeText(this@SettingsActivity, "Coming soon", Toast.LENGTH_SHORT).show()
        }


    }
}