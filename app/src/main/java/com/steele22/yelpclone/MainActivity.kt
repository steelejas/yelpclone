package com.steele22.yelpclone

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "Z_oUnYp016iVQDwXEj1TkzS1YtVjeADnK9AAAhDPyAi_ZchxSbEIOU3tD31VrbQCDk6KVtm4ur7BbfifSbEXMzng7slhPH5Ptum_ThplBl21fkQCJZYanuK8QwCRYXYx"
private const val NO_NETWORK_MESSAGE = "Cannot find network connection. Please verify you are connected to the Internet and restart the app."
class MainActivity : AppCompatActivity() {
    private lateinit var rvRestaurants: RecyclerView
    private lateinit var tvNetworkMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvRestaurants = findViewById(R.id.rvRestaurants)
        tvNetworkMessage = findViewById(R.id.tvNetworkMessage)

        tvNetworkMessage.text = NO_NETWORK_MESSAGE
        if (networkIsAvailable() == true) {
            Log.i(TAG, "networkIsAvailable returned true")
            tvNetworkMessage.visibility = View.GONE
            rvRestaurants.visibility = View.VISIBLE
        } else {
            Log.i(TAG, "networkIsAvailable returned false")
            tvNetworkMessage.visibility = View.VISIBLE
            rvRestaurants.visibility = View.GONE
        }

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val retrofit =
            Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY", "Avocado Toast", "New York").enqueue(object : Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
                tvNetworkMessage.visibility = View.VISIBLE
            }

        })
    }

    private fun networkIsAvailable(): Boolean? {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}