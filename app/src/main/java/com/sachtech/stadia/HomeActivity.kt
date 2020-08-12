package com.sachtech.stadia

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity:AppCompatActivity(), View.OnClickListener {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_runStadia.setOnClickListener(this)
        btn_setUpStadia.setOnClickListener(this)

        setupConnection()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btn_runStadia-> openA<DescriptionActivity>()

            R.id.btn_setUpStadia->openA<SetUpStadiaActivity>()

        }
    }


    fun setupConnection(){

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_SHORT).show();
            // Device doesn't support Bluetooth
        }

        else{
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
        }

    }


}