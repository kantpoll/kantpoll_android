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

import android.os.AsyncTask;
import android.webkit.WebResourceResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class TorRequest extends AsyncTask<String, Void, WebResourceResponse> {
    /**
     * Default TOR Proxy port.
     */
    private static final int PROXY_PORT = 9050;
    /**
     * Default TOR Proxy hostaddr.
     */
    private static final String PROXY_ADDR = "127.0.0.1";
    /**
     * Constant tells SOCKS4/4a to connect. Use it in the <i>req</i> parameter.
     */
    private static final byte TOR_CONNECT = (byte) 0x01;
    /**
     * Constant tells TOR to do a DNS resolve.  Use it in the <i>req</i> parameter.
     */
    //private static final byte TOR_RESOLVE = (byte) 0xF0;
    /**
     * Constant indicates what SOCKS version are talking
     * Either SOCKS4 or SOCKS4a
     */
    private static final byte SOCKS_VERSION = (byte) 0x04;
    /**
     * SOCKS uses Nulls as field delimiters
     */
    private static final byte SOCKS_DELIM = (byte) 0x00;
    /**
     * Setting the IP field to 0.0.0.1 causes SOCKS4a to
     * be enabled.
     */
    private static final int SOCKS4A_FAKEIP = (int) 0x01;

    /**
     * No request is equal due to the random aes encryption
     */
    static private HashMap responses = new HashMap();

    /**
     * This method creates a socket, then sends the inital SOCKS request info
     * It stops before reading so that other methods may
     * differently interpret the results. It returns the open socket.
     *
     * @param targetHostname The hostname of the destination host.
     * @return An open Socket that has been sent the SOCK4a init codes.
     * @throws IOException from any Socket problems
     */
    private static Socket TorSocketPre(String targetHostname)
            throws IOException {

        Socket s;
        s = new Socket(PROXY_ADDR, PROXY_PORT);
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        os.writeByte(SOCKS_VERSION);
        os.writeByte(TOR_CONNECT);
        // 2 bytes
        os.writeShort(80);
        // 4 bytes, high byte first
        os.writeInt(SOCKS4A_FAKEIP);
        os.writeByte(SOCKS_DELIM);
        os.writeBytes(targetHostname);
        os.writeByte(SOCKS_DELIM);
        return (s);
    }

    /**
     * This method creates a socket to the target host and port using TorSocketPre, then reads
     * the SOCKS information.
     *
     * @param targetHostname Hostname of destination host.
     * @return Fully initialized TCP Socket that tunnels to the target Host/Port via the Tor Proxy host/port.
     * @throws IOException when Socket and Read/Write exceptions occur.
     */
    private static Socket TorSocket(String targetHostname)
            throws IOException {
        Socket s = TorSocketPre(targetHostname);
        DataInputStream is = new DataInputStream(s.getInputStream());

        // only the status is useful on a TOR CONNECT
        is.readByte(); //version
        byte status = is.readByte();
        if (status != (byte) 90) {
            //failed for some reason, return useful exception
            throw (new IOException(ParseSOCKSStatus(status)));
        }
        is.readShort(); //port
        is.readInt(); //ipAddr
        return (s);
    }

    /**
     * This helper method allows us to decode the SOCKS4 status codes into
     * Human readable input.<br />
     * Based upon info from http://archive.socks.permeo.com/protocol/socks4.protocol
     *
     * @param status Byte containing the status code.
     * @return String human-readable representation of the error.
     */
    private static String ParseSOCKSStatus(byte status) {
        // func to turn the status codes into useful output reference
        String retval;
        switch (status) {
            case 90:
                retval = status + " Request granted.";
                break;
            case 91:
                retval = status + " Request rejected/failed - unknown reason.";
                break;
            case 92:
                retval = status + " Request rejected: SOCKS server cannot connect to identd on the client.";
                break;
            case 93:
                retval = status + " Request rejected: the client program and identd report different user-ids.";
                break;
            default:
                retval = status + " Unknown SOCKS status code.";
        }
        return (retval);
    }

    /**
     * It opens an onion page with some GET parameters
     *
     * @param params {String...}
     * @return {WebResourceResponse}
     */
    @Override
    protected WebResourceResponse doInBackground(String... params) {
        try {
            String old = (String) responses.get(params[0] + "/" + params[1]);
            String content;

            if (responses.size() > 100) {
                responses = new HashMap();
            }

            if (old != null) {
                content = old;
            } else {
                Socket socket = TorSocket(params[0]);

                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                pw.println("GET /" + params[1] + " HTTP/1.1");
                pw.println("Host: " + params[0]);
                pw.println("");
                pw.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Skipping the headers
                br.readLine(); //HTTP/1.1 200 OK
                br.readLine(); //Access-Control-Allow-Origin: *
                br.readLine(); //Content-Security-Policy: script-src 'unsafe-inline' ;
                br.readLine(); //Content-Type: text/plain; charset=utf-8
                br.readLine(); //Date: Tue, 18 Dec 2018 18:57:41 GMT
                br.readLine(); //Transfer-Encoding: chunked
                br.readLine(); //empty
                br.readLine(); //[length]

                content = br.readLine();
                responses.put(params[0] + "/" + params[1], content);
                socket.close();
            }

            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            WebResourceResponse response = new WebResourceResponse("text/html", "UTF-8", stream);

            Map headers = new HashMap();
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, OPTIONS");
            headers.put("Content-Security-Policy",
                    "script-src 'none' ; style-src 'none' ; child-src 'none'; object-src 'none'; form-action 'none' ;" +
                            " connect-src 'none' ; worker-src 'none'");
            response.setResponseHeaders(headers);
            return response;
        } catch (Exception e) {
            e.printStackTrace();

            InputStream stream = new ByteArrayInputStream("error".getBytes(StandardCharsets.UTF_8));
            WebResourceResponse response = new WebResourceResponse("text/html", "UTF-8", stream);

            response.setResponseHeaders(defaultHeaders());
            return response;
        }
    }

    /**
     * It makes up the headers which are sent to the webview
     * @return
     */
    private Map defaultHeaders(){
        Map headers = new HashMap();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, OPTIONS");
        headers.put("Content-Security-Policy",
                "script-src 'none' ; style-src 'none' ; child-src 'none'; object-src 'none'; form-action 'none' ;" +
                        " connect-src 'none' ; worker-src 'none'");
        return  headers;
    }
}
