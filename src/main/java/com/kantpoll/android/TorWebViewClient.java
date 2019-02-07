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
 */

package com.kantpoll.android;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;

import info.guardianproject.netcipher.proxy.OrbotHelper;

class TorWebViewClient extends android.webkit.WebViewClient {
    private String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".JPG", ".JPEG", ".PNG", ".GIF"};

    /**
     * If the url of the request is an onion address it should be handled by the Orbot
     *
     * @param view    {WebView}
     * @param request {WebResourceRequest}
     * @return WebResourceResponse
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
        if (request.getUrl() != null){
            String urlStr = request.getUrl().toString();
            if (OrbotHelper.isOnionAddress(request.getUrl())) {
                String[] parts = urlStr.split("onion/");
                String url = parts[0].replace("http://", "") + "onion";
                String get_params = "";
                if (parts.length > 1) {
                    get_params = parts[1];
                }
                try {
                    return new TorRequest().execute(new String[]{url, get_params}).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (isImage(urlStr)){
                return null;
            }
        }

        return new WebResourceResponse("text/plain", "UTF-8",
                new ByteArrayInputStream("error".getBytes()));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        final Uri uri = Uri.parse(url);
        return handleOverride(view, uri.toString());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        final Uri uri = request.getUrl();
        return handleOverride(view, uri.toString());
    }

    /**
     * It opens external links in the browser
     *
     * @param view {WebView}
     * @param url  {String}
     * @return boolean
     */
    private boolean handleOverride(WebView view, String url) {
        if (url.startsWith("file:///android_asset/website/")) {
            return false;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(browserIntent);
        return true;
    }

    /**
     * Checks whether a given url points to a valid image. The following formats are treated as
     * images: png, jpg (and jpeg) and gif (Also in capital letters).
     *
     * @param url {String}
     * @return true when url points to a valid image.
     */
    private boolean isImage(String url){
        if (url == null || url.equals("") || !url.startsWith("https://")){
            return false;
        }
        for (String extension: imageExtensions){
            if (url.endsWith(extension)){
                return true;
            }
        }
        return false;
    }
}
