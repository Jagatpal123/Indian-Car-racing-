package com.jagatpal.carracing;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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
        loadInterstitialAd();
        loadRewardedAd();
    }

    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Toast.makeText(MainActivity.this, "AdMob Initialized", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupWebView() {
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        
        // JavaScript interface add karo
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        
        loadGameHTML();
    }

    private void loadGameHTML() {
        // Yahan aapka original game HTML code load hoga
        webView.loadUrl("file:///android_asset/index.html");
    }

    // INTERSTITIAL AD FOR RESTART BUTTON
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        
        InterstitialAd.load(this, INTERSTITIAL_AD_UNIT_ID, adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd interstitialAd) {
                    MainActivity.this.interstitialAd = interstitialAd;
                    Toast.makeText(MainActivity.this, "Interstitial Ad Ready", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    interstitialAd = null;
                    Toast.makeText(MainActivity.this, "Interstitial Ad Failed", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // REWARDED AD FOR GET LIVES BUTTON  
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest,
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd rewardedAd) {
                    MainActivity.this.rewardedAd = rewardedAd;
                    Toast.makeText(MainActivity.this, "Rewarded Ad Ready", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    rewardedAd = null;
                    Toast.makeText(MainActivity.this, "Rewarded Ad Failed", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // INTERSTITIAL AD SHOW KARNE KA FUNCTION
    public void showInterstitialAd() {
        if (interstitialAd != null) {
            interstitialAd.show(this);
            // Next ad load karo
            loadInterstitialAd();
        } else {
            // Agar ad ready nahi hai to direct restart
            webView.loadUrl("javascript:closeAdAndRestart()");
            loadInterstitialAd(); // Phir se try karo
        }
    }

    // REWARDED AD SHOW KARNE KA FUNCTION
    public void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                // Reward mil gaya - 3 lives dena
                webView.loadUrl("javascript:giveLivesReward()");
                Toast.makeText(MainActivity.this, "🎉 You got 3 lives!", Toast.LENGTH_SHORT).show();
            });
            // Next ad load karo
            loadRewardedAd();
        } else {
            // Agar ad ready nahi hai to message show karo
            Toast.makeText(this, "Ad not ready yet. Please try again.", Toast.LENGTH_SHORT).show();
            loadRewardedAd(); // Phir se try karo
        }
    }

    // JavaScript Interface - WebView se communicate karne ke liye
    public class WebAppInterface {
        @JavascriptInterface
        public void showInterstitialAd() {
            runOnUiThread(() -> MainActivity.this.showInterstitialAd());
        }

        @JavascriptInterface
        public void showRewardedAd() {
            runOnUiThread(() -> MainActivity.this.showRewardedAd());
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
