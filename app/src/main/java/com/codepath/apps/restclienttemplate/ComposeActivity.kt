package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {
    lateinit var etCompose: EditText
    lateinit var btnTweet: Button
    lateinit var etVal: TextView
    lateinit var client: TwitterClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)
        etVal = findViewById(R.id.tvCharCount)
        client = TwitterApplication.getRestClient(this)

        etCompose.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val remainingChar = 280 - count
                etVal.text = "$remainingChar characters remaining"
                if (remainingChar <= 0) {
                    etVal.text = "0 characters remaining"

                    btnTweet.isClickable = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                btnTweet.isClickable = true

            }
        })

        // Handling the user's click on the tweet button
        btnTweet.setOnClickListener {
            // grab the content of edittext (etCompose)
            val tweetContent = etCompose.text.toString()

            // make sure the tweet is not empty
            if (tweetContent.isEmpty()) {
                Toast.makeText(this, "Empty tweeets not all", Toast.LENGTH_SHORT).show()
                // look into displaying SnackBar message
            }

            // make sure the tweet is under character count
            if (tweetContent.length > 280) {
                Toast.makeText(this, "Tweet is too long! limit is 148 characters", Toast.LENGTH_SHORT).show()
            } else {
                // Make an api call to Twitter to publish tweet
                Toast.makeText(this, tweetContent, Toast.LENGTH_SHORT).show()
                client.publishTweet(tweetContent, object: JsonHttpResponseHandler() {
                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?
                    ) {
                        Log.e(TAG, "API call is not successfull $statusCode", throwable)
                        Log.e(TAG, "$response")
                    }

                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.i(TAG, "Successfully published tweet")

                        val tweet = Tweet.fromJSON(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet", tweet)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
            }
        }
    }

    companion object {
        const val TAG = "ComposeActivity"
        const val REQUEST_CODE = 20
    }

}