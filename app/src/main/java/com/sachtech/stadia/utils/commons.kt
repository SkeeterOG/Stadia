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
    return String.format("%.1f", this.toDouble());
}
fun Double.cmtoInches(): Double {
    return (this/2.54).toString().uptoTwoDecimal().toDouble()

}
fun Double.cmtoMeters():Double{
    return (this/100.0)

}
fun Double.cmtoFeet():Int{
    return (this/30.48).roundToInt()

}
fun Double.inchestoFeet():Double{
    return (this/12.0)

}
fun Int.inchestocm():Int{
    return (this*2.54).roundToInt()

}
