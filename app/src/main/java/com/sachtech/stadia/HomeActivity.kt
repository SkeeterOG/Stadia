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

class HomeActivity : AppCompatActivity(), View.OnClickListener {


    private var fragmentManager: FragmentManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_runStadia.setOnClickListener(this)
        btn_setUpStadia.setOnClickListener(this)


        fragmentManager = supportFragmentManager

    }

    override fun onResume() {
        super.onResume()
        // setupConnection()
    }




    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_runStadia -> openA<DescriptionActivity>()
            R.id.btn_setUpStadia -> openA<SetUpStadiaActivity>()

        }
    }

}