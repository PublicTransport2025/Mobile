package ru.transport.threeka.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.Coord
import ru.transport.threeka.api.schemas.Stop
import ru.transport.threeka.api.schemas.navigation.RouteReport
import ru.transport.threeka.ui.ErrorCallback

class MainViewModel : ViewModel() {
    private val _count = MutableLiveData(0)
    val count: LiveData<Int> get() = _count

    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> get() = _stops
    private var isStopsLoaded = false

    private val _stopFrom = MutableLiveData<Stop?>()
    val stopFrom: LiveData<Stop?> get() = _stopFrom

    private val _stopTo = MutableLiveData<Stop?>()
    val stopTo: LiveData<Stop?> get() = _stopTo

    private val _routeReport = MutableLiveData<RouteReport?>()
    val routeReport: LiveData<RouteReport?> get() = _routeReport

    private val _acvtiveRoute = MutableLiveData(0)
    val activeRoute: LiveData<Int> get() = _acvtiveRoute

    init {
        _stopFrom.value = null;
        _stopTo.value = null;
    }


    fun increment() {
        _count.value = (_count.value ?: 0) + 1
    }

    fun right() {
        _acvtiveRoute.value = (_acvtiveRoute.value ?: 0) + 1
    }

    fun hasRight(): Boolean {
        return (_acvtiveRoute.value ?: 0) < (_routeReport.value?.count ?: 0)
    }

    fun hasLeft(): Boolean {
        return (_acvtiveRoute.value ?: 0) > 1
    }

    fun left() {
        _acvtiveRoute.value = (_acvtiveRoute.value ?: 0) - 1
    }

    fun killRoute() {
        _acvtiveRoute.value = 0
        _stopFrom.value = null
        _stopTo.value = null
    }


    fun getStopName(index: Int): String {
        return _stops.value?.get(index)?.name ?: "Nothing"
    }

    fun getStopAbout(index: Int): String {
        return _stops.value?.get(index)?.about ?: "Nothing"
    }

    fun setStopFrom(index: Int) {
        _stopFrom.value = _stops.value?.get(index)
        if (_stopFrom.value == _stopTo.value) {
            _stopTo.value = null
        }
        checkRoute()
    }

    fun setStopTo(index: Int) {
        _stopTo.value = _stops.value?.get(index)
        if (_stopFrom.value == _stopTo.value) {
            _stopFrom.value = null
        }
        checkRoute()
    }

    fun resetStops() {
        isStopsLoaded = false
        loadStops()
    }

    fun loadStops() {
        if (isStopsLoaded) return

        viewModelScope.launch {
            try {
                val response = apiService.getStops().awaitResponse()
                _stops.value = response.body()
                isStopsLoaded = true
            } catch (e: Exception) {
                Log.e("APIError", "Не удалось получить список остановок" + e.message)
                errorCallback?.onError(e)
            }
        }
    }


    private var errorCallback: ErrorCallback? = null

    fun setErrorCallback(callback: ErrorCallback) {
        errorCallback = callback
    }

    fun checkRoute() {
        if (_stopFrom.value != null && _stopTo.value != null) {
            loadRoute()
        }
    }

    fun loadRoute() {
        viewModelScope.launch {
            try {
                val fromId = _stopFrom.value!!.id
                val toId = _stopTo.value!!.id
                val response = apiService.createRoute(fromId, toId).awaitResponse()
                _routeReport.value = response.body()
                val result = _routeReport.value?.result ?: 0
                if (result == 200) {
                    _acvtiveRoute.value = 1
                } else {
                    _acvtiveRoute.value = -1
                }
            } catch (e: Exception) {
                Log.e("APIError", "Не удалось построить маршрут")
                errorCallback?.onError(e)
            }
        }
    }

    fun getRouteTitle(): String {
        return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.label
            ?: "Nothing"
    }

    fun getRouteNumber(): String {
        return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.number
            ?: "Nothing"
    }

    fun getRouteCoords(): List<Coord> {
        return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.stops
            ?: listOf()
    }

    fun getRouteLoad(): Int {
        return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.load ?: 0
    }

}