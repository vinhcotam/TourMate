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
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerCity0nClickListener
import com.example.tourmate.databinding.ActivityMainBinding
import com.example.tourmate.model.DataCity
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataCityAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerCity0nClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var dataCityList = ArrayList<DataCity>()
    private lateinit var dataCityAdapter: DataCityAdapter
    private var progressDialog: MaterialDialog? = null
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
        binding.navigationView.menu.findItem(R.id.home).isChecked = true
        dataCityList = ArrayList()
        dataCityAdapter = DataCityAdapter(dataCityList)
        dataCityAdapter.setOnItemClickListener(this)
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }

        val layoutManager = GridLayoutManager(this, 2)
        binding.recycleViewDataLocation.layoutManager = layoutManager
        binding.recycleViewDataLocation.adapter = dataCityAdapter
        getData()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListDataLocation(newText)
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            if (dataCityList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE

            }
            runOnUiThread {

                dataCityAdapter.notifyDataSetChanged()
            }
            Log.d("eizeeez", dataCityList.size.toString())
            true
        }
    }

    private fun getData() {
        val api = RetrofitInstance.api
        api.getDataCity().enqueue(object : Callback<List<DataCity>> {
            override fun onResponse(
                call: Call<List<DataCity>>,
                response: Response<List<DataCity>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { it ->
                        dataCityList.clear()
                        dataCityList.addAll(it)
//                        dataCityList.sortBy { it.city_name }
                    }
                    dataCityAdapter.notifyDataSetChanged()
                    progressDialog?.dismiss()

                }
            }

            override fun onFailure(call: Call<List<DataCity>>, t: Throwable) {
                Toast.makeText(binding.root.context, t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataCity>()
            for (i in dataCityList) {
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
                dataCityAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewDataLocation.visibility = View.VISIBLE
            dataCityAdapter.setFilteredList(dataCityList)
            Log.d("eizeeez", dataCityList.size.toString())
        }
    }

    override fun onItemClick(dataCity: DataCity) {
        val intent = Intent(this, DataLocationActivity::class.java)
        intent.putExtra("city_id", dataCity.city_id)
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }


}
