package com.sachtech.stadia

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_setupstadia.*

class SetUpStadiaActivity:AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setupstadia)
        btn_settings.setOnClickListener(this)
        btn_runstadia.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btn_settings->openA<SettingsActivity>()
            R.id.btn_runstadia->openA<DescriptionActivity>()
        }
    }
}