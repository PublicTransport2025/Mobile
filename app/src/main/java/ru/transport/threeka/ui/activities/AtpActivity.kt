package ru.transport.threeka.ui.activities


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.navigation.Atp

class AtpActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_atp)

        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        val label: TextView = findViewById(R.id.textAtp)
        val phone: TextView = findViewById(R.id.phoneTextView)
        val report: TextView = findViewById(R.id.emailTextView)

        val number = intent.getStringExtra("number")!!

        val call = apiService.getAtp(number)
        call.enqueue(object : Callback<Atp> {
            override fun onResponse(call: Call<Atp>, response: Response<Atp>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    // Обновляем UI с полученными данными
                    runOnUiThread {
                        label.text = data?.title
                        phone.text = data?.phone
                        report.text = data?.report
                    }
                } else {
                    runOnUiThread {
                        label.visibility = View.GONE
                        phone.visibility = View.GONE
                        report.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<Atp>, t: Throwable) {
                runOnUiThread {
                    label.visibility = View.GONE
                    phone.visibility = View.GONE
                    report.visibility = View.GONE
                }
            }
        })

    }
}