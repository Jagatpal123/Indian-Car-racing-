package com.jagatpal.carracing;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    private AdView adViewTop, adViewBottom;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeAds();
        setupWebView();
        loadAds();
    }

    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
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
        loadGameHTML();
    }

    private void loadGameHTML() {
        String gameHTML = getGameHTML();
        webView.loadDataWithBaseURL("file:///android_asset/", gameHTML, "text/html", "UTF-8", null);
    }

    private void loadAds() {
        adViewTop = findViewById(R.id.adViewTop);
        AdRequest adRequestTop = new AdRequest.Builder().build();
        adViewTop.loadAd(adRequestTop);

        adViewBottom = findViewById(R.id.adViewBottom);
        AdRequest adRequestBottom = new AdRequest.Builder().build();
        adViewBottom.loadAd(adRequestBottom);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adViewTop != null) adViewTop.resume();
        if (adViewBottom != null) adViewBottom.resume();
    }

    @Override
    protected void onPause() {
        if (adViewTop != null) adViewTop.pause();
        if (adViewBottom != null) adViewBottom.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adViewTop != null) adViewTop.destroy();
        if (adViewBottom != null) adViewBottom.destroy();
        super.onDestroy();
    }

    private String getGameHTML() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Car Racing Game</title>\n" +
                "    <style>\n" +
                "        body, html { margin: 0; padding: 0; overflow: hidden; width: 100vw; height: 100vh; background: #000; font-family: Arial; }\n" +
                "        #gameContainer { position: relative; width: 100%; height: 100%; }\n" +
                "        .gameScreen { position: absolute; top: 0; left: 0; width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; color: white; flex-direction: column; }\n" +
                "        .action-button { padding: 20px 40px; font-size: 24px; background: #ff4500; color: white; border: none; border-radius: 10px; margin: 10px; cursor: pointer; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"gameContainer\">\n" +
                "        <div id=\"menuScreen\" class=\"gameScreen\">\n" +
                "            <h1>Car Racing Game</h1>\n" +
                "            <button class=\"action-button\" onclick=\"startGame()\">Start Game</button>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        function startGame() {\n" +
                "            document.getElementById('menuScreen').style.display = 'none';\n" +
                "            // Yahan aapka game logic aayega\n" +
                "            alert('Game Starting with AdMob Ads!');\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
