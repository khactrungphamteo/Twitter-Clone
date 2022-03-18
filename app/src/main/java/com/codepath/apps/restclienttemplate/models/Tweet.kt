package com.codepath.apps.restclienttemplate.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject

@Parcelize
class Tweet(var body: String = "", var createAt: String = "", var user: User? = null) : Parcelable {
    companion object {
        fun fromJSON(jsonObject: JSONObject): Tweet {
            val tweet = Tweet()
            tweet.body = jsonObject.getString("text")
            tweet.createAt = TimeFormatter.getTimeDifference(jsonObject.getString("created_at"))
            tweet.user = User.fromJSON(jsonObject.getJSONObject("user"))
            return tweet
        }

        // method that converts a list of tweets into a list of JSON objects

        fun fromJsonArray(jsonArray: JSONArray): List<Tweet> {
            val tweets = ArrayList<Tweet>()
            for (i in 0 until jsonArray.length()) {
                tweets.add(fromJSON(jsonArray.getJSONObject(i)))
            }

            return tweets
        }
    }
}