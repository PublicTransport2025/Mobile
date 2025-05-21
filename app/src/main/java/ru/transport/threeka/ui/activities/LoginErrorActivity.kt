package ru.transport.threeka.ui.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import ru.transport.threeka.R


class LoginErrorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_error)

        val button1: Button = findViewById(R.id.button_back)
        button1.setOnClickListener {
            finish()
        }

        val button2: Button = findViewById(R.id.button_back2)
        button2.setOnClickListener {
            finish()
        }

        val inputBar: TextView = findViewById(R.id.textError)
        inputBar.text = intent.getStringExtra("error")



    }
}