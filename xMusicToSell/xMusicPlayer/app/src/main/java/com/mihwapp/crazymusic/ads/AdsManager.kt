package com.mihwapp.crazymusic.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.mihwapp.crazymusic.constants.IXMusicConstants
import com.mihwapp.crazymusic.setting.YPYSettingManager
import com.mihwapp.crazymusic.utils.DBLog
import com.vungle.warren.*
import java.util.*
import java.util.concurrent.TimeUnit
import com.vungle.warren.error.VungleException





class AdsManager {

    companion object {
        private const val TAG = "AdsManager"
        private const val VUNGLE_INTESTITIAL_PLACEMENT_ID = "DEFAULT-4169153"
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

    fun setup(context: Context, activity: Activity, testMode: Boolean) {

        if (!adsEnabled()) {
            return
        }

        this.context = context

        UnityAdsManager.initialize(activity, testMode)

        MobileAds.initialize(context, IXMusicConstants.ADMOB_APP_ID)

        initBanner()

        initInterstitial()

        initializeVungleSDK(context)
    }

    private fun initializeVungleSDK(context: Context?) {
        context?.let {
            Vungle.init(IXMusicConstants.VUNGLE_APP_ID, context, object: InitCallback {
                override fun onSuccess() {
                    Log.d(TAG, "Vungle Init success")
                    loadVungleAd()
                }

                override fun onAutoCacheAdAvailable(p0: String?) {
                    Log.d(TAG, "Vungle Init Auto cache available")
                }

                override fun onError(p0: Throwable?) {
                    Log.d(TAG, "Vungle Init error")
                }

            })
        }
    }

    private fun loadVungleAd() {
        if (Vungle.isInitialized()) {
            Vungle.loadAd(VUNGLE_INTESTITIAL_PLACEMENT_ID, object: LoadAdCallback {
                override fun onAdLoad(p0: String?) {
                    Log.d(TAG, "Vungle ad load success")
                }

                override fun onError(p0: String?, p1: Throwable?) {
                    Log.d(TAG, "Vungle ad load error")
                }
            })
        }
    }

    private fun playVungleAd(): Boolean {
        val canPlay = Vungle.canPlayAd(VUNGLE_INTESTITIAL_PLACEMENT_ID)
        if (canPlay) {
            val adConfig = AdConfig()
            adConfig.setAutoRotate(false)
            adConfig.setMuted(true)
            Vungle.playAd(VUNGLE_INTESTITIAL_PLACEMENT_ID, null, object : PlayAdCallback {
                override fun onAdStart(placementReferenceId: String) {
                    Log.d(TAG, "Vungle ad play start")
                }

                override fun onAdEnd(placementReferenceId: String, completed: Boolean, isCTAClicked: Boolean) {
                    Log.d(TAG, "Vungle ad play end")
                    loadVungleAd()
                }

                override fun onError(placementReferenceId: String, throwable: Throwable) {
                    Log.d(TAG, "Vungle ad play error")

                    try {
                        val ex = throwable as VungleException

                        if (ex.exceptionCode == VungleException.VUNGLE_NOT_INTIALIZED) {
                            initializeVungleSDK(context)
                        }
                    } catch (cex: ClassCastException) {
                        Log.d(TAG, cex.message)
                    }
                }
            })
        }
        return canPlay
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

    public fun showInterstitial(activity: Activity) {

        if (!goodTimeToShowAds()) {
            return
        }

        // If Vungle ads is ready, show it
        if (playVungleAd()) {
            saveAdShowTime(Date())
            return
        }

        // If unity ads is ready, show it
        if (UnityAdsManager.showVideoAds(activity)) {
            saveAdShowTime(Date())
            return
        }

        // if admob interstitial is ready, show it
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