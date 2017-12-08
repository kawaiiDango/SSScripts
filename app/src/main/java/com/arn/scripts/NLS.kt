package com.arn.scripts

import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.IntentFilter
import android.service.notification.NotificationListenerService

/**
 * Created by arn on 06/12/2017.
 */

class NLS : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()

        val iF = IntentFilter()
        iF.addAction(ACTION_POWER_CONNECTED)
        registerReceiver(ChargeBrokenUSB(), iF)

    }
}
