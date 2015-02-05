package physicalweb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import saviour.SaviourBrain;
import tronbox.heineken.R;

public class PhysicalWebHacking extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meta_wear_hacking);


        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.loadUrl("http://thetronbox.com/tree/index.html");


        startService(new Intent(getApplicationContext(), PhysicalWebBrain.class));


    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

            /** Show a toast from the web page */
            @JavascriptInterface
            public String startLed(String toast)
        {

            sendBroadcast(new Intent("Button_Pressed"));


            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

            return "LED Is On !!!";
        }


        /** Show a toast from the web page */
        @JavascriptInterface
        public String startBuzzer(String toast)
        {

            sendBroadcast(new Intent("Button_Pressed"));

            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

            return "Buzzer Is On !!!";
        }

    }
}
