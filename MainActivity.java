package com.jagatpal.carracing;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    // आपके Ad Unit IDs
    private final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6302105448775587/5186924288";
    private final String REWARDED_AD_UNIT_ID = "ca-app-pub-6302105448775587/1000907894";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAds();
        setupWebView();
    }

    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // AdMob Initialized होने पर Toast हटा दिया, ताकि गेम जल्दी शुरू हो
                loadInterstitialAd();
                loadRewardedAd();
            }
        });
    }

    private void setupWebView() {
        webView = findViewById(R.id.webview);
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        // ये सेटिंग्स Android 10+ (API 29+) पर काम नहीं करेंगी, लेकिन पुराने वर्ज़न के लिए ज़रूरी हैं
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Game Ready Toast हटा दिया
            }
        });
        
        // JavaScript interface को 'Android' नाम से जोड़ें - यह महत्वपूर्ण है!
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        
        loadGameHTML();
    }

    private void loadGameHTML() {
        // सुनिश्चित करें कि आपकी HTML फ़ाइल assets फ़ोल्डर में game.html के नाम से है
        webView.loadUrl("file:///android_asset/game.html");
    }

    // INTERSTITIAL AD LOAD
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        
        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    MainActivity.this.interstitialAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    interstitialAd = null;
                    // Ad Load Fail होने पर, सीधे JavaScript को बताएं कि ad नहीं दिखा सकते
                    webView.loadUrl("javascript:closeAdAndRestart()"); 
                }
            });
    }

    // REWARDED AD LOAD  
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    MainActivity.this.rewardedAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    rewardedAd = null;
                    // Ad Load Fail होने पर, Toast से बताएं
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Reward Ad failed to load. Try again.", Toast.LENGTH_SHORT).show());
                }
            });
    }

    // INTERSTITIAL AD SHOW KARNE KA FUNCTION (Called from JavaScript)
    public void showInterstitialAd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd != null) {
                    interstitialAd.show(MainActivity.this);
                    // Next ad load करो
                    loadInterstitialAd();
                } else {
                    // Agar ad ready nahi hai to direct restart (JavaScript function)
                    webView.loadUrl("javascript:closeAdAndRestart()");
                    loadInterstitialAd(); // Phir se try karo
                }
            }
        });
    }

    // REWARDED AD SHOW KARNE KA FUNCTION (Called from JavaScript)
    public void showRewardedAd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rewardedAd != null) {
                    rewardedAd.show(MainActivity.this, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(RewardItem rewardItem) {
                            // Reward mil gaya - JavaScript function call करो
                            webView.loadUrl("javascript:giveLivesReward()");
                            Toast.makeText(MainActivity.this, "🎉 You got 3 lives!", Toast.LENGTH_LONG).show();
                        }
                    });
                    // Next ad load karo
                    loadRewardedAd();
                } else {
                    // Agar ad ready nahi hai to message show karo
                    Toast.makeText(MainActivity.this, "Ad loading... Please try again.", Toast.LENGTH_SHORT).show();
                    loadRewardedAd(); // Phir se try karo
                }
            }
        });
    }

    // JavaScript Interface - WebView से communicate करने के लिए
    public class WebAppInterface {
        
        @JavascriptInterface
        public void showInterstitialAd() {
            MainActivity.this.showInterstitialAd();
        }

        @JavascriptInterface
        public void showRewardedAd() {
            MainActivity.this.showRewardedAd();
        }
        
        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Back Button Press होने पर JavaScript को बताएं
        webView.loadUrl("javascript:onAndroidBackPress()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // सुनिश्चित करें कि onResume पर ad re-load हों (null होने पर)
        if (interstitialAd == null) {
            loadInterstitialAd();
        }
        if (rewardedAd == null) {
            loadRewardedAd();
        }
    }
}
