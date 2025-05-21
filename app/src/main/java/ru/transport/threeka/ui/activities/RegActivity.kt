package ru.transport.threeka.ui.activities


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.vk.id.auth.VKIDAuthUiParams
import com.vk.id.onetap.xml.OneTap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.Reg
import ru.transport.threeka.api.schemas.Token
import ru.transport.threeka.api.schemas.VKLogin
import ru.transport.threeka.services.TokenManager
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

class RegActivity : AppCompatActivity() {

    private fun generateRandomString(length: Int): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reg)

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

        val inputName: TextInputEditText = findViewById(R.id.input_name)
        val inputLogin: TextInputEditText = findViewById(R.id.input_login)
        val inputPass: TextInputEditText = findViewById(R.id.input_pass)
        val inputPassDouble: TextInputEditText = findViewById(R.id.input_pass_double)
        val isAgree: CheckBox = findViewById(R.id.ok_confidentional)
        val tokenManager = TokenManager(this)
        val intentError = Intent(this, LoginErrorActivity::class.java)


        inputPassDouble.setOnEditorActionListener { v, actionId, event ->
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

                if (!(!inputLogin.text.toString()
                        .isBlank() && Patterns.EMAIL_ADDRESS.matcher(inputLogin.text.toString())
                        .matches())
                ) {
                    intentError.putExtra(
                        "error",
                        "Укажите корректный email-адрес, иначе не получится восстановить пароль"
                    )
                    startActivity(intentError)
                    return@setOnEditorActionListener false
                }

                if (inputPass.text.toString().isBlank() || inputPass.text.toString().length < 5) {
                    intentError.putExtra(
                        "error",
                        "Придумайте сложный пароль"
                    )
                    startActivity(intentError)
                    return@setOnEditorActionListener false
                }

                if (inputPass.text.toString() != inputPassDouble.text.toString()) {
                    intentError.putExtra(
                        "error",
                        "Пароли не совпадают"
                    )
                    startActivity(intentError)
                    return@setOnEditorActionListener false
                }

                if (!isAgree.isChecked) {
                    intentError.putExtra(
                        "error",
                        "Ознакомтесь с пользовательским соглашением. В противном случае откажитесь от регистрации аккаунта"
                    )
                    startActivity(intentError)
                    return@setOnEditorActionListener false
                }

                val reg = Reg(
                    name = inputName.text.toString(),
                    email = inputLogin.text.toString(),
                    password = inputPass.text.toString(),
                )

                val call = apiService.signup(reg)
                call.enqueue(object : Callback<Token> {
                    override fun onResponse(call: Call<Token>, response: Response<Token>) {
                        if (response.isSuccessful) {
                            val token = response.body()
                            if (token != null) {
                                tokenManager.saveToken(token)
                            }
                            runOnUiThread {
                                val resultIntent = Intent()
                                resultIntent.putExtra("login", "enter")
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                val message = when (response.code()) {
                                    422 -> "Еще раз проверьте заполненность всех полей"
                                    400 -> "Такой email уже зарегестрирован"
                                    403 -> "Этот аккаунт уже зарегестрирован"
                                    401 -> "Неверный логин или пароль"
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

        val buttonEnter: Button = findViewById(R.id.button_enter)
        buttonEnter.setOnClickListener {

            if (inputName.text.toString().isBlank()) {
                intentError.putExtra(
                    "error",
                    "Укажите свое имя или псевдоним. В дальнейшем эта информация может отображаться при создании событий"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (!(!inputLogin.text.toString()
                    .isBlank() && Patterns.EMAIL_ADDRESS.matcher(inputLogin.text.toString())
                    .matches())
            ) {
                intentError.putExtra(
                    "error",
                    "Укажите корректный email-адрес, иначе не получится восстановить пароль"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (inputPass.text.toString().isBlank() || inputPass.text.toString().length < 5) {
                intentError.putExtra(
                    "error",
                    "Придумайте сложный пароль"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (inputPass.text.toString() != inputPassDouble.text.toString()) {
                intentError.putExtra(
                    "error",
                    "Пароли не совпадают"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (!isAgree.isChecked) {
                intentError.putExtra(
                    "error",
                    "Ознакомтесь с пользовательским соглашением. В противном случае откажитесь от регистрации аккаунта"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            val reg = Reg(
                name = inputName.text.toString(),
                email = inputLogin.text.toString(),
                password = inputPass.text.toString(),
            )

            val call = apiService.signup(reg)
            call.enqueue(object : Callback<Token> {
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if (response.isSuccessful) {
                        val token = response.body()
                        if (token != null) {
                            tokenManager.saveToken(token)
                        }
                        runOnUiThread {
                            val resultIntent = Intent()
                            resultIntent.putExtra("login", "enter")
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            val message = when (response.code()) {
                                422 -> "Еще раз проверьте заполненность всех полей"
                                400 -> "Такой email уже зарегестрирован"
                                403 -> "Этот аккаунт уже зарегестрирован"
                                401 -> "Неверный логин или пароль"
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


        val vkButton = findViewById<OneTap>(R.id.vkidButton)

        val length = Random.nextInt(43, 129)
        val _randomString = generateRandomString(length)

        val S256Digester = MessageDigest.getInstance("SHA-256")
        val input = _randomString.toByteArray(Charset.forName("ISO_8859_1"))
        S256Digester.update(input)
        val digestBytes = S256Digester.digest()
        val _codeChallenge = Base64.encodeToString(
            digestBytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        // Преобразуем строку в SHA-256

        val _state = UUID.randomUUID().toString()
        vkButton.authParams = VKIDAuthUiParams { codeChallenge = _codeChallenge; state = _state }

        findViewById<OneTap>(R.id.vkidButton).setCallbacks(
            onAuth = { _, _ -> },
            onFail = { _, _ ->
                runOnUiThread {
                    intentError.putExtra(
                        "error",
                        "Авторизация через ВК сейчас недоступна. Пожалуйста, воспользуйтесь другим способом"
                    )
                    startActivity(intentError)
                }
            },
            onAuthCode = { data, isCompletion ->
                Log.i("VK_AUTH", isCompletion.toString())
                val vkLogin = VKLogin(
                    code_verifier = _randomString,
                    state = _state,
                    code = data.code,
                    device_id = data.deviceId
                )
                val call = apiService.loginWithVK(vkLogin)
                call.enqueue(object : Callback<Token> {
                    override fun onResponse(call: Call<Token>, response: Response<Token>) {
                        if (response.isSuccessful) {
                            val token = response.body()
                            if (token != null) {
                                tokenManager.saveToken(token)
                            }
                            runOnUiThread {
                                val resultIntent = Intent()
                                resultIntent.putExtra("login", "enter")
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                val message = when (response.code()) {
                                    422 -> "Еще раз проверьте заполненность всех полей"
                                    400 -> "Такой email уже зарегестрирован"
                                    403 -> "Этот аккаунт заблокирован"
                                    401 -> "Неверный логин или пароль"
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
            }
        )

    }
}