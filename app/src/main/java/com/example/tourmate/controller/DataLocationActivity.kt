package com.example.tourmate.controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerLocation0nClickListener
import com.example.tourmate.databinding.ActivityDataLocationBinding
import com.example.tourmate.model.DataLocation
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataLocationAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class DataLocationActivity : AppCompatActivity(), RecyclerLocation0nClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy {
        ActivityDataLocationBinding.inflate(layoutInflater)
    }
    private var progressDialog: MaterialDialog? = null
    private var dataLocationList = ArrayList<DataLocation>()
    private lateinit var dataLocationAdapter: DataLocationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        val cityId: String = intent.getStringExtra("city_id").toString()
        dataLocationList = ArrayList()
        dataLocationAdapter = DataLocationAdapter(this, dataLocationList)
        dataLocationAdapter.setOnItemClickListener(this)
        binding.recycleViewDataLocation.layoutManager = LinearLayoutManager(this)
        binding.recycleViewDataLocation.adapter = dataLocationAdapter
        GlobalScope.launch {
            while (true) {
                getData(cityId)
                delay(1000)
            }
        }

        binding.searchViewLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListDataLocation(newText)
                return true
            }
        })
        binding.searchViewLocation.setOnCloseListener {
            if (dataLocationList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE

            }
            runOnUiThread {

                dataLocationAdapter.notifyDataSetChanged()
            }
            Log.d("eizeeez", dataLocationList.size.toString())
            true
        }
    }

    private fun getData(cityId: String?) {
        val api = RetrofitInstance.api
        if (cityId != null) {
            api.getLocationsByCity(cityId).enqueue(object : Callback<List<DataLocation>> {
                override fun onResponse(
                    call: Call<List<DataLocation>>,
                    response: Response<List<DataLocation>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            dataLocationList.clear()
                            dataLocationList.addAll(it)
                            dataLocationList.sortBy { it.english_name }
                        }
                        dataLocationAdapter.notifyDataSetChanged()
                        progressDialog?.dismiss()

                    }
                }

                override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                    Toast.makeText(this@DataLocationActivity, t.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            })
        }
    }

    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataLocation>()
            for (i in dataLocationList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE
                dataLocationAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewDataLocation.visibility = View.VISIBLE
            dataLocationAdapter.setFilteredList(dataLocationList)

        }
    }

    override fun onItemClick(dataLocation: DataLocation) {
        val intent = Intent(this, DetailLocationActivity::class.java)
        intent.putExtra("location_id", dataLocation.id.toString())
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.favorite -> {
                val intent = Intent(this, MyFavoriteActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.saved_place -> {
                val intent = Intent(this, SavedPlaceActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> false
        }
    }

}