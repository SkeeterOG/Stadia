package com.sachtech.stadia

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.sachtech.stadia.base.BaseInteractionListener
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(), View.OnClickListener, BaseInteractionListener {

    private val REQUEST_ACCESS_COARSE_LOCATION = 1022 // random number

    private var fragmentManager: FragmentManager? = null
    private var customDialogFragment: CustomDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_runStadia.setOnClickListener(this)
        btn_setUpStadia.setOnClickListener(this)
        start_scan.setOnClickListener(this)

        fragmentManager = supportFragmentManager
        setupConnection()
    }

    override fun onResume() {
        super.onResume()
        // setupConnection()
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

        tv_notconnected.setText("Connected")
        tv_notconnected.setTextColor(Color.GREEN)
    }

    override fun onDisconnect() {
        tv_notconnected.setText("Disconnected")
        tv_notconnected.setTextColor(Color.RED)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_runStadia -> openA<DescriptionActivity>()
            R.id.btn_setUpStadia -> openA<SetUpStadiaActivity>()
            R.id.start_scan -> setupConnection()
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
                    fragmentManager?.let { customDialogFragment?.show(it, "dilaog") }

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

    override fun connectBt(device: BluetoothDevice) {
        onConnectBt(device)
    }

    override fun writeDat(data: String) {
        writeData(data)
    }

}