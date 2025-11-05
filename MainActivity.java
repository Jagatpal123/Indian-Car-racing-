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

    // YAHAN APNI IDs DALI HAIN
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
                Toast.makeText(MainActivity.this, "AdMob Initialized", Toast.LENGTH_SHORT).show();
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
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(MainActivity.this, "Game Ready!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // JavaScript interface add karo
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        
        loadGameHTML();
    }

    private void loadGameHTML() {
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
                }
            });
    }

    // INTERSTITIAL AD SHOW KARNE KA FUNCTION
    public void showInterstitialAd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interstitialAd != null) {
                    interstitialAd.show(MainActivity.this);
                    // Next ad load karo
                    loadInterstitialAd();
                } else {
                    // Agar ad ready nahi hai to direct restart
                    webView.loadUrl("javascript:closeAdAndRestart()");
                    loadInterstitialAd(); // Phir se try karo
                }
            }
        });
    }

    // REWARDED AD SHOW KARNE KA FUNCTION
    public void showRewardedAd() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rewardedAd != null) {
                    rewardedAd.show(MainActivity.this, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(RewardItem rewardItem) {
                            // Reward mil gaya - 3 lives dena
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

    // JavaScript Interface - WebView se communicate karne ke liye
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
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume pe ads phir se load karo
        if (interstitialAd == null) {
            loadInterstitialAd();
        }
        if (rewardedAd == null) {
            loadRewardedAd();
        }
    }
}
