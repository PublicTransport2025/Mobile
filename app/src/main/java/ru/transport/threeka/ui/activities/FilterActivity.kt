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

class FilterActivity : AppCompatActivity() {

    private lateinit var careSwitch: MaterialSwitch
    private lateinit var changeSwitch: MaterialSwitch
    private lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_filter)
        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)


        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        val buttonApply: Button = findViewById(R.id.button_apply)
        buttonApply.setOnClickListener {
            finish()
        }

        val isCare = sharedPref.getBoolean("care", false)
        careSwitch = findViewById(R.id.switch_care)
        careSwitch.isChecked = isCare
        careSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            if (isChecked) {
                editor.putBoolean("care", true)
            } else {
                editor.putBoolean("care", false)
            }
            editor.apply()
        }
        val careButton: Button = findViewById(R.id.button_care)
        careButton.setOnClickListener {
            careSwitch.isChecked = !careSwitch.isChecked
        }

        val isChange = sharedPref.getBoolean("change", false)
        changeSwitch = findViewById(R.id.switch_change)
        changeSwitch.isChecked = isChange
        changeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            if (isChecked) {
                editor.putBoolean("change", true)
            } else {
                editor.putBoolean("change", false)
            }
            editor.apply()
        }
        val changeButton: Button = findViewById(R.id.button_change)
        changeButton.setOnClickListener {
            changeSwitch.isChecked = !changeSwitch.isChecked
        }

        val priorityButton: Button = findViewById(R.id.button_priority)
        val priority = sharedPref.getInt("priority", 0)
        when (priority) {
            0 -> priorityButton.text = "Приоритет: Загруженность"
            1 -> priorityButton.text = "Приоритет: Время"
            2 -> priorityButton.text = "Приоритет: Баланс"
        }
        priorityButton.setOnClickListener {
            val editor = sharedPref.edit()
            if (priorityButton.text == "Приоритет: Загруженность"){
                priorityButton.text = "Приоритет: Время"
                editor.putInt("priority", 1)
                editor.apply()
                return@setOnClickListener
            }
            if (priorityButton.text == "Приоритет: Время"){
                priorityButton.text = "Приоритет: Баланс"
                editor.putInt("priority", 2)
                editor.apply()
                return@setOnClickListener
            }
            if (priorityButton.text == "Приоритет: Баланс"){
                priorityButton.text = "Приоритет: Загруженность"
                editor.putInt("priority", 0)
                editor.apply()
                return@setOnClickListener
            }
        }
    }
}