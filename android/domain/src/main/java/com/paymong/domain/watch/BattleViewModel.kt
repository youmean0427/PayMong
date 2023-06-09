package com.paymong.domain.watch

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paymong.data.socket.BattleSocketService
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.paymong.common.code.MongCode
import com.paymong.common.code.MatchingCode
import com.paymong.common.code.MessageType
import com.paymong.data.model.response.BattleMessageResDto
import com.paymong.data.repository.InformationRepository
import com.paymong.domain.entity.BattleActive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@SuppressLint("MissingPermission")
class BattleViewModel (
    private val application: Application
): AndroidViewModel(application)  {
    companion object {
        private const val FIND_DELAY = 2000L
        private const val ACTIVE_DELAY = 2000L
        private const val SELECT_BEFORE_DELAY = 2000L
        private const val SELECT_DELAY = 100L
        private const val SELECT_INTERVAL = 0.01
        private const val END_DELAY = 10000L
    }

    var mongId by mutableStateOf(0L)
    var matchingState by mutableStateOf(MatchingCode.FINDING)
    var battleActive: BattleActive by mutableStateOf(BattleActive())

    var mongCode by mutableStateOf(MongCode.CH444)
    var isLoading by mutableStateOf(false)
    private val informationRepository: InformationRepository = InformationRepository()

    // gps
    private lateinit var gpsJob : Job
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    // socket
    private lateinit var socketJob : Job
    private lateinit var battleSocketService: BattleSocketService

    // Battle - 찾기
    var playerCodeA by mutableStateOf(MongCode.CH444)
    var playerCodeB by mutableStateOf(MongCode.CH444)
    
    // Battle - 진행
    var totalTurn = 10
    var nextAttacker by mutableStateOf("A")
    var battleSelectTime by mutableStateOf(0.0)
    private var selectState by mutableStateOf(MessageType.STAY)

    // Battle - 끝
    var win by mutableStateOf(false)

    fun init() {
        viewModelScope.launch(Dispatchers.Main) {
            isLoading = false
            findMong()
        }
    }
    fun select(select : MessageType) {
        selectState = select
        matchingState = MatchingCode.SELECT_AFTER
    }
    private fun findMong() {
        viewModelScope.launch(Dispatchers.IO) {
            informationRepository.findMong()
                .catch {
                    it.printStackTrace()
                }
                .collect { data ->
                    mongCode = MongCode.valueOf(data.mongCode)
                    isLoading = true
                }
        }
    }
    private fun findCharacterId(mongCodeA: String, mongCodeB: String) {
        // playerCodeA :: 아래쪽
        // playerCodeB :: 위쪽
        if (battleActive.order == "A") {
            playerCodeA = MongCode.valueOf(mongCodeB)
            playerCodeB = MongCode.valueOf(mongCodeA)
        }
        else {
            playerCodeA = MongCode.valueOf(mongCodeA)
            playerCodeB = MongCode.valueOf(mongCodeB)
        }
    }
    override fun onCleared() {
        super.onCleared()
        try {
            battleSocketService.disConnect(mongId, mongCode.code)
            gpsJob.cancel()
            socketJob.cancel()
        } catch (_: Exception) {  }
    }

    // -------------------------------------------------------------------------------------------------
    // gps
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 위치 한번 받고 업데이트 요청 종료
            val latitude = locationResult.lastLocation.latitude
            val longitude = locationResult.lastLocation.longitude

            Log.d("battle", "latitude : $latitude, longitude : $longitude")

            mFusedLocationProviderClient.removeLocationUpdates(this)

            battleSocketService = BattleSocketService()
            battleSocketService.init(listener)

            socketJob = viewModelScope.launch {
                try {
                    battleSocketService.connect(mongId, mongCode.code, latitude, longitude)
                } catch (_: NullPointerException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    // socket
    private val listener: WebSocketListener = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val battleMessageResDto = Gson().fromJson(text, BattleMessageResDto::class.java)

                if (battleMessageResDto.totalTurn == 0) {
                    matchingState = MatchingCode.NOT_FOUND
                } else {
                    println(battleMessageResDto.toString())
                    battleActive = BattleActive(
                        battleMessageResDto.battleRoomId,
                        battleMessageResDto.mongCodeA,
                        battleMessageResDto.mongCodeB,
                        battleMessageResDto.nowTurn,
                        battleMessageResDto.totalTurn,
                        battleMessageResDto.nextAttacker,
                        battleMessageResDto.order,
                        battleMessageResDto.damageA,
                        battleMessageResDto.damageB,
                        battleMessageResDto.healthA,
                        battleMessageResDto.healthB
                    )
                    when (battleActive.nowTurn) {
                        0 -> {
                            // 시작
                            findCharacterId(battleMessageResDto.mongCodeA, battleMessageResDto.mongCodeB)
                            matchingState = MatchingCode.FOUND
                        }
                        -1 -> {
                            // 게임 끝
                            battleSocketService.disConnect(mongId, mongCode.code)
                            matchingState = MatchingCode.END
                        }
                        else -> {
                            // 다음 턴
                            matchingState = MatchingCode.ACTIVE_RESULT
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // -------------------------------------------------------------------------------------------------
    // 배틀 대기열 등록 함수
    fun battleWait() {
        viewModelScope.launch {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
            mLocationRequest = LocationRequest.create().apply { priority = LocationRequest.PRIORITY_HIGH_ACCURACY }

            if (::mFusedLocationProviderClient.isInitialized) {
                mFusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()!!
                )
            }
        }
    }

    // 배틀 찾았을 때 호출 할 함수
    fun battleFind() {
        viewModelScope.launch {
            delay(FIND_DELAY)
            matchingState = MatchingCode.ACTIVE
        }
    }

    // 배틀 찾기 실패했을 때 호출 할 함수
    fun battleFindFail() {
        viewModelScope.launch {
            matchingState = MatchingCode.FINDING
            try {
                battleSocketService.disConnect(mongId, mongCode.code)
                gpsJob.cancel()
                socketJob.cancel()
            } catch (_: Exception) {  }
        }
    }

    // 배틀 시작할 때 호출 할 함수
    fun battleActive() {
        viewModelScope.launch {
            delay(ACTIVE_DELAY)
            matchingState = MatchingCode.SELECT_BEFORE
        }
    }

    // 배틀 선택 전 호출 할 함수 (선택으로 넘어가기 위한 함수)
    fun battleSelectBefore() {
        viewModelScope.launch {
            delay(SELECT_BEFORE_DELAY)
            matchingState = MatchingCode.SELECT
        }
    }
    
    // 배틀 선택 시 호출 할 함수
    fun battleSelect() {
        viewModelScope.launch {
            selectState = MessageType.STAY
            battleSelectTime = 0.0
            do {
                delay(SELECT_DELAY)
                battleSelectTime += SELECT_INTERVAL

                if (matchingState == MatchingCode.SELECT_AFTER)
                    break
            } while(battleSelectTime <= 1.0)

            battleSocketService.select(selectState, mongId, battleActive.battleRoomId, battleActive.order)
            matchingState = MatchingCode.SELECT_AFTER
        }
    }

    // 배틀 끝난 후 호출 할 함수
    fun battleEnd() {
        viewModelScope.launch {
            if (battleActive.healthA == battleActive.healthB) {
                win = true
            }
            else if (battleActive.order == "A" &&
                battleActive.healthA > battleActive.healthB) {
                win = true
            }
            else if(battleActive.order == "B" &&
                    battleActive.healthB > battleActive.healthA){
                win = true
            }
            delay(END_DELAY)
            matchingState = MatchingCode.FINDING
        }
    }
}


