/*
 * Kantpoll Project
 * https://github.com/kantpoll
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * --------------------------------------------------------------------------
 *
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kantpoll.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import info.guardianproject.netcipher.proxy.OrbotHelper;

public class HomeActivity extends AppCompatActivity {
    private final static int USER_PASSWORD_SIZE = 16;
    private final static int REGISTER_REQUEST_CODE = 1;
    private final static int LOGIN_REQUEST_CODE = 2;
    private final static int EXPORT_VAULT_REQUEST_CODE = 3;
    private final static int NEW_INTENT_CALLER = 1;
    private final static int RESUME_CALLER = 2;

    private String global_password = "";
    private String global_url_complement = "";

    /**
     * It opens the home.html page and sets the Tor WebView client and the Javascript interface
     *
     * @param context {Activity}
     * @param webView {WebView}
     * @param url     (String}
     */
    private void setWebView(Activity context, WebView webView, String url) {
        SharedPreferences sharedPref = getSharedPreferences("com.kantpoll.android",
                Context.MODE_PRIVATE);
        String user = sharedPref.getString("user", null);

        if (user == null || user.equals("")) {

            Toast.makeText(getApplication(), R.string.create_vault_first,
                    Toast.LENGTH_LONG).show();
            onBackPressed();
            return;
        }

        if (webView.getUrl() == null || webView.getUrl().equals("")) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            webView.setWebViewClient(new TorWebViewClient());
            webView.addJavascriptInterface(new WebAppInterface(context), "Android");

            webView.loadUrl(url);

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    android.util.Log.d("WebView", consoleMessage.message());
                    return true;
                }
            });
        } else if (!webView.getUrl().equals(url)) {
            webView.loadUrl(url);
        }

        global_url_complement = "";
    }

    /**
     * Inflating the home menu
     *
     * @param menu {Menu}
     * @return {boolean}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Do nothing
     *
     * @param item {MenuItem}
     * @return {boolean}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    /**
     * It sets the content view and the toolbar, opens the home.html in the web view, gets the
     * link from the extras, and checks if Orbot is installed
     *
     * @param savedInstanceState {Bundle}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        global_url_complement = "";
        if (getIntent() != null && getIntent().getData() != null){
            global_url_complement = getUrlComplement(getIntent());
        }

        Toolbar homeToolbar = findViewById(R.id.home_toolbar);
        homeToolbar.setTitle("");
        setSupportActionBar(homeToolbar);

        //To change the color of status Bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        WebView webView = findViewById(R.id.webview1);
        if (savedInstanceState == null) {
            setWebView(this, webView,
                    "file:///android_asset/website/home.html?mode=start");
        } else {
            setWebView(this, webView, "file:///android_asset/website/home.html");
        }

        //The Orbot must be installed and running
        if (OrbotHelper.isOrbotInstalled(this)) {
            OrbotHelper.requestStartTor(this);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.install_orbot_title));
            alertDialog.setMessage(getString(R.string.install_orbot_message));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("market://details?id=org.torproject.android"));
                            startActivity(goToMarket);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.quit_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                        }
                    });
            alertDialog.show();
        }
    }

    /**
     * Search for a new campaign if the user clicked the link
     * @param intent {Intent}
     */
    @Override
    protected void onNewIntent (Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        checkIntentAndLoadData(NEW_INTENT_CALLER);
    }

    /**
     * It loads the vault
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkIntentAndLoadData(RESUME_CALLER);
    }

    /**
     * It may be called by onResume or onNewIntent methods
     */
    private void checkIntentAndLoadData(int caller){
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null){
            global_url_complement = getUrlComplement(intent);
        }

        WebView webView = findViewById(R.id.webview1);
        String w_url = webView.getUrl();
        if (w_url == null || (caller == NEW_INTENT_CALLER && !global_url_complement.equals("")) ||
                w_url.equals("file:///android_asset/website/home.html?mode=start")) {
            loadData();
            intent.setData(null);
        }
    }

    /**
     * For deep links
     * @param intent {Intent}
     * @return {String}
     */
    private String getUrlComplement(Intent intent){
        Uri uri = intent.getData();
        String url_complement = "";

        if (uri != null) {
            try {
                URL url = new URL(uri.toString());
                Map queries = splitQuery(url);
                String ipns = (String) queries.get("ipns");
                String onion = (String) queries.get("onion");
                String address = (String) queries.get("address");
                String observers = (String) queries.get("observers");

                if (ipns != null && !ipns.equals("")) {
                    url_complement = "&ipns=" + ipns;
                }
                if (onion != null && !onion.equals("")) {
                    url_complement += "&onion=" + onion;
                }
                if (address != null && !address.equals("")) {
                    url_complement += "&address=" + address;
                }
                if (observers != null && !observers.equals("")) {
                    url_complement += "&observers=" + observers;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url_complement;
    }

    /**
     * It loads the necessary data to open the vault
     */
    private void loadData() {
        WebView webView = findViewById(R.id.webview1);
        final UserAuthentication userAuthentication = new UserAuthentication(this);

        SharedPreferences sharedPref = getSharedPreferences("com.kantpoll.android",
                Context.MODE_PRIVATE);
        String words = sharedPref.getString("words", null);
        String data = sharedPref.getString("data", null);
        String user = sharedPref.getString("user", null);
        String registered = sharedPref.getString("registered", null);

        if (registered != null && registered.equals("false")) {
            showAuthenticationScreen(REGISTER_REQUEST_CODE);
            SharedPreferences.Editor editor =
                    getSharedPreferences("com.kantpoll.android",
                            Context.MODE_PRIVATE).edit();
            editor.putString("registered", "true");
            editor.apply();
        } else {
            try {
                if (global_password == null || global_password.equals("")) {
                    global_password = userAuthentication.getUserPassword();
                    eraseGlobalPassword();
                }

                String w_url = webView.getUrl();
                if (w_url == null || w_url.equals("file:///android_asset/website/home.html?mode=start")
                    || !global_url_complement.equals("")){
                    setWebView(this, webView,
                            "file:///android_asset/website/home.html" +
                                    "?user=" + user +
                                    "&password=" + global_password +
                                    "&words=" + words +
                                    "&data=" + data +
                                    "&mode=openVaultAndroid" + global_url_complement);
                } else{
                    if (global_url_complement.length() > 1){
                        global_url_complement = global_url_complement.substring(1);
                    }
                    setWebView(this, webView,
                            "file:///android_asset/website/home.html?"
                                    + global_url_complement);
                }

            } catch (InvalidKeyException e) {
                showAuthenticationScreen(LOGIN_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * It is usually called after user authentication
     *
     * @param requestCode {int}
     * @param resultCode  {int}
     * @param intent      {Intent}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            super.onActivityResult(requestCode, resultCode, intent);
            WebView webView = findViewById(R.id.webview1);

            UserAuthentication userAuthentication = new UserAuthentication(this);
            SharedPreferences sharedPref = getSharedPreferences("com.kantpoll.android",
                    Context.MODE_PRIVATE);
            String user = sharedPref.getString("user", null);
            String words = sharedPref.getString("words", null);
            String data = sharedPref.getString("data", null);
            String password;

            switch (requestCode) {
                case REGISTER_REQUEST_CODE:
                    password = randomAlphanumeric(USER_PASSWORD_SIZE);
                    userAuthentication.saveUserPassword(password);

                    WebAppInterface.may_set_vault = true;
                    WebAppInterface.may_get_certificate = true;
                    setWebView(this, webView,
                            "file:///android_asset/website/home.html" +
                                    "?user=" + user +
                                    "&password=" + password +
                                    "&mode=createVaultAndroid");

                    invalidateOptionsMenu();
                    break;
                case LOGIN_REQUEST_CODE:
                    if (global_password == null || global_password.equals("")) {
                        global_password = userAuthentication.getUserPassword();
                        eraseGlobalPassword();
                    }

                    setWebView(this, webView,
                            "file:///android_asset/website/home.html" +
                                    "?user=" + user +
                                    "&password=" + global_password +
                                    "&words=" + words +
                                    "&data=" + data +
                                    "&mode=openVaultAndroid" + global_url_complement);
                    break;
                case EXPORT_VAULT_REQUEST_CODE:
                    password = userAuthentication.getUserPassword();
                    if (password == null || password.equals("") || words == null || data == null) {
                        final Intent errorIntent = new Intent(this, MessageActivity.class);
                        errorIntent.putExtra(MessageActivity.MESSAGE_EXTRA,
                                new ParcelableMessage(getString(R.string.no_vault)));
                        startActivity(errorIntent);
                        return;
                    }

                    final Intent exportIntent = new Intent(this, ExportActivity.class);
                    exportIntent.putExtra(ExportActivity.USERNAME_EXTRA,
                            new ParcelableMessage(user));
                    exportIntent.putExtra(ExportActivity.PASSWORD_EXTRA,
                            new ParcelableMessage(password));
                    exportIntent.putExtra(ExportActivity.VAULT_EXTRA,
                            new ParcelableMessage(words + "\r\n" + data));
                    startActivity(exportIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Keep the password for 2 minutes
     */
    private void eraseGlobalPassword() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        global_password = "";
                    }
                },
                2 * 60 * 1000);
    }

    /**
     * Return to the MainActivity
     */
    @Override
    public void onBackPressed() {
        Intent maIntent = new Intent(this, MainActivity.class);
        startActivity(maIntent);
    }

    /**
     * It calls the system's authentication screen
     *
     * @param requestCode {int}
     */
    private void showAuthenticationScreen(int requestCode) {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
            if (intent != null) {
                startActivityForResult(intent, requestCode);
            }
        }
    }

    /**
     * It calls the ExportActivity
     *
     * @param item {MenuItem}
     */
    public void exportVault(MenuItem item) {
        showAuthenticationScreen(EXPORT_VAULT_REQUEST_CODE);
    }

    /**
     * In order to update the vote status
     *
     * @param item {MenuItem}
     */
    public void refreshPage(MenuItem item) {
        WebView webView = findViewById(R.id.webview1);
        setWebView(this, webView, "file:///android_asset/website/home.html?nonce=" +
                randomAlphanumeric(5));
    }

    /**
     * It deletes the vault in the mobile and in the login provider
     *
     * @param item {MenuItem}
     */
    public void deleteVault(MenuItem item) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(R.string.delete_vault)
                .setMessage(R.string.delete_vault_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor =
                                getSharedPreferences("com.kantpoll.android",
                                        Context.MODE_PRIVATE).edit();
                        editor.putString("token", null);
                        editor.putString("user", null);
                        editor.putString("words", null);
                        editor.putString("data", null);
                        editor.putString("address", null);
                        editor.putString("certificate", null);
                        editor.putString("registered", null);
                        editor.putString(UserAuthentication.PASSWORD_KEY, null);
                        editor.apply();

                        Toast.makeText(getApplication(), R.string.deleted,
                                Toast.LENGTH_LONG).show();
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        WebView webView = findViewById(R.id.webview1);
                        webView.loadUrl("file:///android_asset/website/home.html?mode=start");
                        Intent maIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(maIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * It generates the password
     *
     * @return password
     */
    private static String randomAlphanumeric(int size) {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < size) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    /**
     * Parse a URI String into Name-Value Collection
     *
     * @param url {URL}
     * @return a map with parameters
     * @throws UnsupportedEncodingException not UTF-8
     */
    private static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}