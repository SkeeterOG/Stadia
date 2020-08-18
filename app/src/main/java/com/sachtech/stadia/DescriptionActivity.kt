package com.sachtech.stadia

import android.os.Bundle
import android.view.View
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

    override fun onReceivedData(height: String, battery: String) {
        if (height.isNotEmpty()) {
            // tv_heightftvalue.text=height

        }

    }

    override fun onConnect() {


    }

    override fun onDisconnect() {

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