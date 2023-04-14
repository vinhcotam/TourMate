package com.example.tourmate.controller


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.example.tourmate.databinding.ActivityMainBinding
import com.example.tourmate.model.DataCity
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataCityAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), RecyclerCity0nClickListener   {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var dataCityList = ArrayList<DataCity>()
    private lateinit var dataCityAdapter: DataCityAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        dataCityList = ArrayList()
        dataCityAdapter = DataCityAdapter(dataCityList)
        dataCityAdapter.setOnItemClickListener(this) // set listener của activity cho adapter
        binding.recycleViewDataLocation.adapter = dataCityAdapter
        val api = RetrofitInstance.api
        api.getDataCity().enqueue(object : Callback<List<DataCity>> {
            override fun onResponse(
                call: Call<List<DataCity>>,
                response: Response<List<DataCity>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { dataCityList.addAll(it) }
                    dataCityAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<DataCity>>, t: Throwable) {
                Toast.makeText(binding.root.context, t.toString(), Toast.LENGTH_LONG).show()
            }

        })
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

    }

    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataCity>()
            for (i in dataCityList) {
                if (i.city_name.lowercase(Locale.ROOT).contains(newText)) {
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
        }
    }

    override fun onItemClick(dataCity: DataCity) {
        Toast.makeText(this, "Clicked on city ${dataCity.city_name} (ID: ${dataCity.city_id})", Toast.LENGTH_LONG).show()
        val intent = Intent(this, DataLocationActivity::class.java)
        intent.putExtra("city_id", dataCity.city_id)
        startActivity(intent)
    }

}
