package com.codepath.apps.restclienttemplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyCallback
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException


private const val TAG = "TimeLineActivity"

class TimeLineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient
    lateinit var rvTweets: RecyclerView
    lateinit var tweetAdapter: TweetAdapter
    lateinit var swipeContainer: SwipeRefreshLayout

    val tweets = ArrayList<Tweet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)

        // once the activity is launched
        client = TwitterApplication.getRestClient(this)

        tweetAdapter = TweetAdapter(tweets)

        // locate the Recycler Review in the layout
        rvTweets = findViewById(R.id.rvTweets)
        rvTweets.adapter = tweetAdapter
        rvTweets.layoutManager = LinearLayoutManager(this)


        // locate the swip container view
        swipeContainer = findViewById(R.id.swipeContainer)

        swipeContainer.setOnRefreshListener {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            Log.i(TAG, "refreshing timeline")
            populateHomeTimeLine()
        }

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );

        populateHomeTimeLine()

    }

    fun populateHomeTimeLine() {
        client.getHomeTimeLine(object : JsonHttpResponseHandler() {
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.e(TAG, "API call is not successful $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "API call successfull")
                val tweetsJsonArray = json.jsonArray
                try {
                    // clear out currently fetched tweets
                    tweetAdapter.clear()

                    val listOfTweets = Tweet.fromJsonArray(tweetsJsonArray)
                    tweets.addAll(listOfTweets)
                    tweetAdapter.notifyDataSetChanged()
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false)
                } catch (e: JSONException) {
                    Log.e(TAG, "Exception $e encountered when trying to parse JSON arrays")
                }

            }

        })
    }
}