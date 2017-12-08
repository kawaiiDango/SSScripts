package com.arn.scripts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChargeBrokenUSB : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action){
            Intent.ACTION_POWER_CONNECTED -> {
                Main.runAsRoot(context, Main.FAST_CHARGE)
            }
            Intent.ACTION_CONFIGURATION_CHANGED -> {
//                Main.toast(context, intent.action)
            }
        }
    }

}
