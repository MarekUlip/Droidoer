package com.marekulip.droidoer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView

import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import android.widget.Spinner


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        category = position
        fragment?.category = position
    }

    /**
     * Sub task category
     */
    var category:Int = 0
    /**
     * Indicates wheter activity is displaying completed main tasks
     */
    var displayingCompleted = false
    var fragment: TodoFragment? = null
    var spinner: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinner = spinner_nav
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        this.spinner = spinner
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
        } else if(category!= 0){
            spinner?.setSelection(0)
        }
        else{
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
