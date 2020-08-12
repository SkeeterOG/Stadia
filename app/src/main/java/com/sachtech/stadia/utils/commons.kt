package com.sachtech.stadia.utils

import android.content.Context
import android.content.Intent

inline fun <reified T> Context.openA(extras: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.extras()
    startActivity(intent)
}