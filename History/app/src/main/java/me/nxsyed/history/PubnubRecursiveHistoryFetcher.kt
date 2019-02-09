package com.nxsyed.history

import android.util.Log
import com.pubnub.api.PubNub
import com.pubnub.api.PNConfiguration

import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.history.PNHistoryResult

import java.util.concurrent.CountDownLatch

class PubnubRecursiveHistoryFetcher internal constructor() {
    private val pubnub: PubNub

    abstract inner class CallbackSkeleton() {
        abstract fun handleResponse(result: PNHistoryResult)
    }

    init {
        // NOTICE: for demo/demo pub/sub keys Storage & Playback is disabled,
        // so use your pub/sub keys instead
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = "sub-c-87dbd99c-e470-11e8-8d80-3ee0fe19ec50"
        pubnub = PubNub(pnConfiguration)
    }

    fun fetch(){
        getAllMessages(object: CallbackSkeleton(){
            override fun handleResponse(result: PNHistoryResult){
                Log.d("pubnub", result.toString())
            }
        })
    }

    fun getAllMessages(callback: CallbackSkeleton) {
        Log.d("pubnub", "Entering")
        getAllMessages(null, callback)
    }

    fun getAllMessages(startTimestamp: Long?, callback: CallbackSkeleton) {
        var startTimestamp = startTimestamp
        val latch = CountDownLatch(1)

        if (startTimestamp == null) {
            startTimestamp = -1L
        }

        pubnub.history()
                .channel("whiteboard") // where to fetch history from
                .count(100) //  how many items to fetch
                .start(startTimestamp) // first timestamp
                .async(object : PNCallback<PNHistoryResult>() {
                    override fun onResponse(historyResult: PNHistoryResult, status: PNStatus) {
                        if (!status.isError && historyResult.messages.size != 0) {
                            println(historyResult.messages.size)
                            System.out.println("start: " + historyResult.startTimetoken)
                            System.out.println("end: " + historyResult.endTimetoken)
                            callback.handleResponse(historyResult)
                            getAllMessages(historyResult.endTimetoken, callback)
                        }
                    }
                })

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val fetcher = PubnubRecursiveHistoryFetcher
        }
    }
}