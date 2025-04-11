package ru.transport.threeka.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.awaitResponse
import ru.transport.threeka.api.RetrofitClient.apiService
import ru.transport.threeka.api.schemas.Stop
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

    init {
        _stopFrom.value = null;
        _stopTo.value = null;
    }


    fun increment() {
        _count.value = (_count.value ?: 0) + 1
    }

    fun getStopName(index: Int): String {
        return _stops.value?.get(index)?.name ?: "Nothing"
    }

    fun getStopAbout(index: Int): String {
        return _stops.value?.get(index)?.about ?: "Nothing"
    }

    fun setStopFrom(index: Int) {
        _stopFrom.value = _stops.value?.get(index)
    }

    fun setStopTo(index: Int) {
        _stopTo.value = _stops.value?.get(index)
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
                Log.e("APIError", "Не удалось получить список остановок")
                errorCallback?.onError(e)
            }
        }
    }


    private var errorCallback: ErrorCallback? = null

    fun setErrorCallback(callback: ErrorCallback) {
        errorCallback = callback
    }
}