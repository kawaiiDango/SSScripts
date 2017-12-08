package com.arn.scripts

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Created by arn on 07/12/2017.
 */

class MyAccessibilityService : AccessibilityService() {
    lateinit private var pref: SharedPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        val iF = IntentFilter(Main.DA_BROADCAST)
        registerReceiver(AccBroadcastReceiver(), iF)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        TYPE_SPLIT_SCREEN_DIVIDER
//        Log.d("scripts", "windowChanged " + " " + event.packageName+" " + event.source?.window)

        if (!windows.isEmpty()){
            var shouldIgnore = false
            var isSplitVisible = false
            windows.forEach {
                when {
                    it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD -> shouldIgnore = true
                    it.type == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER -> isSplitVisible = true
                    it.type == AccessibilityWindowInfo.TYPE_SYSTEM -> {
                        Log.d("scripts", "window: " + it)
                        if (it.id == 1 && it.isActive && it.isFocused) // status bar, afaik
                            shouldIgnore = true
                    }
                }
            }
            if (!shouldIgnore && isSplitVisible)
                setDPI(pref.getInt("split_dpi", 0))
            else if (!shouldIgnore && !isSplitVisible)
                setDPI(pref.getInt("normal_dpi", 0))

        }

    }

    override fun onInterrupt() {

    }

    private fun setDPI(dpi:Int){
        if (resources.displayMetrics.densityDpi == 0 || resources.displayMetrics.densityDpi == dpi) {
            Log.d("scripts", "setDPI ignored")
            return
        }
        Log.d("scripts", "setDPI $dpi")
        if(dpi == 0) //0 = reset
            Main.runAsRoot(this,"wm density reset")
        else
            Main.runAsRoot(this,"wm density $dpi")
    }

    class AccBroadcastReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            if(intent?.action == Main.DA_BROADCAST){
                if (intent.getIntExtra("extra", 0) == Main.DIE)
                    (context as AccessibilityService).disableSelf()
            }
        }
    }
}
