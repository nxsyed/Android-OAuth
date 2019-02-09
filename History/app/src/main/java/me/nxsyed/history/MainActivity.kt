package me.nxsyed.history

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import java.util.*
import com.pubnub.api.models.consumer.history.PNHistoryResult
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNPublishResult

import com.nxsyed.history.PubnubRecursiveHistoryFetcher
import java.net.CacheResponse
import java.util.concurrent.CountDownLatch

class MainActivity : AppCompatActivity() {

    private val pubNub: PubNub

    init {
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = "sub-c-87dbd99c-e470-11e8-8d80-3ee0fe19ec50"
        pnConfiguration.publishKey = "pub-c-09557b6c-9513-400f-a915-658c0789e264"
        pubNub = PubNub(pnConfiguration)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val publishText = findViewById<EditText>(R.id.editTextPublish)
        val subscribeText = findViewById<TextView>(R.id.textViewSubscribe)
        val historyButtonAll = findViewById<Button>(R.id.buttonHistoryAll)
        val historyButtonLast = findViewById<Button>(R.id.buttonHistoryLast)
        val historyButtonTen = findViewById<Button>(R.id.buttonHistoryTen)

        pubNub.run {
            addListener(object : SubscribeCallback()  {
                override fun status(pubnub: PubNub, status: PNStatus) {

                }
                override fun message(pubnub: PubNub, message: PNMessageResult) {
                    runOnUiThread {
                        subscribeText.text = message.message.toString()
                    }
                }
                override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
                }
            })
            subscribe()
                    .channels(Arrays.asList("whiteboard")) // subscribe to channels
                    .execute()
        }

        historyButtonLast.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                pubNub.history()
                        .channel("whiteboard") // where to fetch history from
                        .count(1) // how many items to fetch
                        .async(object : PNCallback<PNHistoryResult>() {
                            override fun onResponse(result: PNHistoryResult, status: PNStatus) {
                                subscribeText.text = result.messages.toString()
                            }
                        })
            }
        })

        historyButtonTen.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                pubNub.history()
                        .channel("whiteboard") // where to fetch history from
                        .count(10) // how many items to fetch
                        .start(13847168620721752L) // first timestamp
                        .end(21847168620721752L) // last timestamp
                        .async(object : PNCallback<PNHistoryResult>() {
                            override fun onResponse(result: PNHistoryResult, status: PNStatus) {
                                subscribeText.text = result.messages.toString()
                            }
                        })
            }
        })

        historyButtonAll.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                getAllMessages(21847168620721752L, object: CallbackSkeleton(){
                    override fun handleResponse(result: PNHistoryResult) {
                        subscribeText.append(result.messages.toString())
                    }
                }, 10)
            }
        })

        publishText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                pubNub.run {
                    publish()
                            .message(publishText.text.toString())
                            .channel("whiteboard")
                            .async(object : PNCallback<PNPublishResult>() {
                                override fun onResponse(result: PNPublishResult, status: PNStatus) {
                                    if (!status.isError) {
                                        println("Message was published")
                                    }else {
                                        println("Could not publish")
                                    }
                                }
                            })
                }
                return@OnKeyListener true
            }
            false
        })
    }

    abstract inner class CallbackSkeleton() {
        abstract fun handleResponse(result: PNHistoryResult)
    }


    fun getAllMessages(startTimestamp: Long?, callback: CallbackSkeleton, count: Int) {
        var startTimestamp = startTimestamp

        Log.d("Time", startTimestamp.toString())
        pubNub.history()
                .channel("whiteboard") // where to fetch history from
                .count(count) //  how many items to fetch
                .start(startTimestamp) // first timestamp
                .async(object : PNCallback<PNHistoryResult>() {
                    override fun onResponse(historyResult: PNHistoryResult, status: PNStatus) {
                        Log.d("Message", historyResult.messages.toString())
                        if (!status.isError && historyResult.messages.size === count) {
                            println(historyResult.messages.size)
                            System.out.println("start: " + historyResult.startTimetoken)
                            System.out.println("end: " + historyResult.endTimetoken)
                            callback.handleResponse(historyResult)
                            getAllMessages(historyResult.endTimetoken, callback, count)
                        }else{
                            println(historyResult.messages.size)
                            System.out.println("start: " + historyResult.startTimetoken)
                            System.out.println("end: " + historyResult.endTimetoken)
                            callback.handleResponse(historyResult)
                        }
                    }
                })

    }

}
