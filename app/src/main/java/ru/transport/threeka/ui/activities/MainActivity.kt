package ru.transport.threeka.ui.activities


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import io.appmetrica.analytics.AppMetrica
import ru.transport.threeka.R
import ru.transport.threeka.data.MainViewModel
import ru.transport.threeka.ui.ErrorCallback
import ru.transport.threeka.ui.fragments.AdvFragment
import ru.transport.threeka.ui.fragments.DoubleRouteFragment
import ru.transport.threeka.ui.fragments.EventInfoFragment
import ru.transport.threeka.ui.fragments.NetworkErrorFragment
import ru.transport.threeka.ui.fragments.NoRouteFragment
import ru.transport.threeka.ui.fragments.PointInfoFragment
import ru.transport.threeka.ui.fragments.SimpleRouteFragment
import ru.transport.threeka.ui.fragments.StopInfoFragment
import java.time.LocalDate


class MainActivity : AppCompatActivity(), ErrorCallback {

    private val viewModel: MainViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    private lateinit var sharedPref: SharedPreferences

    private lateinit var mapView: MapView
    private var polylineObject: MapObject? = null
    private var polylineObject2: MapObject? = null
    private lateinit var imageProviderPoint: ImageProvider

    private val placemarkTapListener = MapObjectTapListener { obj, _ ->
        //Toast.makeText(this@MainActivity,"${obj.userData}", Toast.LENGTH_SHORT).show()

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction().remove(existingFragment).commit()
        }

        val fragment = StopInfoFragment.newInstance(obj.userData as Int)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        true
    }

    private val placemarkTapListenerEvent = MapObjectTapListener { obj, _ ->
        //Toast.makeText(this@MainActivity,"${obj.userData}", Toast.LENGTH_SHORT).show()

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction().remove(existingFragment).commit()
        }

        val fragment = EventInfoFragment.newInstance(obj.userData as Int)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
        true
    }

    private var stopsIconsCollection: MapObjectCollection? = null
    private var stopsIconsCollectionLikes: MapObjectCollection? = null

    private val placemarks = mutableListOf<PlacemarkMapObject>()

    private var stopsIconsCollectionEvents: MapObjectCollection? = null
    private val placemarksEvents = mutableListOf<PlacemarkMapObject>()

    private var pointCollection: MapObjectCollection? = null
    private var thePoint: PlacemarkMapObject? = null


    private val cameraListener = CameraListener { _, newPosition, _, _ ->
        if (newPosition.zoom > 13.9) {
            stopsIconsCollection?.setVisible(true)
            stopsIconsCollectionEvents?.setVisible(true)
        } else {
            stopsIconsCollection?.setVisible(false)
            stopsIconsCollectionEvents?.setVisible(false)
        }
    }

    val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            // Handle single tap ...
        }

        override fun onMapLongTap(map: Map, point: Point) {
            if (stopsIconsCollection?.isVisible ?: false) {

                thePoint = pointCollection!!.addPlacemark().apply {
                    geometry = Point(point.latitude, point.longitude)
                    setIcon(imageProviderPoint)
                }

                val existingFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (existingFragment != null) {
                    supportFragmentManager.beginTransaction().remove(existingFragment).commit()
                }

                val fragment = PointInfoFragment.newInstance(point.latitude, point.longitude)
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
                    .commit()
            } else {
                Toast.makeText(this@MainActivity, "Увеличьте масштаб карты", Toast.LENGTH_SHORT)
                    .show()
            }
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
                                ),/* zoom = */ 16.0f,/* azimuth = */ 0.0f,/* tilt = */ 0.0f
                            ), Animation(Animation.Type.SMOOTH, 0.5f), null
                        )
                    }
                }
                var resultTime = result.data?.getIntExtra("time", -1)
                if (resultTime != null && resultTime < 1) {
                    resultTime = null
                }
                viewModel.setTime(resultTime)
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
                                ),/* zoom = */ 16.0f,/* azimuth = */ 0.0f,/* tilt = */ 0.0f
                            ), Animation(Animation.Type.SMOOTH, 0.5f), null
                        )
                    }
                }
                var resultTime = result.data?.getIntExtra("time", -1)
                if (resultTime != null && resultTime < 1) {
                    resultTime = null
                }
                viewModel.setTime(resultTime)
            }
        }

    private val ifLogin =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultData = result.data?.getStringExtra("login")
                if (resultData == "logout") {
                    viewModel.setAuth(false)
                    viewModel.resetStops()
                } else if (resultData == "enter") {
                    viewModel.setAuth(true)
                    viewModel.resetStops()
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

        viewModel.refreshTokens()
        viewModel.setErrorCallback(this)
        viewModel.setCare(sharedPref.getBoolean("care", false))
        viewModel.setChange(sharedPref.getBoolean("change", false))
        viewModel.setNotif(sharedPref.getBoolean("notif", false))
        viewModel.setPriority(sharedPref.getInt("priority", 0))

        MapKitFactory.initialize(this)
        mapView = findViewById(R.id.mapview)
        mapView.mapWindow.map.isNightModeEnabled = isDarkTheme
        mapView.mapWindow.map.isRotateGesturesEnabled = !isNorth
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(51.68, 39.2),/* zoom = */ 12.0f,/* azimuth = */ 0.0f,/* tilt = */ 0.0f
            )
        )
        Toast.makeText(
            this@MainActivity,
            "Увеличьте масштаб карты, чтобы отобразить остановки",
            Toast.LENGTH_SHORT
        ).show()

        mapView.mapWindow.map.addInputListener(inputListener)

        val style =
            "[{\"tags\": {\"any\": [\"poi\", \"transit\"]}, \"stylers\": {\"visibility\": \"off\"}}]";
        mapView.mapWindow.map.setMapStyle(style);

        val imageProvider = ImageProvider.fromResource(this, R.drawable.stop)
        val imageProviderLiked = ImageProvider.fromResource(this, R.drawable.stop_liked)
        val imageProviderEvent = ImageProvider.fromResource(this, R.drawable.road_event)
        imageProviderPoint = ImageProvider.fromResource(this, R.drawable.point)

        stopsIconsCollection = mapView.mapWindow.map.mapObjects.addCollection()
        stopsIconsCollection!!.addTapListener(placemarkTapListener)

        stopsIconsCollectionLikes = mapView.mapWindow.map.mapObjects.addCollection()
        stopsIconsCollectionLikes!!.addTapListener(placemarkTapListener)

        stopsIconsCollectionEvents = mapView.mapWindow.map.mapObjects.addCollection()
        stopsIconsCollectionEvents!!.addTapListener(placemarkTapListenerEvent)

        pointCollection = mapView.mapWindow.map.mapObjects.addCollection()



        viewModel.stops.observe(this, Observer { stops ->
            //for (placemark in placemarks) {
            //    stopsIconsCollection!!.remove(placemark)
            //}
            //for (placemark in placemarksLikes) {
            //    stopsIconsCollectionLikes!!.remove(placemark)
            //}
            stopsIconsCollection!!.clear()
            stopsIconsCollectionLikes!!.clear()
            placemarks.clear()
            //placemarksLikes.clear()
            for (i in stops.indices) {
                if (stops[i].like) {
                    val placemark = stopsIconsCollectionLikes!!.addPlacemark().apply {
                        geometry = Point(stops[i].coord.lat, stops[i].coord.lon)
                        userData = i
                        setIcon(imageProviderLiked)
                    }
                    //placemarksLikes.add(placemark)
                    placemarks.add(placemark)
                } else {
                    val placemark = stopsIconsCollection!!.addPlacemark().apply {
                        geometry = Point(stops[i].coord.lat, stops[i].coord.lon)
                        userData = i
                        setIcon(imageProvider)
                    }
                    placemarks.add(placemark)
                }
            }
        })


        viewModel.events.observe(this, Observer { events ->
            stopsIconsCollectionEvents!!.clear()
            placemarksEvents.clear()
            for (i in events.indices) {
                val placemark = stopsIconsCollectionEvents!!.addPlacemark().apply {
                    geometry = Point(events[i].lat.toDouble(), events[i].lon.toDouble())
                    userData = i
                    setIcon(imageProviderEvent)
                }
                placemarksEvents.add(placemark)
            }
        })

        viewModel.likedStop.observe(this, Observer { index ->
            if (index in placemarks.indices) {
                val lon = placemarks[index].geometry.longitude
                val lat = placemarks[index].geometry.latitude
                stopsIconsCollection!!.remove(placemarks[index])
                val placemark = stopsIconsCollectionLikes!!.addPlacemark().apply {
                    geometry = Point(lat, lon)
                    userData = index
                    setIcon(imageProviderLiked)
                }
                placemarks[index] = placemark
            }
        })

        viewModel.dislikedStop.observe(this, Observer { index ->
            if (index in placemarks.indices) {
                val lon = placemarks[index].geometry.longitude
                val lat = placemarks[index].geometry.latitude
                stopsIconsCollectionLikes!!.remove(placemarks[index])
                val placemark = stopsIconsCollection!!.addPlacemark().apply {
                    geometry = Point(lat, lon)
                    userData = index
                    setIcon(imageProviderEvent)
                }
                placemarks[index] = placemark
            }
        })

        viewModel.clearPoint.observe(this, Observer { index ->
            if (thePoint != null) {
                pointCollection!!.remove(thePoint!!)
            }
        })


        viewModel.deletedEvent.observe(this, Observer { index ->
            if (index in placemarksEvents.indices) {
                stopsIconsCollectionEvents!!.remove(placemarksEvents[index])
            }
        })

        viewModel.addedEvent.observe(this, Observer { event ->
            if (event != null) {
                val placemark = stopsIconsCollectionEvents!!.addPlacemark().apply {
                    geometry = Point(event.lat.toDouble(), event.lon.toDouble())
                    userData = placemarksEvents.size
                    setIcon(imageProviderEvent)
                }
                placemarksEvents.add(placemark)
            }
        })

        /*viewModel.likedStop.observe(this, Observer { index ->
            for (i in placemarks.indices) {
                if (index == placemarks[i].userData) {
                    val lon = placemarks[i].geometry.longitude
                    val lat = placemarks[i].geometry.latitude
                    stopsIconsCollection!!.remove(placemarks[i])
                    val placemark = stopsIconsCollectionLikes!!.addPlacemark().apply {
                        geometry = Point(lat, lon)
                        userData = index
                        setIcon(imageProviderLiked)
                    }
                    placemarksLikes.add(placemark)
                    placemarks.remove(placemarks[i])
                    break

                    placemarks.set
                }
            }
        })

        viewModel.dislikedStop.observe(this, Observer { index ->
            for (i in placemarksLikes.indices) {
                if (index == placemarksLikes[i].userData) {
                    val lon = placemarksLikes[i].geometry.longitude
                    val lat = placemarksLikes[i].geometry.latitude
                    stopsIconsCollectionLikes!!.remove(placemarksLikes[i])
                    val placemark = stopsIconsCollection!!.addPlacemark().apply {
                        geometry = Point(lat, lon)
                        userData = index
                        setIcon(imageProvider)
                    }
                    placemarks.add(placemark)
                    placemarksLikes.remove(placemarksLikes[i])
                    break
                }
            }
        })*/

        val myButton1: Button = findViewById(R.id.myButton1)
        val myButton2: Button = findViewById(R.id.myButton2)
        val timeLabel: TextView = findViewById(R.id.time_header)

        viewModel.time.observe(this, { time ->
            if (time == null) {
                timeLabel.visibility = View.GONE
            } else {
                timeLabel.visibility = View.VISIBLE
                timeLabel.text = String.format("Отправление в %d:%02d", time / 60, time % 60)

            }
        })

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
                    supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
                        .commit()
                } else {
                    val fragment = DoubleRouteFragment()
                    supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
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
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
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
            intent.putExtra("stops_like", viewModel.getStopsLikes())
            intent.putExtra("time", viewModel.time.value ?: -1)
            selectStopFrom.launch(intent)
        }


        myButton2.setOnClickListener {
            val intent = Intent(this, SelectActivity::class.java)
            intent.putExtra("label", "Куда")
            intent.putExtra("stops", viewModel.getStops())
            intent.putExtra("stops_lat", viewModel.getStopsLat())
            intent.putExtra("stops_lon", viewModel.getStopsLon())
            intent.putExtra("stops_like", viewModel.getStopsLikes())
            intent.putExtra("time", viewModel.time.value ?: -1)
            selectStopTo.launch(intent)
        }

        val settingsButton: ImageButton = findViewById(R.id.button_setting)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            ifLogin.launch(intent)
        }

        val filterButton: ImageButton = findViewById(R.id.button_filter)
        filterButton.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            startActivity(intent)
        }


        val imageView: ImageView = findViewById(R.id.myImageView)

        val today = LocalDate.now()
        val targetDate = LocalDate.of(2025, 6, 9)
        if (today.isBefore(targetDate)) {
            imageView.visibility = View.VISIBLE
            val eventParameters = mapOf("type" to "adv1")
            AppMetrica.reportEvent("AdvShown", eventParameters)
        }
        imageView.setOnClickListener {
            val eventParameters = mapOf("type" to "adv1")
            AppMetrica.reportEvent("AdvClicked", eventParameters)
            val existingFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (existingFragment != null) {
                supportFragmentManager.beginTransaction().remove(existingFragment).commit()
            }
            val fragment = AdvFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
                .commit()
        }

        viewModel.adv.observe(this, Observer { adv ->
            if (adv) {
                imageView.visibility = View.GONE
            }
        })

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
        viewModel.setNotif(sharedPref.getBoolean("notif", false))

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)
        val isNorth = sharedPref.getBoolean("north_upper", false)
        if (mapView.mapWindow.map.isRotateGesturesEnabled && isNorth) {
            mapView.mapWindow.map.isRotateGesturesEnabled = false
            mapView.mapWindow.map.move(
                CameraPosition(
                    Point(51.68, 39.2),/* zoom = */ 12.0f,/* azimuth = */ 0.0f,/* tilt = */ 0.0f
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
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }
}