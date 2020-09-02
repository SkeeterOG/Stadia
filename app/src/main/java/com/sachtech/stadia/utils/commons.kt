package com.sachtech.stadia.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.math.roundToInt

inline fun <reified T> Context.openA(extras: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.extras()
    startActivity(intent)

}

fun Context.sharedPreferences(){
    val mSharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    var mEditor: SharedPreferences.Editor = mSharedPreferences.edit()
}
fun String.uptoTwoDecimal(): String {
    return String.format("%.2f", this.toDouble());
}
fun Int.cmtoInches():Int{
    return (this/2.54).roundToInt()

}
fun Int.cmtoMeters():Double{
    return (this/100.0)

}
fun Int.cmtoFeet():Int{
    return (this/30.48).roundToInt()

}
fun Int.inchestoFeet():Double{
    return (this/12.0)

}
