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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerFavoriteOnClickListener
import com.example.tourmate.databinding.ActivityMyFavoriteBinding
import com.example.tourmate.model.ErrorResponse
import com.example.tourmate.model.ViewFavoriteLocation
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.ViewFavoriteAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MyFavoriteActivity : AppCompatActivity(), RecyclerFavoriteOnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy {
        ActivityMyFavoriteBinding.inflate(layoutInflater)
    }
    private var favoriteList = ArrayList<ViewFavoriteLocation>()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewFavoriteAdapter: ViewFavoriteAdapter
    private var myJob: Job? = null
    private var progressDialog: MaterialDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
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
        binding.navigationView.menu.findItem(R.id.favorite).isChecked = true
        favoriteList = ArrayList()
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        viewFavoriteAdapter = ViewFavoriteAdapter(this, favoriteList)
        viewFavoriteAdapter.setOnItemClickListener(this)
        binding.recycleViewFavorite.layoutManager = LinearLayoutManager(this)
        binding.recycleViewFavorite.adapter = viewFavoriteAdapter
        myJob = GlobalScope.launch {
            while (true) {
                getData(auth.uid)
                delay(3000)
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFavoriteList(newText)
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            if (favoriteList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewFavorite.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewFavorite.visibility = View.VISIBLE

            }
            runOnUiThread {

                viewFavoriteAdapter.notifyDataSetChanged()
            }
            Log.d("eizeeez", favoriteList.size.toString())
            true
        }
    }

    private fun filterFavoriteList(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<ViewFavoriteLocation>()
            for (i in favoriteList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewFavorite.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewFavorite.visibility = View.VISIBLE
                viewFavoriteAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewFavorite.visibility = View.VISIBLE
            viewFavoriteAdapter.setFilteredList(favoriteList)

        }
    }

    private fun getData(uid: String?) {
        val api = RetrofitInstance.api
        if (uid != null) {
            api.getFavoriteListByUidd(uid)
                .enqueue(object : Callback<List<ViewFavoriteLocation>> {
                    override fun onResponse(
                        call: Call<List<ViewFavoriteLocation>>,
                        response: Response<List<ViewFavoriteLocation>>
                    ) {
                        if (response.isSuccessful) {
                            favoriteList.clear()
                            response.body()?.let {
                                favoriteList.addAll(it)
                                favoriteList.sortBy { it.english_name }
                            }
                            Log.d("img_url", response.body().toString())
                            viewFavoriteAdapter.notifyDataSetChanged()
                            if (favoriteList.isNullOrEmpty()) {
                                binding.textViewEmptyNotice.visibility = View.VISIBLE
                            } else {
                                binding.textViewEmptyNotice.visibility = View.GONE
                            }
                            progressDialog?.dismiss()
                        }
                    }

                    override fun onFailure(call: Call<List<ViewFavoriteLocation>>, t: Throwable) {
                        Toast.makeText(this@MyFavoriteActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }

                })
        }
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

    override fun onItemClick(viewFavoriteLocation: ViewFavoriteLocation) {
        val intent = Intent(this, DetailLocationActivity::class.java)
        intent.putExtra("location_id", viewFavoriteLocation.location_id.toString())
        startActivity(intent)
    }

    private fun deleteFavoriteLocation(viewFavoriteLocation: ViewFavoriteLocation) {
        val api = RetrofitInstance.api
        val call = api.deleteFavoriteListByUid(
            uid = auth.uid ?: "",
            locationId = viewFavoriteLocation.location_id
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    if (responseData != null && responseData == "success") {
                        Toast.makeText(
                            this@MyFavoriteActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MyFavoriteActivity,
                            getString(R.string.fail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = Gson().fromJson(
                        response.errorBody()?.string(),
                        ErrorResponse::class.java
                    )
                    val errorMessage = error?.message ?: getString(R.string.unknown_error)
                    Toast.makeText(
                        this@MyFavoriteActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@MyFavoriteActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MyFavoriteActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onDeleteItemClick(viewFavoriteLocation: ViewFavoriteLocation) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notice))
        builder.setMessage(getString(R.string.delete_favorite_location))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteFavoriteLocation(viewFavoriteLocation)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()


    }

    override fun onStop() {
        super.onStop()
        myJob?.cancel()

    }

}