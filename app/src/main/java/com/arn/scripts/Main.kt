package com.arn.scripts

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Main : Activity() {

    private var mVisible: Boolean = false
    lateinit private var pref:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        btn_dummy.setOnClickListener{
//            runAsRoot(this, FAST_CHARGE)
            checkNotificationAccess()
            finish()
        }

        btn_acc.setOnClickListener{
            if (!btn_acc.isChecked)
                sendBroadcast(Intent(Main.DA_BROADCAST).putExtra("extra", DIE))
            else
                startActivityForResult(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0)
        }
        normal_dpi_bar.currentValue = pref.getInt("normal_dpi", 0)
        split_dpi_bar.currentValue = pref.getInt("split_dpi", 0)

        runAsRoot(this, FAST_CHARGE)
//        finish()
    }

    override fun onResume() {
        super.onResume()
        current_dpi.text = "Current DPI: " + resources.displayMetrics.densityDpi
        btn_acc.isChecked = isAccessibilityEnabled(BuildConfig.APPLICATION_ID + "/." + MyAccessibilityService::class.java.simpleName)

    }

    override fun onPause(){
        super.onPause()
        Log.i("acc:", normal_dpi_bar.currentValue.toString())
        pref.edit()
                .putInt("normal_dpi", normal_dpi_bar.currentValue)
                .putInt("split_dpi", split_dpi_bar.currentValue)
                .apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun checkNotificationAccess() {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver,"enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return
                    }
                }
            }
        }
        startActivity(Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun isAccessibilityEnabled(id: String): Boolean {

        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)

        return runningServices.any { id == it.id }
    }

    companion object {

        val FAST_CHARGE = "chmod +w /sys/class/power_supply/usb/current_max\n" +
                "echo 1800000 > /sys/class/power_supply/usb/current_max"
        val DA_BROADCAST = "com.arn.scripts.br"
        val DIE = -1

        fun runAsRoot(context:Context, s:String){
            val p: Process
            try {
                // Preform su to get root privledges
                p = Runtime.getRuntime().exec("su")

                // Attempt to write a file to a root-only
                val os = DataOutputStream(p.outputStream)
                val istr = BufferedReader(InputStreamReader(p.inputStream))
                os.writeBytes(s + "\n")
                os.writeBytes( "exit\n")
                os.flush()

                try {
                    p.waitFor()
                    if (p.exitValue() != 255) {
                        // TODO Code to run on success
                        val line = istr.readLine()
                        if(line?.length ?: 0 > 1)
                            Main.toast(context,  line)
                    } else {
                        // TODO Code to run on unsuccessful
                        Main.toast(context, "unsuccessful")
                    }
                } catch (e: InterruptedException) {
                    // TODO Code to run in interrupted exception
                    Main.toast(context, "interrupted exception")
                } finally {
                    os.close()
                    istr.close()
                }

            } catch (e: IOException) {
                // TODO Code to run in input/output exception
                Main.toast(context, "nput/output exception")
                e.printStackTrace()
            }
        }

        fun toast(context: Context, s:String){
            Log.i(BuildConfig.APPLICATION_ID, s)
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
        }
    }
}
