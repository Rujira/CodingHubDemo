package com.codinghub.apps.codinghubdemo.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.core.view.GravityCompat

import android.view.MenuItem

import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.codinghub.apps.codinghubdemo.R
import com.codinghub.apps.codinghubdemo.app.Injection
import com.codinghub.apps.codinghubdemo.helper.replaceFragmenty
import com.codinghub.apps.codinghubdemo.ui.addnewface.AddNewFaceActivity
import com.codinghub.apps.codinghubdemo.ui.compare.CompareFragment
import com.codinghub.apps.codinghubdemo.ui.face.FaceFragment
import com.codinghub.apps.codinghubdemo.ui.licenseplate.LicensePlateFragment
import com.codinghub.apps.codinghubdemo.ui.liveness.LivenessFragment
import com.codinghub.apps.codinghubdemo.viewmodel.AddNewFaceViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        replaceFragmenty(fragment = FaceFragment(),
            allowStateLoss = true,
            containerViewId = R.id.main_content)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        navView.menu.getItem(0).setChecked(true)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)


    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                addNewPerson()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_face -> {
                replaceFragmenty(fragment = FaceFragment(),
                   allowStateLoss = true,
                   containerViewId = R.id.main_content)
            }

            R.id.nav_compare -> {
                replaceFragmenty(fragment = CompareFragment(),
                    allowStateLoss = true,
                    containerViewId = R.id.main_content)
            }

            R.id.nav_license_plate -> {
                replaceFragmenty(fragment = LicensePlateFragment(),
                    allowStateLoss = true,
                    containerViewId = R.id.main_content)
            }

            R.id.nav_liveness -> {
                replaceFragmenty(fragment = LivenessFragment(),
                    allowStateLoss = true,
                    containerViewId = R.id.main_content)
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addNewPerson() {
        val intent = Intent(this@MainActivity, AddNewFaceActivity::class.java)
        startActivity(intent)
    }
}
