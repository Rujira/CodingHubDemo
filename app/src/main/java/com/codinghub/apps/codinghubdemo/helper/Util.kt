package com.codinghub.apps.codinghubdemo.helper

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.replaceFragmenty(fragment: Fragment, allowStateLoss: Boolean = false, @IdRes containerViewId: Int) {

    val ft = supportFragmentManager
        .beginTransaction()
        .replace(containerViewId, fragment)

    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}