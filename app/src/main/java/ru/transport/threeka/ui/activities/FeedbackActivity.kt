package ru.transport.threeka.ui.activities


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.profile.ResponseMessage
import ru.transport.threeka.api.schemas.profile.Feedback
import ru.transport.threeka.services.TokenManager

class FeedbackActivity : AppCompatActivity() {

    private lateinit var stars: Array<ImageView>
    var mark: Int = 8;

    private fun showConfirmationDialog(
        context: Context,
        onConfirm: () -> Unit,
    ) {
        AlertDialog.Builder(context)
            .setTitle("Спасибо!")
            .setMessage("Ваш отзыв сохранен")
            .setPositiveButton("Ок") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setRating(newRating: Int) {
        mark = newRating
        for (i in stars.indices) {
            if (i < newRating) {
                stars[i].setImageResource(R.drawable.full)
            } else {
                stars[i].setImageResource(R.drawable.empty)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        stars = arrayOf(
            findViewById(R.id.mark1),
            findViewById(R.id.mark2),
            findViewById(R.id.mark3),
            findViewById(R.id.mark4),
            findViewById(R.id.mark5),
            findViewById(R.id.mark6),
            findViewById(R.id.mark7),
            findViewById(R.id.mark8),
            findViewById(R.id.mark9),
            findViewById(R.id.mark10)
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                setRating(index + 1)
            }
        }

        setRating(8)

        val tokenManager = TokenManager(this)
        val intentError = Intent(this, LoginErrorActivity::class.java)

        val inputName: TextInputEditText = findViewById(R.id.input_name)
        val inputEmail: TextInputEditText = findViewById(R.id.input_login)
        val inputAbout: TextInputEditText = findViewById(R.id.input_about)

        val buttonEnter: Button = findViewById(R.id.button_enter)
        buttonEnter.setOnClickListener {

            if (inputName.text.toString().isBlank()) {
                intentError.putExtra(
                    "error",
                    "Пожалуйста, укажите как к Вам обращаться"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (!(!inputEmail.text.toString()
                    .isBlank() && Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString())
                    .matches())
            ) {
                intentError.putExtra(
                    "error",
                    "Неверный формат электронной почты"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            if (inputAbout.text.toString().isBlank()) {
                intentError.putExtra(
                    "error",
                    "Пожалуйста, заполните текст содержания отзыва"
                )
                startActivity(intentError)
                return@setOnClickListener
            }

            val feedback = Feedback(
                name = inputName.text.toString(),
                email = inputEmail.text.toString(),
                mark = mark,
                about = inputAbout.text.toString()
            )

            val call = apiService.writeFeedback(tokenManager.getAccessToken()!!, feedback)
            call.enqueue(object : Callback<ResponseMessage> {
                override fun onResponse(
                    call: Call<ResponseMessage>,
                    response: Response<ResponseMessage>
                ) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            showConfirmationDialog(this@FeedbackActivity,
                                onConfirm = {
                                    finish()
                                }
                            )
                        }
                    } else {
                        runOnUiThread {
                            val message = when (response.code()) {
                                422 -> "Еще раз проверьте заполненность всех полей"
                                502 -> "Пожалуйста, попробуйте позднее"
                                else -> "Произошла неизвестная ошибка"
                            }
                            intentError.putExtra("error", message)
                            startActivity(intentError)
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseMessage>, t: Throwable) {
                    runOnUiThread {
                        intentError.putExtra("error", "Возникла проблема с интернет соединением")
                        startActivity(intentError)
                    }
                }
            })
        }

    }
}