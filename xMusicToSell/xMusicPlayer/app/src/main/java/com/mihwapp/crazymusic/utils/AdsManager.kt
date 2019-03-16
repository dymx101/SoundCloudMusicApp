package com.mihwapp.crazymusic.utils

import android.content.Context
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.google.android.gms.ads.*
import com.mihwapp.crazymusic.R
import com.mihwapp.crazymusic.constants.IXMusicConstants
import com.mihwapp.crazymusic.setting.YPYSettingManager
import com.mihwapp.crazymusic.task.IYPYCallback
import java.util.*
import java.util.concurrent.TimeUnit

class AdsManager {

    companion object {
        private const val TAG = "AdsManager"
        private const val adShowInterval = 300

        fun getInstance(): AdsManager {
            return Holder.instance
        }
    }
    private object Holder {
        val instance = AdsManager()
    }

    private fun adsEnabled(): Boolean = IXMusicConstants.SHOW_ADS

    private var adView: AdView? = null

    private val adViewContainer: ViewGroup? get() = adView?.parent as? ViewGroup

    private var interstitialAd: InterstitialAd? = null

    private val mHandlerAds = Handler()

    private var context: Context? = null

    fun setup(context: Context) {

        if (!adsEnabled()) {
            return
        }

        this.context = context

        MobileAds.initialize(context, IXMusicConstants.ADMOB_APP_ID)

        initBanner()

        initInterstitial()
    }

    private fun initBanner() {
        adView = AdView(context)
        adView?.let {
            it.adUnitId = IXMusicConstants.ADMOB_BANNER_ID
            it.adSize = AdSize.SMART_BANNER

            it.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    DBLog.d(TAG, "===========> Ad view loaded")
                }
            }

            val mAdRequest = AdRequest.Builder().addTestDevice(IXMusicConstants.ADMOB_TEST_DEVICE).build()
            it.loadAd(mAdRequest)
        }
    }

    private fun showBanner() {
        adViewContainer?.visibility = View.VISIBLE
    }

    private fun hideBanner() {
        adViewContainer?.visibility = View.GONE
    }

    fun installBanner(parent: ViewGroup) {
        if (!adsEnabled()) {
            return
        }

        (adView?.parent as? ViewGroup)?.let {
            it.removeView(adView)
        }

        adView?.let {
            parent.addView(it)
            parent.visibility = View.VISIBLE
        }
    }

    private fun saveAdShowTime(showTime: Date) {
        YPYSettingManager.setShowAdsTime(context, showTime.time)
    }

    private fun adShowTime(): Date {
        val milliseconds = YPYSettingManager.getShowAdsTime(context)
        return Date(milliseconds)
    }

    private fun goodTimeToShowAds(): Boolean {

        if (!adsEnabled()) {
            return false
        }

        val adShowTime = adShowTime()
        val now = Date()

        if (adShowTime.time <= 0) {
            saveAdShowTime(now)
            return false
        }

        val interval = now.time - adShowTime.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(interval)
        return seconds > adShowInterval
    }

    public fun showInterstitial() {
        if (!goodTimeToShowAds()) {
            return
        }

        interstitialAd?.let {
            if (!it.isLoaded) {
                return
            }

            try {
                it.show()
                saveAdShowTime(Date())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadInterstitial() {
        interstitialAd?.let {
            if (!it.isLoaded || it.isLoading) {
                val adRequest = AdRequest.Builder().addTestDevice(IXMusicConstants.ADMOB_TEST_DEVICE).build()
                it.loadAd(adRequest)
            }
        }
    }

    private fun initInterstitial() {
        interstitialAd = InterstitialAd(context)

        interstitialAd?.let {
            it.adUnitId = IXMusicConstants.ADMOB_INTERSTITIAL_ID
            it.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    loadInterstitial()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    loadInterstitial()
                }

                override fun onAdFailedToLoad(i: Int) {
                    super.onAdFailedToLoad(i)
                    loadInterstitial()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    loadInterstitial()
                }
            }

            loadInterstitial()

        }
    }
}