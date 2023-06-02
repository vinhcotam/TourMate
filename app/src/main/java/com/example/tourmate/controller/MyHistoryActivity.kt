package com.example.tourmate.controller

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.controller.interfaces.RecyclerHistoryOnClickListener
import com.example.tourmate.databinding.ActivityMyHistoryBinding
import com.example.tourmate.model.DataLocation
import com.example.tourmate.model.ErrorResponse
import com.example.tourmate.model.History
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataLocationAdapter
import com.example.tourmate.view.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MyHistoryActivity : BaseActivity(), RecyclerHistoryOnClickListener {
    private val binding by lazy {
        ActivityMyHistoryBinding.inflate(layoutInflater)
    }
    private var myJob: Job? = null
    private var itineraryList = ArrayList<DataLocation>()
    private var dataLocationList = ArrayList<DataLocation>()
    private var historyList = ArrayList<History>()
    private lateinit var auth: FirebaseAuth
    private lateinit var historyAdapter: HistoryAdapter
    private var progressDialog: MaterialDialog? = null
    private lateinit var locationAdapter: DataLocationAdapter
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
        binding.navigationView.menu.findItem(R.id.history).isChecked = true
        historyList = ArrayList()
        dataLocationList = ArrayList()
        itineraryList = ArrayList()
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        locationAdapter = DataLocationAdapter(this, itineraryList)
        historyAdapter = HistoryAdapter(this, historyList, dataLocationList)
        historyAdapter.setOnItemClickListener(this)
        binding.recycleHistory.layoutManager = LinearLayoutManager(this)
        binding.recycleHistory.adapter = historyAdapter
        getDataLocation()
//        getData(auth.uid)
        myJob = GlobalScope.launch {
            while (true) {
                getData(auth.uid)
                delay(15000)
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterHistory(newText)
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            if (historyList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleHistory.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleHistory.visibility = View.VISIBLE

            }
            runOnUiThread {

                historyAdapter.notifyDataSetChanged()
            }
            true
        }
    }

    private fun filterHistory(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<History>()
            for (i in historyList) {
                if (i.itinerary.lowercase(Locale.ROOT).contains(newText) || i.date.lowercase(
                        Locale.ROOT
                    ).contains(newText)
                ) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleHistory.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleHistory.visibility = View.VISIBLE
                historyAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleHistory.visibility = View.VISIBLE
            historyAdapter.setFilteredList(historyList)

        }
    }


    private fun getDataLocation() {
        val api = RetrofitInstance.api
        api.getLocations().enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        dataLocationList.clear()
                        dataLocationList.addAll(it)
                    }
                }

                Log.d("ádfgfh1", dataLocationList.size.toString())

            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {

            }

        })
    }

    private fun getData(uid: String?) {
        val api = RetrofitInstance.api
        if (uid != null) {
            api.getHistoryByUid(uid)
                .enqueue(object : Callback<List<History>> {
                    override fun onResponse(
                        call: Call<List<History>>,
                        response: Response<List<History>>
                    ) {
                        if (response.isSuccessful) {
                            historyList.clear()
                            response.body()?.let {
                                historyList.addAll(it)
                                historyList.sortBy { it.date }

                            }
                            historyAdapter.notifyDataSetChanged()
                            if (historyList.isEmpty()) {
                                binding.textViewEmptyNotice.visibility = View.VISIBLE
                            } else {
                                binding.textViewEmptyNotice.visibility = View.GONE
                            }
                            progressDialog?.dismiss()
                        }
                        Log.d("ádfgfh", historyList.size.toString())


                    }

                    override fun onFailure(call: Call<List<History>>, t: Throwable) {
                        Log.d("Ressss", t.toString())
                        Toast.makeText(this@MyHistoryActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                        progressDialog?.dismiss()

                    }

                })
        }

    }

    override fun onDeleteItemClick(history: History) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notice))
        builder.setMessage(getString(R.string.delete_favorite_location))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteDetailHistory(history)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteHistory(history: History) {
        val api = RetrofitInstance.api
        val call = api.deleteHistory(
            uid = auth.uid ?: "",
            id = history.id
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                Log.d("ảdfg",response.toString())
                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    if (responseData != null && responseData == "success") {
                        Toast.makeText(
                            this@MyHistoryActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                        getData(auth.uid)

                    } else {
                        Toast.makeText(
                            this@MyHistoryActivity,
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
                        this@MyHistoryActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@MyHistoryActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MyHistoryActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun deleteDetailHistory(history: History) {
        val api = RetrofitInstance.api
        val call = api.deleteDetailHistory(
            uid = auth.uid ?: "",
            history_id = history.id
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
                            this@MyHistoryActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                        getData(auth.uid)
                        deleteHistory(history)
                    } else {
                        Toast.makeText(
                            this@MyHistoryActivity,
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
                        this@MyHistoryActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@MyHistoryActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MyHistoryActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        myJob?.cancel()
    }
}