package com.example.servicetest01

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log

class TestService() : Service() {

    // 綁定時調用
    // 必須要實現的方法
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    // 使次創建時，系統將調用此方法來執行一次性程序（在調用onStartCommand() 或onBind() 之前）
    // 如果服務已經在運行，則不會掉用此方法。該方法只會被調用一次
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    // 每次通過startService()方法啟動Service都會被回調
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        return super.onStartCommand(intent, flags, startId)
    }

    // 服務銷毀時的回調
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun stopService(name: Intent?): Boolean {
        Log.d(TAG, "stopService: ")
        return super.stopService(name)
    }
}