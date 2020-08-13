package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity:AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_runStadia.setOnClickListener(this)
        btn_setUpStadia.setOnClickListener(this)
        start_scan.setOnClickListener(this)

        setupConnection()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btn_runStadia-> openA<DescriptionActivity>()

            R.id.btn_setUpStadia->openA<SetUpStadiaActivity>()
            R.id.start_scan->setupConnection()

        }
    }


    fun setupConnection(){

        if (BluetoothHelper.bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
            // Device doesn't support Bluetooth
        }

        else{
            if (BluetoothHelper.isEnabled() == false) {

                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
            else{
                val fragmentManager: FragmentManager = supportFragmentManager
                val customDialogFragment = CustomDialogFragment()
                customDialogFragment.show(fragmentManager, "dilaog")
            }
        }

    }


}