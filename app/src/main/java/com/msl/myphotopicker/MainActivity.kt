package com.msl.myphotopicker

import com.msl.myphotopicker.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.bumptech.glide.Glide
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdOptionsView
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.MediaView
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.facebook.ads.AdSize as AdSizeFace
import com.facebook.ads.AdView as AdViewFace
import com.facebook.ads.InterstitialAd as InterstitialAdFaceBook
import com.facebook.ads.NativeAd as NativeAdFace


class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {
    lateinit var btn: AppCompatButton
    lateinit var btnInterstitialAdBtn: AppCompatButton
    lateinit var btnRewardedAdAdBtn: AppCompatButton
    lateinit var btninAppPurchaseBtn: AppCompatButton
    lateinit var btninterstitialAdFaceBtn: AppCompatButton
    lateinit var btninAppPurchaseFacebookBtn : AppCompatButton
    private lateinit var mainContainer: RelativeLayout
    lateinit var nativeAdIcon: ImageView
    lateinit var tv: TextView

    // mBannerAds
    lateinit var mAdView: AdView

    // mInterstitialAd
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivityLucky"
    var adRequest = AdRequest.Builder().build()
    lateinit var adLoader: AdLoader

    private var rewardedAd: RewardedAd? = null


    private lateinit var billingClient: BillingClient
    private var adViewFace: AdViewFace? = null

    private var interstitialAdFacebook: InterstitialAdFaceBook? = null
    private var rewardedVideoAd: RewardedVideoAd? = null

    private var nativeAdFace: NativeAdFace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)









        initializeMobileAds()

        btninAppPurchaseFacebookBtn = findViewById<AppCompatButton>(R.id.inAppPurchaseFacebookBtn)
        btninAppPurchaseFacebookBtn.setOnClickListener(View.OnClickListener {
            loadRewardedAdFacebook()
        })

        btninterstitialAdFaceBtn = findViewById<AppCompatButton>(R.id.interstitialAdFaceBtn)

        btninterstitialAdFaceBtn.setOnClickListener(View.OnClickListener {
            loadInterstitialAdFacebook()
        })
        btninAppPurchaseBtn = findViewById<AppCompatButton>(R.id.inAppPurchaseBtn)

        btninAppPurchaseBtn.setOnClickListener(View.OnClickListener {
            val skuId = "your_product_id"
            getSkuDetails(skuId) { skuDetails ->
                initiatePurchase(skuDetails)
            }
        })
        // Reference the main container
        mainContainer = findViewById(R.id.mainContainer)

        btn = findViewById<AppCompatButton>(R.id.loginBtn)

        btn.setOnClickListener(View.OnClickListener {
            val intent: Intent? = Intent(this, LoginActivity::class.java)
            startActivity(intent)


        })

        btnInterstitialAdBtn = findViewById<AppCompatButton>(R.id.interstitialAdBtn)
        btnInterstitialAdBtn.setOnClickListener(View.OnClickListener {
            showInterstitialAd(adRequest)

        })

        btnRewardedAdAdBtn = findViewById(R.id.rewardedAdAdBtn)

        btnRewardedAdAdBtn.setOnClickListener(View.OnClickListener {
            rewardedAd?.let { ad ->
                ad.show(this, OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    Log.d(TAG, "User earned the reward.")
                })
            } ?: run {
                Log.d(TAG, "The rewarded ad wasn't ready yet.")
            }
        })

        billingClient =
            BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build()

        // in app purchase
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.i("Billing", "billingResult.responseCode: ${billingResult.responseCode}")
                    queryAvailableProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.i("Billing", "onBillingServiceDisconnected: ")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })


    }


    private fun getSkuDetails(skuId: String, onSkuDetailsLoaded: (SkuDetails) -> Unit) {
        val skuList = listOf(skuId)
        val params =
            SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    if (skuDetails.sku == skuId) {
                        // Found matching SkuDetails
                        onSkuDetailsLoaded.invoke(skuDetails)
                        return@querySkuDetailsAsync
                    }
                }
                Log.e("Billing", "SkuDetails not found for $skuId")
            } else {
                Log.e("Billing", "Failed to query SkuDetails: ${billingResult.debugMessage}")
            }
        }
    }

    private fun initiatePurchase(skuId: SkuDetails) {
        val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuId).build()

        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    override fun onDestroy() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd!!.destroy();
            rewardedVideoAd = null;
        }
        if (adViewFace != null) {
            adViewFace!!.destroy();
        }
        super.onDestroy()
        billingClient.endConnection()
    }

    // Implement PurchasesUpdatedListener methods
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                // Handle the purchase
                handlePurchase(purchase)
            }
        } else {
            Log.e("Billing", "Failed to update purchases: ${billingResult.debugMessage}")
        }
    }

    private fun queryAvailableProducts() {
        val skuList = listOf("your_product_id_1", "your_product_id_2")
        val params =
            SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the retrieved products (skuDetailsList)
                skuDetailsList?.forEach { skuDetails ->
                    Log.d("Billing", "Found product: ${skuDetails.title}, ${skuDetails.price}")
                    // Display or use product details as needed
                }
            } else {
                Log.e(
                    "Billing", "Failed to query available products: ${billingResult.debugMessage}"
                )
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // Handle the purchase, e.g., acknowledge the purchase, unlock content, etc.
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                    .build()

            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Purchase acknowledged successfully
                    Log.d("Billing", "Purchase acknowledged: ${purchase.purchaseToken}")
                } else {
                    Log.e(
                        "Billing", "Failed to acknowledge purchase: ${billingResult.debugMessage}"
                    )
                }
            }
        }
    }

    private fun initializeMobileAds() {


        loadBannerAdFacebook()
        loadBannerAd(adRequest)


        loadInterstitialAd(adRequest)

        loadNativeAdFacebook()
        loadNativeAd(adRequest)

        loadRewardedAd(adRequest)

    }


    // Function to show the interstitial ad
    private fun showInterstitialAd(adRequest: AdRequest) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            loadInterstitialAd(adRequest)
        }
    }


    private fun loadInterstitialAdFacebook() {
        interstitialAdFacebook = InterstitialAdFaceBook(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
        // Create listeners for the Interstitial Ad
        // Create listeners for the Interstitial Ad
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.")
            }

            override fun onError(ad: Ad?, adError: AdError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage())
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
                // Show the ad
                interstitialAdFacebook!!.show()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        }

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAdFacebook!!.loadAd(
            interstitialAdFacebook!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )

    }

    private fun loadInterstitialAd(adRequest: AdRequest) {
        // InterstitialAd
        InterstitialAd.load(this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun loadBannerAdFacebook() {
        adViewFace =
            AdViewFace(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSizeFace.BANNER_HEIGHT_50)

        // Find the Ad Container

        // Find the Ad Container
        val adContainer = findViewById<View>(R.id.banner_container) as LinearLayout

        // Add the ad view to your activity layout

        // Add the ad view to your activity layout
        adContainer.addView(adViewFace)

        // Request an ad

        // Request an ad
        adViewFace!!.loadAd()
    }

    private fun loadBannerAd(adRequest: AdRequest) {
        // Banner Ads
        mAdView = findViewById(R.id.adView)

        mAdView.loadAd(adRequest)
    }


    private fun loadNativeAdFacebook(){
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        // Instantiate a NativeAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeAdFace = NativeAdFace(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")

        val nativeAdListener: NativeAdListener = object : NativeAdListener {
            override fun onMediaDownloaded(ad: Ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.")
            }

            override fun onError(ad: Ad, adError: AdError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.errorMessage)
            }

            override fun onAdLoaded(ad: Ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!")

                // Race condition, load() called again before last ad was displayed
                if (nativeAdFace == null || nativeAdFace != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAdFace!!);
            }

            override fun onAdClicked(ad: Ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!")
            }
        }

        // Request an ad

        // Request an ad
        nativeAdFace!!.loadAd(
            nativeAdFace!!.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build()
        )
    }

    private var nativeAdLayout: NativeAdLayout? = null
    private var adViewRFace: LinearLayout? = null
    private fun inflateAd(nativeAd: NativeAdFace) {
        nativeAd.unregisterView()

        // Add the Ad view into the ad container.
        nativeAdLayout = findViewById<NativeAdLayout>(R.id.native_ad_container)
        val inflater = LayoutInflater.from(this)
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adViewRFace =
            inflater.inflate(R.layout.native_ad_layout_1, nativeAdLayout, false) as LinearLayout
        nativeAdLayout!!.addView(adViewRFace)

        // Add the AdOptionsView
        val adChoicesContainer = findViewById<LinearLayout>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(this, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adViewRFace!!.findViewById(R.id.native_ad_icon)
        val nativeAdTitle: TextView = adViewRFace!!.findViewById(R.id.native_ad_title)
        val nativeAdMedia: MediaView = adViewRFace!!.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext: TextView = adViewRFace!!.findViewById(R.id.native_ad_social_context)
        val nativeAdBody: TextView = adViewRFace!!.findViewById(R.id.native_ad_body)
        val sponsoredLabel: TextView = adViewRFace!!.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: Button = adViewRFace!!.findViewById(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName())
        nativeAdBody.setText(nativeAd.getAdBodyText())
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext())
        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction())
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation())

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
            adViewRFace!!, nativeAdMedia, nativeAdIcon, clickableViews
        )
    }

    private fun loadNativeAd(adRequest: AdRequest) {

        val adOptions =
            NativeAdOptions.Builder().setMediaAspectRatio(MediaAspectRatio.PORTRAIT).build()

        adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                val adView = layoutInflater.inflate(R.layout.native_ad_layout, null)
                // This method sets the text, images and the native ad, etc into the ad
                // view.
                tv = adView.findViewById<TextView>(R.id.nativeAdTitle)
                tv.text = ad.headline
                nativeAdIcon = adView.findViewById<ImageView>(R.id.nativeAdIcon)
                // Load ad icon using Glide
                loadAdImage(ad.icon?.uri.toString(), nativeAdIcon)
                mainContainer.addView(adView)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "native ads = " + adError.message);
                    // Handle the failure by logging, altering the UI, and so on.
                }
            }).withNativeAdOptions(adOptions).build()
        adLoader.loadAd(adRequest)


    }

    private fun loadAdImage(imageUrl: String?, imageView: ImageView) {
        imageUrl?.let {
            Glide.with(this).load(it).into(imageView)
        }
    }

    private fun loadRewardedAdFacebook(){
        rewardedVideoAd = RewardedVideoAd(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID")
        val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onError(ad: Ad, error: AdError) {
                // Rewarded video ad failed to load
                Log.e(TAG, "Rewarded video ad failed to load: " + error.errorMessage)
            }

            override fun onAdLoaded(ad: Ad) {
                // Rewarded video ad is loaded and ready to be displayed
                Log.d(TAG, "Rewarded video ad is loaded and ready to be displayed!")
                rewardedVideoAd!!.show();
            }

            override fun onAdClicked(ad: Ad) {
                // Rewarded video ad clicked
                Log.d(TAG, "Rewarded video ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                Log.d(TAG, "Rewarded video ad impression logged!")
            }

            override fun onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                Log.d(TAG, "Rewarded video completed!")

                // Call method to give reward
                // giveReward();
            }

            override fun onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
                Log.d(TAG, "Rewarded video ad closed!")
            }
        }


        rewardedVideoAd!!.loadAd(
            rewardedVideoAd!!.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener)
                .build()
        )
    }

    private fun loadRewardedAd(adRequest: AdRequest) {
        RewardedAd.load(this,
            "ca-app-pub-3940256099942544/5224354917",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad
                }
            })
    }


}