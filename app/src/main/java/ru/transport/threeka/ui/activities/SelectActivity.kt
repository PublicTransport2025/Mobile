package ru.transport.threeka.ui.activities


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import ru.transport.threeka.R


class SelectActivity : AppCompatActivity() {

    private lateinit var adapter: SelectAdapter

    private lateinit var stops: ArrayList<String>
    private lateinit var stopsLat: DoubleArray
    private lateinit var stopsLon: DoubleArray

    val searchSessionListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val geoObjects = response.collection.children.mapNotNull { it.obj }
            for (place in geoObjects) {
                if (place.geometry[0].point != null
                    && place.geometry[0].point!!.longitude < 40
                    && place.geometry[0].point!!.longitude > 38.9
                    && place.geometry[0].point!!.latitude > 51.5
                    && place.geometry[0].point!!.latitude < 52) {
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

        val searchBar: TextInputLayout = findViewById(R.id.outlinedTextField)
        searchBar.setStartIconOnClickListener {
            finish()
        }

        val inputBar: TextInputEditText = findViewById(R.id.inputTextField)
        inputBar.hint = intent.getStringExtra("label")
        inputBar.setOnEditorActionListener { v, actionId, event ->
            true
        }

        inputBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().trim()
                if (text.isNotEmpty()) {
                    var k = 0;
                    val rez: MutableList<String> = mutableListOf()
                    val rez_ids: MutableList<Int> = mutableListOf()

                    for (i in stops.indices) {
                        if (stops[i].contains(text, ignoreCase = true)) {
                            k++
                            Log.d("Stops Finding", stops[i])
                            rez.add(stops[i])
                            rez_ids.add(i)
                            if (k == 10) {
                                break
                            }
                        }
                    }
                    adapter.addButton(rez, rez_ids)
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


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SelectAdapter(this, mutableListOf(), mutableListOf(), mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


    }
}


