package com.sachtech.stadia

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import com.sachtech.stadia.utils.PrefKey
import com.sachtech.stadia.utils.cmtoInches
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_setupstadia.*

class SetUpStadiaActivity : BaseActivity(), View.OnClickListener {
    private val REQUEST_ACCESS_COARSE_LOCATION = 1022 // random number
    private var customDialogFragment: CustomDialogFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setupstadia)
        tv_CurrentOffsetValue.text=sharedPreference.getInt(PrefKey.HEIGHT_OFFSET,0).toString()


        btn_settings.setOnClickListener(this)
        btn_runstadia.setOnClickListener(this)
        btn_enter.setOnClickListener(this)
        iv_bluetooth.setOnClickListener {
            setupConnection()
        }
        btn_calibrate.setOnClickListener {

            val calibrate_value = sharedPreference.getInt("Calibrate_value", 0)
            if(sharedPreference?.getBoolean(PrefKey.isMetricMeasurement,true)){
                tv_CurrentOffsetValue.text=calibrate_value.toString()
            } else{
                tv_CurrentOffsetValue.text=calibrate_value.cmtoInches().toString()
            }

            sharedPreference.edit().putInt(PrefKey.HEIGHT_OFFSET,tv_CurrentOffsetValue.text.toString().toInt()).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        if(sharedPreference?.getBoolean(PrefKey.isMetricMeasurement,true)){
            et_inches.hint="Cm"
        }
        else{
            et_inches.hint="Inches"

        }
    }
    override fun onReceivedData(height: String, battery: String) {
        onConnect()
    }

    override fun onConnect() {
        if(tv_tapButton.text.toString()!=getString(R.string.connected)) {
            if (customDialogFragment != null) {
                if (customDialogFragment!!.dialog?.isShowing == true) {
                    customDialogFragment?.dismiss()
                }
            }
            tv_tapButton.text = getString(R.string.connected)
            tv_tapButton.setTextColor(Color.GREEN)
        }
    }

    override fun onDisconnect() {
        runOnUiThread {
            tv_tapButton.text = getString(R.string.tap_button_to_connect_to_stadia_via_bluetooth)
            tv_tapButton.setTextColor(Color.RED)
        }

    }

    private fun setupConnection() {
        if (BluetoothHelper.isLocationPermissionsGranted(this)) {
            // Bluetooth must be enabled
            if (BluetoothHelper.isBleEnabled()) {
                if (BluetoothHelper.isLocationRequired(this) || BluetoothHelper.isLocationEnabled(
                        this
                    )
                ) {

                    customDialogFragment = CustomDialogFragment() {
                        it?.let {
                            connectBt(it)
                        }
                    }
                    supportFragmentManager?.let { customDialogFragment?.show(it, "dilaog") }

                } else {
                    val intent =
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }

        } else {
            BluetoothHelper.markLocationPermissionRequested(this)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACCESS_COARSE_LOCATION -> if (permissions[0] == Manifest.permission.ACCESS_COARSE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                setupConnection()
            } /*else {
                // Permission was denied. Display an error message.
            }*/
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_settings -> openA<SettingsActivity>()
            R.id.btn_runstadia -> openA<StadiaActivity>()
            R.id.btn_enter -> {

                tv_CurrentOffsetValue.text = et_inches.text.toString()
                sharedPreference.edit().putInt(PrefKey.HEIGHT_OFFSET,et_inches.text.toString().toInt()).apply()
            }
        }
    }
}