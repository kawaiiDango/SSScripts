package com.arn.scripts

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
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
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        var txt = ""+event.eventType
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> txt = "TYPE_WINDOWS_CHANGED"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> txt = "TYPE_WINDOW_STATE_CHANGED"
        }
//        TYPE_SPLIT_SCREEN_DIVIDER
        Log.d("scripts", "onAccessibilityEvent "+ txt + " " + event.packageName+" " + (event.source?.window?.type == 5))

        if (!windows.isEmpty()){
            if (windows.any { it.type == AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER }) {
                setDPI(pref.getInt("split_dpi", 0))
            } else {
                setDPI(pref.getInt("normal_dpi", 0))
            }
        }

    }

    override fun onInterrupt() {

    }

    private fun setDPI(dpi:Int){
        if (resources.displayMetrics.densityDpi == dpi) {
            Log.d("scripts", "setDPI ignored")
            return
        }
        Log.d("scripts", "setDPI $dpi")
        if(dpi == 0) //0 = reset
            Main.runAsRoot(this,"wm density reset")
        else
            Main.runAsRoot(this,"wm density $dpi")
    }
}
