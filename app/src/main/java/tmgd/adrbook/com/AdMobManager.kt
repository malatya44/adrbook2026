package tmgd.adrbook.com

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowMetrics
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdMobManager(private val activity: Activity) {

    private val tag = "AdMobManager"
    private var adView: AdView? = null
    private var interstitialAd: InterstitialAd? = null
    private var pageNavigationCount = 0
    private val interstitialThreshold = 5 // Show interstitial every 5 navigations

    fun requestConsentAndInitAds(container: FrameLayout) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(tag, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        loadAdaptiveBanner(container)
                        loadInterstitial()
                    }
                }
            },
            { requestConsentError ->
                Log.w(tag, "Consent update error: ${requestConsentError.message}")
                loadAdaptiveBanner(container)
                loadInterstitial()
            }
        )

        if (consentInformation.canRequestAds()) {
            loadAdaptiveBanner(container)
            loadInterstitial()
        }
    }

    fun loadAdaptiveBanner(container: FrameLayout) {
        try {
            val bannerAdView = AdView(activity)
            bannerAdView.adUnitId = activity.getString(R.string.admob_banner_id)
            container.removeAllViews()
            container.addView(bannerAdView)

            val adSize = getAdSize()
            bannerAdView.setAdSize(adSize)

            val adRequest = AdRequest.Builder().build()
            bannerAdView.loadAd(adRequest)
            adView = bannerAdView
        } catch (e: Exception) {
            Log.e(tag, "Error loading adaptive banner: ${e.message}")
        }
    }

    private fun getAdSize(): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density
        var adWidthPixels = outMetrics.widthPixels.toFloat()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            adWidthPixels = windowMetrics.bounds.width().toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            activity.getString(R.string.admob_interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    setupInterstitialCallbacks()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun setupInterstitialCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial() // preload next
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
            }
        }
    }

    fun onPageLoaded() {
        pageNavigationCount++
        if (pageNavigationCount >= interstitialThreshold) {
            if (interstitialAd != null) {
                interstitialAd?.show(activity)
                pageNavigationCount = 0
            }
        }
    }

    fun onResume() {
        adView?.resume()
    }

    fun onPause() {
        adView?.pause()
    }

    fun onDestroy() {
        adView?.destroy()
    }
}
