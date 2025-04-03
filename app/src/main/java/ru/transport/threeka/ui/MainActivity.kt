package ru.transport.threeka.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    private val placemarkTapListener = MapObjectTapListener { obj, _ ->
        Toast.makeText(
            this@MainActivity,
            "${obj.userData}",
            Toast.LENGTH_SHORT
        ).show()
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

        CoroutineScope(Dispatchers.Main).launch {
            val stops = loadStops()
            if (stops != null) {
                for (cat in stops) {
                    stopsIconsCollection.addPlacemark().apply {
                        geometry = Point(cat.coord.lat.toDouble(), cat.coord.lon.toDouble())
                        userData = cat.name
                        setIcon(imageProvider)
                    }
                }
            } else {
                Log.e("Error", "Не удалось получить список остановок")
            }
        }

        val myButton1: Button = findViewById(R.id.myButton1)
        val myButton2: Button = findViewById(R.id.myButton2)
        myButton1.setOnClickListener {
            // Действие при нажатии на кнопку
            Toast.makeText(this@MainActivity, "Кнопка нажата!", Toast.LENGTH_SHORT).show()
        }


        myButton2.setOnClickListener {
            // Действие при нажатии на кнопку
            CoroutineScope(Dispatchers.Main).launch {
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
            }
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

    private suspend fun loadStops(): List<Stop>? {
        return try {
            val response = RetrofitClient.apiService.getStops().awaitResponse()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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
}