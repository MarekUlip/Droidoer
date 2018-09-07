package com.marekulip.droidoer

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter



class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        fragment?.category = position
    }

    var category:Int = 0
    var displayingCompleted = false
    var fragment: TodoFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinner = spinner_nav
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        fragment = TodoFragment.newInstance(category)
        supportFragmentManager.beginTransaction().replace(R.id.fragment,fragment as TodoFragment).commit()
        fab.setOnClickListener { _ ->
            fragment?.addMainTask()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        if (displayingCompleted){
            displayingCompleted = false
            fragment?.isDisplayingCompleted = false
        } else{
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_add_task -> {
                fragment?.addMainTask()
                true
            }

            R.id.display_completed -> {
                displayingCompleted = true
                fragment?.isDisplayingCompleted = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
