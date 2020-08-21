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
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_setupstadia.*

class SetUpStadiaActivity : BaseActivity(), View.OnClickListener {
    private val REQUEST_ACCESS_COARSE_LOCATION = 1022 // random number
    private var customDialogFragment: CustomDialogFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tv_CurrentOffsetValue.text=sharedPreference.getInt(PrefKey.Height_Inches,0).toString()

        setContentView(R.layout.activity_setupstadia)
        btn_settings.setOnClickListener(this)
        btn_runstadia.setOnClickListener(this)
        btn_enter.setOnClickListener(this)
    }

    override fun onHeightAlert() {


    }

    override fun onReceivedData(height: String, battery: String) {

    }

    override fun onConnect() {
        if (customDialogFragment != null) {
            if (customDialogFragment!!.dialog?.isShowing == true) {
                customDialogFragment?.dismiss()
            }
        }
        tv_tapButton.text = getString(R.string.connected)
        tv_tapButton.setTextColor(Color.GREEN)
    }

    override fun onDisconnect() {
        tv_tapButton.text = getString(R.string.tap_button_to_connect_to_stadia_via_bluetooth)
        tv_tapButton.setTextColor(Color.RED)
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
            R.id.btn_runstadia -> openA<DescriptionActivity>()
            R.id.btn_enter -> {

                tv_CurrentOffsetValue.text = et_inches.text.toString()
                sharedPreference.edit().putInt(PrefKey.Height_Inches,et_inches.text.toString().toInt()).apply()
            }
        }
    }
}