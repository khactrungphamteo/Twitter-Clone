package com.codepath.apps.restclienttemplate

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyCallback
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.ComposeActivity.Companion.REQUEST_CODE
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

    // TODO
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Handles click on menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.compose) {
            // whenever the user shows the icon, the message "Ready to compose tweet!" show up
            Toast.makeText(this, "Ready to compose tweet!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ComposeActivity::class.java)
            composeActivityResultLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    // this method is called after we come back from the ComposeActivity
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
//
//            // get the data from our intent
//            val tweet = data?.getParcelableExtra<Tweet>("tweet") as Tweet
//
//            // update timeline by adding add our new tweet to the existing Arraylist of tweet
//            tweets.add(0, tweet)
//
//            // update adapter
//            tweetAdapter.notifyItemInserted(0)
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    private var composeActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If the user comes back to this activity from EditActivity
        // with no error or cancellation
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            // get the data from our intent
            val tweet = data?.getParcelableExtra<Tweet>("tweet") as Tweet

            // update timeline by adding our new tweet
            tweets.add(0, tweet)

            // update adapter
            tweetAdapter.notifyItemInserted(0)
            rvTweets.smoothScrollToPosition(0)
        }
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