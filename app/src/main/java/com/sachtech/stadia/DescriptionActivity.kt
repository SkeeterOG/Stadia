package com.sachtech.stadia

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sachtech.stadia.utils.PrefKey
import kotlinx.android.synthetic.main.activity_description.*

class DescriptionActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        btn_activeMode.setOnClickListener(this)
        btn_standBy.setOnClickListener(this)
    }


    @SuppressLint("SetTextI18n")
    override fun onReceivedData(height: String, battery: String) {
        if (height.isNotEmpty()) {
            if (height.contains("STANDBY", true)) {
                tv_heightftvalue.text = "" + height
                tv_warning.visibility = View.GONE
            } else {
                val heightInt = height.toInt()
                tv_heightftvalue.text = "" + (heightInt * 0.0328).toString().uptoTwoDecimal()
                if (isHeightAllert(heightInt)) {
                    if (sharedPreference?.getBoolean(PrefKey.VisualAlert, false)) {
                        tv_warning.visibility = View.VISIBLE
                    }
                } else {
                    tv_warning.visibility = View.GONE
                }

            }

        }

    }
    fun String.uptoTwoDecimal(): String {
      return  String.format("%.2f", this.toDouble());
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
                sharedPreference.edit().putString(PrefKey.DATA_COMMAND,"0").apply()
            }
            R.id.btn_standBy -> {
                sharedPreference.edit().putString(PrefKey.DATA_COMMAND,"1").apply()

            }

        }

    }


}