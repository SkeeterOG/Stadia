package com.sachtech.stadia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.BluetoothConnector
import kotlinx.android.synthetic.main.activity_description.*

class DescriptionActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        btn_activeMode.setOnClickListener(this)
        btn_standBy.setOnClickListener(this)
    }

    override fun onHeightAlert() {

        tv_warning.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    override fun onReceivedData(height: String, battery: String) {
        if (height.isNotEmpty()) {
            if(height.contains("StandBy",true)){
                tv_heightftvalue.text=""+height
                tv_warning.visibility = View.GONE
            }else {
                val heightInt = height.toInt()
                tv_heightftvalue.text = "" + (heightInt * 0.0328)


            }

        }

    }

    override fun onConnect() {


    }

    override fun onDisconnect() {
        Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_activeMode -> {
           writeData("0")

            }
            R.id.btn_standBy -> {
                writeData("1")
            }

        }

    }


}