package edu.uco.rnolastname.program6.app;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import edu.uco.rnolastname.program6.R;

public class FragmentWebView extends Fragment {
	private static View v;
	private static WebView webView;
	private static String url;
	private static WebViewBrowser webViewClient;
	private boolean mIsWebViewAvailable;
	
	public static FragmentWebView newInstance(String url){
		FragmentWebView f = new FragmentWebView();
		
		Bundle b = new Bundle();
		b.putString("url", url);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		url = getArguments().getString("url");
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		//v = inflater.inflate(R.layout.webview_fragment,container, false);
		if (webView != null) {
			webView.destroy();
        }
		
		webView = new WebView(getActivity().getApplicationContext());
				
		mIsWebViewAvailable = true;
		webView.setWebViewClient(new WebViewBrowser());		
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.loadUrl(url);
		
		return webView;
	}
	
	@Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }
	
	@Override
    public void onResume() {
        webView.onResume();
        super.onResume();
    }
	
	@Override
    public void onDestroy() {
        if (webView != null) {
        	webView.destroy();
        	webView = null;
        }
        super.onDestroy();
    }
	
	public void loadUrl(String url) {
        if (mIsWebViewAvailable) getWebView().loadUrl(this.url = url);
        else Log.w("ImprovedWebViewFragment", "WebView cannot be found. Check the view and fragment have been loaded.");
    }
	
	public WebView getWebView() {
        return mIsWebViewAvailable ? webView : null;
    }
		
	private class WebViewBrowser extends WebViewClient{
		@Override
		public boolean shouldOverrideUrlLoading(WebView webView,String url){
			webView.loadUrl(url);
			return true;
		}
	}
}
