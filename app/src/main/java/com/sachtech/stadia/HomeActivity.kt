package com.sachtech.stadia

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.sachtech.stadia.utils.PrefKey
import com.sachtech.stadia.utils.openA
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), View.OnClickListener {


    private var fragmentManager: FragmentManager? = null
    val sharedPreference: SharedPreferences by lazy {
        getSharedPreferences(
            "PREFERENCE_NAME",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        btn_runStadia.setOnClickListener(this)
        btn_setUpStadia.setOnClickListener(this)
        sharedPreference.edit().putBoolean(PrefKey.isDeviceConnected,false).apply()


        fragmentManager = supportFragmentManager


    }

    override fun onResume() {
        super.onResume()
        // setupConnection()
        StadiaService.getInstance(this)
    }




    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_runStadia -> openA<StadiaActivity>()
            R.id.btn_setUpStadia -> openA<SetUpStadiaActivity>()

        }
    }

}