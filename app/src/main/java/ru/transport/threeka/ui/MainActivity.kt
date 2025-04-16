package ru.transport.threeka.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

import ru.transport.threeka.R
import ru.transport.threeka.api.RetrofitClient
import ru.transport.threeka.api.schemas.Route
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.data.MainViewModel


class MainActivity : AppCompatActivity(), ErrorCallback {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var mapView: MapView

    private val placemarkTapListener = MapObjectTapListener { obj, _ ->
        //Toast.makeText(this@MainActivity,"${obj.userData}", Toast.LENGTH_SHORT).show()

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction().remove(existingFragment).commit()
        }

        val fragment = StopInfoFragment.newInstance(obj.userData as Int)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.setErrorCallback(this)

        MapKitFactory.initialize(this)
        mapView = findViewById(R.id.mapview)
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(51.68, 39.2),
                /* zoom = */ 12.0f,
                /* azimuth = */ 0.0f,
                /* tilt = */ 0.0f
            )
        )

        val style =
            "[{\"tags\": {\"any\": [\"poi\", \"transit\"]}, \"stylers\": {\"visibility\": \"off\"}}]";
        mapView.mapWindow.map.setMapStyle(style);

        val stopsIconsCollection = mapView.mapWindow.map.mapObjects.addCollection()
        val imageProvider = ImageProvider.fromResource(this, R.drawable.stop)

        stopsIconsCollection.addTapListener(placemarkTapListener)


        viewModel.loadStops()
        viewModel.stops.observe(this, Observer { stops ->
            for (i in stops.indices) {
                stopsIconsCollection.addPlacemark().apply {
                    geometry = Point(stops[i].coord.lat, stops[i].coord.lon)
                    userData = i
                    setIcon(imageProvider)
                }
            }
        })


        val myButton1: Button = findViewById(R.id.myButton1)
        val myButton2: Button = findViewById(R.id.myButton2)

        viewModel.stopFrom.observe(this, Observer { stop ->
            if (stop == null) {
                myButton1.text = "Откуда"
            } else {
                myButton1.text = stop.name
            }
        })

        viewModel.stopTo.observe(this, Observer { stop ->
            if (stop == null) {
                myButton2.text = "Куда"
            } else {
                myButton2.text = stop.name
            }
        })

        myButton1.setOnClickListener {
            // Действие при нажатии на кнопку
            Toast.makeText(this@MainActivity, "Кнопка нажата!", Toast.LENGTH_SHORT).show()
            viewModel.increment()
        }


        myButton2.setOnClickListener {
            if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                val fragment = NetworkErrorFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
            // Действие при нажатии на кнопку
            /*CoroutineScope(Dispatchers.Main).launch {
                val route = loadRoute()
                if (route != null) {
                    val points = ArrayList<Point>()
                    for (cat in route.stops) {
                        points.add(Point(cat.lat.toDouble(), cat.lon.toDouble()))
                    }
                    val polyline = Polyline(points)
                    val polylineObject = mapView.mapWindow.map.mapObjects.addPolyline(polyline)
                    polylineObject.apply {
                        strokeWidth = 4f
                        setStrokeColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.blue
                            )
                        )
                        outlineWidth = 1f
                        outlineColor = ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    }
                } else {
                    Log.e("Error", "Не удалось получить маршрут")
                }
            }*/
        }

    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }


    private suspend fun loadRoute(): Route? {
        return try {
            val response = RetrofitClient.apiService.getRoute().awaitResponse()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onError(exception: Exception) {
        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction().remove(existingFragment).commit()
        }
        val fragment = NetworkErrorFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }
}