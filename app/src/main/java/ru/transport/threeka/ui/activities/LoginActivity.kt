package ru.transport.threeka.ui.activities


import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.vk.id.auth.VKIDAuthUiParams
import com.vk.id.onetap.xml.OneTap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.ResponseData
import ru.transport.threeka.api.schemas.Token
import ru.transport.threeka.api.schemas.VKLogin
import ru.transport.threeka.services.TokenManager
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private fun generateRandomString(length: Int): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)


        val inputLogin: TextInputEditText = findViewById(R.id.input_login)
        val inputPass: TextInputEditText = findViewById(R.id.input_pass)
        val tokenManager = TokenManager(this)

        val buttonEnter: Button = findViewById(R.id.button_enter)
        buttonEnter.setOnClickListener {
            val call = apiService.login(inputLogin.text.toString(), inputPass.text.toString())
            call.enqueue(object : Callback<Token> {
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if (response.isSuccessful) {
                        val token = response.body()
                        if (token != null) {
                            tokenManager.saveToken(token)
                        }
                        runOnUiThread {

                        }
                    } else {
                        runOnUiThread {

                        }
                    }
                }

                override fun onFailure(call: Call<Token>, t: Throwable) {
                    runOnUiThread {

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
            onAuthCode = { data, isCompletion ->
                Log.i("VK_AUTH", isCompletion.toString())
                val vkLogin = VKLogin(
                    code_verifier = _randomString,
                    state = _state,
                    code = data.code,
                    device_id = data.deviceId
                )
                RetrofitClient.apiService.loginWithVK(vkLogin).enqueue(object :
                    Callback<ResponseData> {
                    override fun onResponse(
                        call: Call<ResponseData>,
                        response: Response<ResponseData>
                    ) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            println("Response: ${responseData?.message}")
                            Toast.makeText(
                                this@LoginActivity,
                                "${responseData?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            println("Error: ${response.code()} - ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                        println("Failure: ${t.message}")
                    }
                })
            }
        )

    }
}