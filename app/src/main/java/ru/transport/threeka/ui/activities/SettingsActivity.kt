package ru.transport.threeka.ui.activities


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.transport.threeka.R

class SettingsActivity : AppCompatActivity() {
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
    }
}