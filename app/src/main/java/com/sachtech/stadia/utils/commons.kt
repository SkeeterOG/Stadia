package com.sachtech.stadia.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager

inline fun <reified T> Context.openA(extras: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.extras()
    startActivity(intent)

}

fun Context.sharedPreferences(){
    val mSharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    var mEditor: SharedPreferences.Editor = mSharedPreferences.edit()
}