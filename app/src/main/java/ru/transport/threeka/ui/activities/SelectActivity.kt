package ru.transport.threeka.ui.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import ru.transport.threeka.R
import java.util.Calendar


class SelectActivity : AppCompatActivity() {

    private lateinit var adapter: SelectAdapter

    private lateinit var stops: ArrayList<String>
    private lateinit var stopsLat: DoubleArray
    private lateinit var stopsLon: DoubleArray
    private lateinit var stopsLike: BooleanArray
    private var timestart = -1

    val searchSessionListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val geoObjects = response.collection.children.mapNotNull { it.obj }
            for (place in geoObjects) {
                if (place.geometry[0].point != null
                    && place.geometry[0].point!!.longitude < 40
                    && place.geometry[0].point!!.longitude > 38.9
                    && place.geometry[0].point!!.latitude > 51.5
                    && place.geometry[0].point!!.latitude < 52
                ) {
                    var delta = 9999.0
                    var min_id = -1
                    for (i in stops.indices) {
                        if ((stopsLat[i] - place.geometry[0].point!!.latitude)
                            * (stopsLat[i] - place.geometry[0].point!!.latitude)
                            + (stopsLon[i] - place.geometry[0].point!!.longitude)
                            * (stopsLon[i] - place.geometry[0].point!!.longitude) < delta
                        ) {
                            min_id = i
                            delta = ((stopsLat[i] - place.geometry[0].point!!.latitude)
                                    * (stopsLat[i] - place.geometry[0].point!!.latitude)
                                    + (stopsLon[i] - place.geometry[0].point!!.longitude)
                                    * (stopsLon[i] - place.geometry[0].point!!.longitude))
                        }
                    }
                    if (min_id >= 0) {
                        adapter.insertButton(
                            stops[min_id] + ":  " + (place.name ?: "Неизвестное место"),
                            min_id
                        )
                    }
                }
            }
        }

        override fun onSearchError(p0: com.yandex.runtime.Error) {
            // Handle search error
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SelectAdapter(this, mutableListOf(), mutableListOf(), mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val buttonTime: Button = findViewById(R.id.button_time)
        timestart = intent.getIntExtra("time", -1)
        if (timestart == -1) {
            buttonTime.text = "Время отправления"
        } else {
            buttonTime.text = String.format("Отправление в %d:%02d", timestart / 60, timestart % 60)
            val resultIntent = Intent()
            resultIntent.putExtra("time", timestart)
            setResult(Activity.RESULT_OK, resultIntent)
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Время отправления")
                .setInputMode(INPUT_MODE_CLOCK)
                .build()


        picker.addOnNegativeButtonClickListener {
            buttonTime.text = "Отправление сейчас"
            timestart = -1
            val resultIntent = Intent()
            resultIntent.putExtra("time", timestart)
            setResult(Activity.RESULT_OK, resultIntent)
        }

        picker.addOnPositiveButtonClickListener {
            val h = picker.hour
            val m = picker.minute
            buttonTime.text = String.format("Отправление в %d:%02d", h, m)
            timestart = 60 * h + m
            val resultIntent = Intent()
            resultIntent.putExtra("time", timestart)
            setResult(Activity.RESULT_OK, resultIntent)
        }


        buttonTime.setOnClickListener {
            picker.show(getSupportFragmentManager(), "starttime")
        }


        val boundaryPoints = listOf(
            Point(51.5, 39.0),
            Point(51.5, 41.7),
            Point(50.9, 41.2),
            Point(50.8, 40.5),
            Point(51.0, 39.5),
            Point(51.5, 39.0)
        )

        val pointsFull = ArrayList<Point>()
        for (point in boundaryPoints) {
            pointsFull.add(point)
        }
        val polylineFull = Polyline(pointsFull)
        val geometry = Geometry.fromPolyline(polylineFull)

        stops = intent.getStringArrayListExtra("stops")!!
        stopsLat = intent.getDoubleArrayExtra("stops_lat")!!
        stopsLon = intent.getDoubleArrayExtra("stops_lon")!!
        stopsLike = intent.getBooleanArrayExtra("stops_like")!!


        val searchBar: TextInputLayout = findViewById(R.id.outlinedTextField)
        searchBar.setStartIconOnClickListener {
            finish()
        }

        val inputBar: TextInputEditText = findViewById(R.id.inputTextField)
        inputBar.hint = intent.getStringExtra("label")
        inputBar.setOnEditorActionListener { v, actionId, event ->
            true
        }

        val rez: MutableList<String> = mutableListOf()
        val rez_ids: MutableList<Int> = mutableListOf()
        val rez_likes: MutableList<Boolean> = mutableListOf()
        for (i in stops.indices) {
            if (stopsLike[i]) {
                rez.add(stops[i])
                rez_ids.add(i)
                rez_likes.add(true)
            }
        }
        adapter.addButton(rez, rez_ids, rez_likes)

        inputBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().trim()
                if (text.isNotEmpty()) {
                    var k = 0;
                    rez.clear()
                    rez_ids.clear()
                    rez_likes.clear()
                    for (i in stops.indices) {
                        if (stops[i].contains(text, ignoreCase = true)) {
                            k++
                            if (stopsLike[i]) {
                                rez.add(0, stops[i])
                                rez_ids.add(0, i)
                                rez_likes.add(0, true)
                            } else if (k <= 10) {
                                rez.add(stops[i])
                                rez_ids.add(i)
                                rez_likes.add(false)
                            }
                        }
                    }
                    adapter.addButton(rez, rez_ids, rez_likes)
                } else {
                    rez.clear()
                    rez_ids.clear()
                    rez_likes.clear()
                    for (i in stops.indices) {
                        if (stopsLike[i]) {
                            rez.add(stops[i])
                            rez_ids.add(i)
                            rez_likes.add(true)
                        }
                    }
                    adapter.addButton(rez, rez_ids, rez_likes)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val enteredText = s.toString()

                val searchManager = SearchFactory.getInstance().createSearchManager(
                    SearchManagerType.COMBINED
                )
                val searchOptions = SearchOptions().apply {
                    resultPageSize = 5
                }

                val session = searchManager.submit(
                    enteredText,
                    geometry,
                    searchOptions,
                    searchSessionListener,
                )


            }
        })





    }
}


