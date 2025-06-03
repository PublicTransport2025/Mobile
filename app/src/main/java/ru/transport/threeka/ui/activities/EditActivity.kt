package ru.transport.threeka.ui.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.profile.Token
import ru.transport.threeka.services.TokenManager

class EditActivity : AppCompatActivity() {

    private fun generateRandomString(length: Int): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)

        val button1: Button = findViewById(R.id.button_back)
        button1.setOnClickListener {
            finish()
        }

        val inputName: TextInputEditText = findViewById(R.id.input_name)
        val tokenManager = TokenManager(this)
        inputName.setText(tokenManager.getName().toString())

        val textVK: TextView = findViewById(R.id.textVK)
        val textPass: TextView = findViewById(R.id.textPass)

        if (tokenManager.isVk()){
            textVK.visibility = View.VISIBLE
        }
        if (tokenManager.isEmail()){
            textPass.visibility = View.VISIBLE
        }

        val intentError = Intent(this, LoginErrorActivity::class.java)


        inputName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {

                if (inputName.text.toString().isBlank()) {
                    intentError.putExtra(
                        "error",
                        "Укажите свое имя или псевдоним. В дальнейшем эта информация может отображаться при создании событий"
                    )
                    startActivity(intentError)
                    return@setOnEditorActionListener false
                }


                val call = apiService.changeName(
                    tokenManager.getAccessToken()!!,
                    inputName.text.toString()
                )
                call.enqueue(object : Callback<Token> {
                    override fun onResponse(call: Call<Token>, response: Response<Token>) {
                        if (response.isSuccessful) {
                            val token = response.body()
                            if (token != null) {
                                tokenManager.saveToken(token)
                            }
                            runOnUiThread {
                                Toast.makeText(
                                    this@EditActivity,
                                    "Ваше имя обновлено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            runOnUiThread {
                                val message = when (response.code()) {
                                    422 -> "Еще раз проверьте заполненность всех полей"
                                    400 -> "Такой email уже зарегестрирован"
                                    403 -> "Этот аккаунт заблокирован"
                                    401 -> "Неверный логин или пароль"
                                    480 -> "Вы не запросили код подтверждения"
                                    481 -> "Код неверный, попробуйте еще раз"
                                    482 -> "Код подтверждения просрочен"
                                    500 -> "Приносим свои извенения, произошла ошибка на сервере"
                                    502 -> "Пожалуйста, попробуйте позднее"
                                    else -> "Произошла неизвестная ошибка"
                                }
                                intentError.putExtra("error", message)
                                startActivity(intentError)
                            }
                        }
                    }

                    override fun onFailure(call: Call<Token>, t: Throwable) {
                        runOnUiThread {
                            intentError.putExtra(
                                "error",
                                "Возникла проблема с интернет соединением"
                            )
                            startActivity(intentError)
                        }
                    }
                })

                true
            } else {
                false
            }
        }

        val buttonEnter: Button = findViewById(R.id.button_send)
        buttonEnter.setOnClickListener {

            if (inputName.text.toString().isBlank()) {
                intentError.putExtra(
                    "error",
                    "Укажите свое имя или псевдоним. В дальнейшем эта информация может отображаться при создании событий"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            val call =
                apiService.changeName(tokenManager.getAccessToken()!!, inputName.text.toString())
            call.enqueue(object : Callback<Token> {
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if (response.isSuccessful) {
                        val token = response.body()
                        if (token != null) {
                            tokenManager.saveToken(token)
                        }
                        runOnUiThread {
                            Toast.makeText(
                                this@EditActivity,
                                "Ваше имя обновлено",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        runOnUiThread {
                            val message = when (response.code()) {
                                422 -> "Еще раз проверьте заполненность всех полей"
                                400 -> "Такой email уже зарегестрирован"
                                403 -> "Этот аккаунт заблокирован"
                                401 -> "Неверный логин или пароль"
                                480 -> "Вы не запросили код подтверждения"
                                481 -> "Код неверный, попробуйте еще раз"
                                482 -> "Код подтверждения просрочен"
                                500 -> "Приносим свои извенения, произошла ошибка на сервере"
                                502 -> "Пожалуйста, попробуйте позднее"
                                else -> "Произошла неизвестная ошибка"
                            }
                            intentError.putExtra("error", message)
                            startActivity(intentError)
                        }
                    }
                }

                override fun onFailure(call: Call<Token>, t: Throwable) {
                    runOnUiThread {
                        intentError.putExtra("error", "Возникла проблема с интернет соединением")
                        startActivity(intentError)
                    }
                }
            })
        }

    }
}