package ru.transport.threeka.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.yandex.mapkit.Animation


import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.ui.ErrorCallback
import ru.transport.threeka.ui.fragments.DoubleRouteFragment
import ru.transport.threeka.ui.fragments.NetworkErrorFragment
import ru.transport.threeka.ui.fragments.NoRouteFragment
import ru.transport.threeka.ui.fragments.SimpleRouteFragment
import ru.transport.threeka.ui.fragments.StopInfoFragment


class MainActivity : AppCompatActivity(), ErrorCallback {

    private val viewModel: MainViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    private lateinit var sharedPref: SharedPreferences

    private lateinit var mapView: MapView
    private var polylineObject: MapObject? = null
    private var polylineObject2: MapObject? = null


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

    private var stopsIconsCollection: MapObjectCollection? = null

    private val cameraListener = CameraListener { _, newPosition, _, _ ->
        if (newPosition.zoom > 13.9) {
            stopsIconsCollection?.setVisible(true)
        } else {
            stopsIconsCollection?.setVisible(false)
        }
    }

    private val selectStopFrom =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data?.getIntExtra("stop_id", -1)
                if (resultData != null) {
                    if (resultData > 0) {
                        viewModel.setStopFrom(resultData)
                        mapView.mapWindow.map.move(
                            CameraPosition(
                                Point(
                                    viewModel.stopFrom.value?.coord?.lat!!,
                                    viewModel.stopFrom.value?.coord?.lon!!
                                ),
                                /* zoom = */ 16.0f,
                                /* azimuth = */ 0.0f,
                                /* tilt = */ 0.0f
                            ),
                            Animation(Animation.Type.SMOOTH, 0.5f),
                            null
                        )
                    }
                }
            }
        }

    private val selectStopTo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data?.getIntExtra("stop_id", -1)
                if (resultData != null) {
                    if (resultData > 0) {
                        viewModel.setStopTo(resultData)
                        mapView.mapWindow.map.move(
                            CameraPosition(
                                Point(
                                    viewModel.stopTo.value?.coord?.lat!!,
                                    viewModel.stopTo.value?.coord?.lon!!
                                ),
                                /* zoom = */ 16.0f,
                                /* azimuth = */ 0.0f,
                                /* tilt = */ 0.0f
                            ),
                            Animation(Animation.Type.SMOOTH, 0.5f),
                            null
                        )
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)
        val isNorth = sharedPref.getBoolean("north_upper", false)

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.setErrorCallback(this)
        viewModel.setCare(sharedPref.getBoolean("care", false))
        viewModel.setChange(sharedPref.getBoolean("change", false))
        viewModel.setPriority(sharedPref.getInt("priority", 0))

        MapKitFactory.initialize(this)
        mapView = findViewById(R.id.mapview)
        mapView.mapWindow.map.isNightModeEnabled = isDarkTheme
        mapView.mapWindow.map.isRotateGesturesEnabled = !isNorth
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(51.68, 39.2),
                /* zoom = */ 12.0f,
                /* azimuth = */ 0.0f,
                /* tilt = */ 0.0f
            )
        )
        Toast.makeText(
            this@MainActivity,
            "Увеличьте масштаб карты, чтобы отобразить остановки",
            Toast.LENGTH_SHORT
        ).show()

        val style =
            "[{\"tags\": {\"any\": [\"poi\", \"transit\"]}, \"stylers\": {\"visibility\": \"off\"}}]";
        mapView.mapWindow.map.setMapStyle(style);

        stopsIconsCollection = mapView.mapWindow.map.mapObjects.addCollection()
        val imageProvider = ImageProvider.fromResource(this, R.drawable.stop)

        stopsIconsCollection!!.addTapListener(placemarkTapListener)


        viewModel.loadStops()
        viewModel.stops.observe(this, Observer { stops ->
            for (i in stops.indices) {
                stopsIconsCollection!!.addPlacemark().apply {
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

        viewModel.activeRoute.observe(this, Observer { route ->
            if (route > 0) {
                val existingFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (existingFragment != null) {
                    supportFragmentManager.beginTransaction().remove(existingFragment).commit()
                }
                if (viewModel.isSimple()) {
                    val fragment = SimpleRouteFragment()
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit()
                } else {
                    val fragment = DoubleRouteFragment()
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit()
                }

                val coords = viewModel.getRouteCoords()
                val points = ArrayList<Point>()
                for (coord in coords) {
                    points.add(Point(coord.lat, coord.lon))
                }
                val polyline = Polyline(points)
                if (polylineObject != null) {
                    mapView.mapWindow.map.mapObjects.remove(polylineObject!!)
                    polylineObject = null
                }
                if (polylineObject2 != null) {
                    mapView.mapWindow.map.mapObjects.remove(polylineObject2!!)
                    polylineObject2 = null
                }

                val load = viewModel.getRouteLoad()
                val color = when (load) {
                    1 -> ContextCompat.getColor(this@MainActivity, R.color.load1)
                    2 -> ContextCompat.getColor(this@MainActivity, R.color.load2)
                    3 -> ContextCompat.getColor(this@MainActivity, R.color.load3)
                    4 -> ContextCompat.getColor(this@MainActivity, R.color.load4)
                    5 -> ContextCompat.getColor(this@MainActivity, R.color.load5)
                    else -> ContextCompat.getColor(this@MainActivity, R.color.load0)
                }

                polylineObject = mapView.mapWindow.map.mapObjects.addPolyline(polyline).apply {
                    strokeWidth = 6f
                    setStrokeColor(color)
                    outlineWidth = 2f
                    outlineColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                }

                var geometry = Geometry.fromPolyline(polyline)

                if (!viewModel.isSimple()) {
                    val coords2 = viewModel.getRouteCoordsDouble()
                    val points2 = ArrayList<Point>()
                    for (coord in coords2) {
                        points2.add(Point(coord.lat, coord.lon))
                    }
                    val polyline2 = Polyline(points2)

                    val load2 = viewModel.getRouteLoadDouble()
                    val color2 = when (load2) {
                        1 -> ContextCompat.getColor(this@MainActivity, R.color.load1)
                        2 -> ContextCompat.getColor(this@MainActivity, R.color.load2)
                        3 -> ContextCompat.getColor(this@MainActivity, R.color.load3)
                        4 -> ContextCompat.getColor(this@MainActivity, R.color.load4)
                        5 -> ContextCompat.getColor(this@MainActivity, R.color.load5)
                        else -> ContextCompat.getColor(this@MainActivity, R.color.load0)
                    }

                    polylineObject2 =
                        mapView.mapWindow.map.mapObjects.addPolyline(polyline2).apply {
                            strokeWidth = 6f
                            setStrokeColor(color2)
                            outlineWidth = 2f
                            outlineColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                        }
                    val pointsFull = ArrayList<Point>()
                    for (coord in coords) {
                        pointsFull.add(Point(coord.lat, coord.lon))
                    }
                    for (coord in coords2) {
                        pointsFull.add(Point(coord.lat, coord.lon))
                    }
                    val polylineFull = Polyline(pointsFull)
                    geometry = Geometry.fromPolyline(polylineFull)

                }



                mapView.mapWindow.focusRect = ScreenRect(
                    ScreenPoint(
                        /* x = */ mapView.mapWindow.width().toFloat() * 0.05F,
                        /* y = */ mapView.mapWindow.height().toFloat() * 0.05F,
                    ),
                    ScreenPoint(
                        /* x = */ mapView.mapWindow.width().toFloat() * 0.95F,
                        /* y = */ mapView.mapWindow.height().toFloat() * 0.5F,
                    )
                )

                mapView.mapWindow.map.move(
                    mapView.mapWindow.map.cameraPosition(geometry),
                    Animation(Animation.Type.SMOOTH, 0.5f), // Анимация перемещения
                    null
                )


            }
            if (route <= 0) {
                val existingFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (existingFragment != null) {
                    supportFragmentManager.beginTransaction().remove(existingFragment).commit()
                }
                if (polylineObject != null) {
                    mapView.mapWindow.map.mapObjects.remove(polylineObject!!)
                    polylineObject = null
                }
                if (polylineObject2 != null) {
                    mapView.mapWindow.map.mapObjects.remove(polylineObject2!!)
                    polylineObject2 = null
                }
            }

            if (route < 0) {
                val existingFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (existingFragment != null) {
                    supportFragmentManager.beginTransaction().remove(existingFragment).commit()
                }
                val fragment = NoRouteFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        })


        mapView.mapWindow.map.addCameraListener(cameraListener)


        myButton1.setOnClickListener {
            val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("label", "Откуда")
            intent.putExtra("stops", viewModel.getStops())
            intent.putExtra("stops_lat", viewModel.getStopsLat())
            intent.putExtra("stops_lon", viewModel.getStopsLon())
            selectStopFrom.launch(intent)
        }


        myButton2.setOnClickListener {
            val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("label", "Куда")
            intent.putExtra("stops", viewModel.getStops())
            intent.putExtra("stops_lat", viewModel.getStopsLat())
            intent.putExtra("stops_lon", viewModel.getStopsLon())
            selectStopTo.launch(intent)
        }

        val settingsButton: ImageButton = findViewById(R.id.button_setting)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val filterButton: ImageButton = findViewById(R.id.button_filter)
        filterButton.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            startActivity(intent)
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

    private var isActive = false

    override fun onResume() {
        super.onResume()
        isActive = true

        viewModel.setCare(sharedPref.getBoolean("care", false))
        viewModel.setChange(sharedPref.getBoolean("change", false))
        viewModel.setPriority(sharedPref.getInt("priority", 0))

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        val isNorth = sharedPref.getBoolean("north_upper", false)
        if (mapView.mapWindow.map.isRotateGesturesEnabled && isNorth) {
            mapView.mapWindow.map.isRotateGesturesEnabled = false
            mapView.mapWindow.map.move(
                CameraPosition(
                    Point(51.68, 39.2),
                    /* zoom = */ 12.0f,
                    /* azimuth = */ 0.0f,
                    /* tilt = */ 0.0f
                )
            )
        }
        if (!mapView.mapWindow.map.isRotateGesturesEnabled && !isNorth) {
            mapView.mapWindow.map.isRotateGesturesEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    fun isActivityActive(): Boolean {
        return isActive
    }


    override fun onError(exception: Exception) {
        if (!isActivityActive()) {
            return
        }
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