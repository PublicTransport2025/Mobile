package ru.transport.threeka.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.navigation.Coord
import ru.transport.threeka.api.schemas.navigation.Event
import ru.transport.threeka.api.schemas.navigation.RouteReport
import ru.transport.threeka.api.schemas.navigation.Stop
import ru.transport.threeka.services.TokenManager
import ru.transport.threeka.ui.ErrorCallback

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    private val _authorized = MutableLiveData(false)
    val authorized: LiveData<Boolean> get() = _authorized

    fun setAuth(bool: Boolean) {
        _authorized.value = bool;
    }

    private val _adv = MutableLiveData(false)
    val adv: LiveData<Boolean> get() = _adv

    fun setAdv(bool: Boolean) {
        _adv.value = bool;
    }

    private val _count = MutableLiveData(0)
    val count: LiveData<Int> get() = _count

    private val _likedStop = MutableLiveData(-1)
    val likedStop: LiveData<Int> get() = _likedStop

    fun setLikedStop(index: Int) {
        _likedStop.value = index;
    }

    private val _deletedEvent = MutableLiveData(-1)
    val deletedEvent: LiveData<Int> get() = _deletedEvent

    fun setDeletedEvent(index: Int) {
        _deletedEvent.value = index;
    }

    private val _dislikedStop = MutableLiveData(-1)
    val dislikedStop: LiveData<Int> get() = _dislikedStop

    fun setDislikedStop(index: Int) {
        _dislikedStop.value = index;
    }


    private val _addedEvent = MutableLiveData<Event?>()
    val addedEvent: LiveData<Event?> get() = _addedEvent


    private val _care = MutableLiveData(false)
    val care: LiveData<Boolean> get() = _care

    fun setCare(bool: Boolean) {
        _care.value = bool;
    }

    private val _notif = MutableLiveData(false)
    val notif: LiveData<Boolean> get() = _notif

    fun setNotif(bool: Boolean) {
        _notif.value = bool;
    }

    private val _change = MutableLiveData(false)
    val change: LiveData<Boolean> get() = _change

    fun setChange(bool: Boolean) {
        _change.value = bool;
    }

    private val _time = MutableLiveData<Int?>()
    val time: LiveData<Int?> get() = _time

    fun setTime(newTime: Int?) {
        _time.value = newTime;
    }

    private val _priority = MutableLiveData(0)
    val priority: LiveData<Int> get() = _priority

    fun setPriority(bool: Int) {
        _priority.value = bool;
    }

    private val _stops = MutableLiveData<MutableList<Stop>>()
    val stops: LiveData<MutableList<Stop>> get() = _stops
    private var isStopsLoaded = false

    private val _events = MutableLiveData<MutableList<Event>>()
    val events: LiveData<MutableList<Event>> get() = _events

    fun replaceStop(index: Int, stop: Stop?) {
        if (stop != null) {
            _stops.value!![index] = stop
        }
    }

    fun replaceEvent(index: Int, event: Event?) {
        if (event != null) {
            _events.value!![index] = event
        }
    }

    fun addEvent(event: Event) {
        _events.value?.add(event)
        _addedEvent.value = event
    }


    private val _clearPoint = MutableLiveData(0)
    val clearPoint: LiveData<Int?> get() = _clearPoint

    fun clearMapPoint() {
        _clearPoint.value = 0;
    }


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
        _time.value = null;
        _addedEvent.value = null
    }


    fun increment() {
        _count.value = (_count.value ?: 0) + 1
    }

    fun right() {
        _acvtiveRoute.value = (_acvtiveRoute.value ?: 0) + 1
    }

    fun awakeRoute() {
        _acvtiveRoute.value = _acvtiveRoute.value
    }

    fun hasRight(): Boolean {
        return (_acvtiveRoute.value ?: 0) < (_routeReport.value?.count ?: 0)
    }

    fun isSimple(): Boolean {
        return (_acvtiveRoute.value ?: 0) - 1 < (_routeReport.value?.count_simple ?: 0)
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

    fun getStopLike(index: Int): Boolean {
        return _stops.value?.get(index)?.like ?: false
    }

    fun getEvent(index: Int): Event? {
        return _events.value?.get(index)
    }


    fun getStopId(index: Int): Int {
        return _stops.value?.get(index)?.id ?: -1
    }

    fun setStopFrom(index: Int) {
        if (index < 0) {
            return
        }
        _stopFrom.value = _stops.value?.get(index)
        if (_stopFrom.value == _stopTo.value) {
            _stopTo.value = null
        }
        checkRoute()

        val eventParameters = mapOf("name" to (_stopFrom.value?.name ?: "-"))
        AppMetrica.reportEvent("StopSelected", eventParameters)
    }

    fun setStopTo(index: Int) {
        if (index < 0) {
            return
        }
        _stopTo.value = _stops.value?.get(index)
        if (_stopFrom.value == _stopTo.value) {
            _stopFrom.value = null
        }
        checkRoute()

        val eventParameters = mapOf("name" to (_stopTo.value?.name ?: "-"))
        AppMetrica.reportEvent("StopSelected", eventParameters)
    }

    fun resetStops() {
        isStopsLoaded = false
        loadStops()
    }

    private fun loadStops() {
        if (isStopsLoaded) return

        viewModelScope.launch {
            try {
                val response1 = apiService.getStops(tokenManager.getAccessToken()).awaitResponse()
                _stops.value = response1.body()
                val response2 = apiService.getEvents(tokenManager.getAccessToken()).awaitResponse()
                _events.value = response2.body()
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
                val care_ = _care.value ?: false
                val change_ = _change.value ?: false
                val priority_ = _priority.value ?: 0
                val response = apiService
                    .createRoute(fromId, toId, care_, change_, priority_, _time.value)
                    .awaitResponse()
                _routeReport.value = response.body()
                val result = _routeReport.value?.result ?: 0
                if (result == 200) {
                    _acvtiveRoute.value = 1
                    val eventParameters =
                        mapOf("from" to _stopFrom.value!!.name, "to" to _stopTo.value!!.name)
                    AppMetrica.reportEvent("RouteCreated", eventParameters)
                } else {
                    _acvtiveRoute.value = -1
                }
            } catch (e: Exception) {
                Log.e("APIError", "Не удалось построить маршрут" + e.message)
                _acvtiveRoute.value = -1
            }
        }
    }

    fun getRouteTitle(): String {
        if ((_acvtiveRoute.value ?: 0) - 1 < (_routeReport.value?.count_simple ?: 0)) {
            return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.label
                ?: "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.label1
                ?: "Nothing"
        }
    }

    fun getRouteTitleDouble(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.label2
                ?: "Nothing"
        }
    }

    fun getRouteNumber(): String {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.number
                ?: "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.number1
                ?: "Nothing"
        }
    }

    fun getRouteNumberDouble(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.number2
                ?: "Nothing"
        }
    }

    fun getRouteCoords(): List<Coord> {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.stops
                ?: listOf()
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.stops1
                ?: listOf()
        }
    }

    fun getRouteCoordsDouble(): List<Coord> {
        if (isSimple()) {
            return listOf()
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.stops2
                ?: listOf()
        }
    }

    fun getStops(): ArrayList<String> {
        val list = ArrayList<String>()
        for (stop in _stops.value ?: listOf()) {
            if (stop.about != null && stop.about != "None") {
                list.add(stop.name + " (" + stop.about + ")")
            } else {
                list.add(stop.name)
            }

        }
        return list
    }

    fun getStopsLat(): DoubleArray {
        val array = DoubleArray(_stops.value?.size ?: 0)
        for (i in (_stops.value ?: listOf()).indices) {
            array[i] = (_stops.value ?: listOf())[i].coord.lat
        }
        return array
    }

    fun getStopsLon(): DoubleArray {
        val array = DoubleArray(_stops.value?.size ?: 0)
        for (i in (_stops.value ?: listOf()).indices) {
            array[i] = (_stops.value ?: listOf())[i].coord.lon
        }
        return array
    }

    fun getStopsLikes(): BooleanArray {
        val array = BooleanArray(_stops.value?.size ?: 0)
        for (i in (_stops.value ?: listOf()).indices) {
            array[i] = (_stops.value ?: listOf())[i].like
        }
        return array
    }

    fun getRouteLoad(): Int {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.load ?: 0
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.load1
                ?: 0
        }
    }

    fun getRouteLoadDouble(): Int {
        if (isSimple()) {
            return 0
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.load2
                ?: 0
        }
    }

    fun getRouteTimeLabel(): String {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1
            )?.time_label
                ?: "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_label1
                ?: "Nothing"
        }
    }

    fun getRouteTimeLabelDouble(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_label2
                ?: "Nothing"
        }
    }

    fun getRouteTimeBegin(): String {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1
            )?.time_begin
                ?: "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_begin1
                ?: "Nothing"
        }
    }

    fun getRouteTimeBeginDouble(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_begin2
                ?: "Nothing"
        }
    }

    fun getRouteTimeRoad(): String {
        if (isSimple()) {
            return _routeReport.value?.simple_routes?.get((_acvtiveRoute.value ?: 1) - 1)?.time_road
                ?: "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_road1
                ?: "Nothing"
        }
    }

    fun getRouteTimeRoadDouble(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.time_road2
                ?: "Nothing"
        }
    }

    fun getRouteStop1(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.stop1
                ?: "Nothing"
        }
    }

    fun getRouteStop2(): String {
        if (isSimple()) {
            return "Nothing"
        } else {
            return _routeReport.value?.double_routes?.get(
                (_acvtiveRoute.value ?: 1) - 1 - (_routeReport.value?.count_simple ?: 0)
            )?.stop2
                ?: "Nothing"
        }
    }


    fun refreshTokens() {
        viewModelScope.launch {
            val currentRefreshToken = tokenManager.getRefreshToken()
            if (currentRefreshToken == null) {
                handleLogout()
                try {
                    loadStops()
                } catch (e: Exception) {
                    isStopsLoaded = false
                    Log.e("LoginError", "Не загрузить остановки" + e.message)
                    errorCallback?.onError(e)
                }
                return@launch
            }

            try {
                val response = apiService.refreshToken(currentRefreshToken).awaitResponse()
                if (response.isSuccessful) {
                    val token = response.body()
                    if (token != null) {
                        tokenManager.saveToken(token)
                        _authorized.value = true
                    } else {
                        handleLogout()
                    }
                } else {
                    handleLogout()
                }
                loadStops()
            } catch (e: Exception) {
                isStopsLoaded = false
                Log.e("LoginError", "Не удалось обновить токен" + e.message)
                errorCallback?.onError(e)
            }


        }
    }

    private fun handleLogout() {
        tokenManager.clearTokens()
        _authorized.value = false
    }

}