package com.mihwapp.crazymusic.ads

import android.app.Activity
import android.util.Log
import com.unity3d.ads.UnityAds
import com.unity3d.services.UnityServices
import com.unity3d.services.monetization.IUnityMonetizationListener
import com.unity3d.services.monetization.UnityMonetization
import com.unity3d.services.monetization.placementcontent.ads.IShowAdListener
import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent
import com.unity3d.services.monetization.placementcontent.core.PlacementContent
import com.unity3d.services.monetization.placementcontent.purchasing.PromoAdPlacementContent

/**
 * Document On How to integrate Unity Ads:
 * https://unityads.unity3d.com/help/android/integration-guide-android
 * NOTE:
 * 1. video ad has been implemented
 * 2. rewarded video ads has been implemented
 * 3. banner ads has not been implemented
 */

object UnityAdsManager: IUnityMonetizationListener {

    private const val PLACEMENT_ID_VIDEO = "video"
    private const val PLACEMENT_ID_VIDEO_REWARDED = "rewardedVideo"
    private const val GAME_ID = "1458309"
    private const val PLACEMENT_CONTENT_SHOW_AD = "SHOW_AD"
    private const val TAG = "UnityAdsManager"

    override fun onPlacementContentStateChange(p0: String?, p1: PlacementContent?, p2: UnityMonetization.PlacementContentState?, p3: UnityMonetization.PlacementContentState?) {

    }

    override fun onPlacementContentReady(placementId: String?, placementContent: PlacementContent?) {
        // Check the Placement ID to determine behavior
        when (placementId) {
            PLACEMENT_ID_VIDEO -> if (placementContent is PromoAdPlacementContent) {
                // Promo content is ready, prepare Promo display
            } else if (placementContent is ShowAdPlacementContent) {
                // Ad content is ready, prepare video ad display
            }
            PLACEMENT_ID_VIDEO_REWARDED -> if (placementContent is ShowAdPlacementContent) {
                if (placementContent.isRewarded) {
                    // Rewarded content is ready, prepare content for display and implement reward handlers
                    // TODO: update ui...e.g. light up the reward button
                }
            }
        }
    }

    override fun onUnityServicesError(p0: UnityServices.UnityServicesError?, p1: String?) {

    }



    fun initialize(activity: Activity, testMode: Boolean) {
        UnityMonetization.initialize(activity, GAME_ID, this, testMode)
    }

    fun showRewardVideoAds(activity: Activity, rewardCompletion: ()-> Unit): Boolean {
        return showContentWithId(PLACEMENT_ID_VIDEO_REWARDED, activity, rewardCompletion)
    }

    fun showVideoAds(activity: Activity): Boolean {
        return showContentWithId(PLACEMENT_ID_VIDEO, activity) {}
    }

    private fun showContentWithId(placementId: String, activity: Activity, rewardCompletion: ()-> Unit): Boolean {

        val isReady = UnityMonetization.isReady(placementId)

        if (isReady) {

            val pc = UnityMonetization.getPlacementContent(placementId)

            if (pc.type.equals(PLACEMENT_CONTENT_SHOW_AD, ignoreCase = true)) {

                (pc as ShowAdPlacementContent).show(activity, object : IShowAdListener {
                    override fun onAdFinished(s: String, finishState: UnityAds.FinishState) {
                        Log.d(TAG, "PlacementId: $s $finishState")

                        if (finishState == UnityAds.FinishState.COMPLETED) {
                            if (s == PLACEMENT_ID_VIDEO_REWARDED) {
                                // TODO: add completion handler to reward the user
                                rewardCompletion()
                            }
                        } else if (finishState == UnityAds.FinishState.SKIPPED) {
                            // Optionally implement skipped logic
                        } else if (finishState == UnityAds.FinishState.ERROR) {
                            // Optionally attempt to retrieve another ad
                        }
                    }

                    override fun onAdStarted(s: String) {

                    }
                })
            }

        } else {
            Log.e(TAG, "Video Placement is not ready!")
        }

        return isReady
    }
}