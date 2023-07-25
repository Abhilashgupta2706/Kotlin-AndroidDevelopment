package com.practice.fragmentsnavmenu

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.practice.fragmentsnavmenu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        replaceFragment(DashboardFragment(), "Dashboard") // Loading Default Fragment

        toggle = ActionBarDrawerToggle(this, vb.navDrawerLayout, R.string.open_nav, R.string.close_nav)
        vb.navDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        vb.mainNavView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isCheckable = true // Making the clicked option highlighted
            when (menuItem.itemId) {
                R.id.menuDashboard -> replaceFragment(DashboardFragment(), menuItem.title.toString())
                R.id.menuMessage -> replaceFragment(MessageFragment(), menuItem.title.toString())
                R.id.menuShare -> replaceFragment(ShareFragment(), menuItem.title.toString())
                R.id.menuRateUs -> replaceFragment(RateUsFragment(), menuItem.title.toString())
                R.id.menuLogout -> Toast.makeText(applicationContext, "Logout", Toast.LENGTH_SHORT).show()
            }
            vb.navDrawerLayout.closeDrawer(GravityCompat.START) // Closing drawer after click
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainFrameLayout, fragment)
        fragmentTransaction.commit()

        setTitle(title) // Setting title in the toolbar for each fragment
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
