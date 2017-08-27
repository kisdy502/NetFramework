/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 bboyfeiyu@gmail.com, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kisdy.net.lib;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;



/**
 * 使用HttpURLConnection执行网络请求的HttpSender
 * 
 * @author mrsimple
 */
public class HttpUrlConnectionSender implements IHttpSender {
    public final static String TAG="HttpUrlConnectionSender";
    /**
     * 配置Https
     */
    HttpUrlConnectionConfig mConfig = HttpUrlConnectionConfig.getConfig();

    @Override
    public Response sendRequest(Request<?> request) {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createUrlConnection(request.getUrl());   // 构建HttpURLConnection
            setRequestHeaders(urlConnection, request);              // 设置headers
            setRequestParams(urlConnection, request);                // 设置Body参数
            configHttps(request);                     // https 配置
            return fetchResponse(urlConnection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private HttpURLConnection createUrlConnection(String url) throws IOException {
        URL newURL = new URL(url);
        URLConnection urlConnection = newURL.openConnection();
        urlConnection.setConnectTimeout(mConfig.connTimeOut);
        urlConnection.setReadTimeout(mConfig.soTimeOut);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        return (HttpURLConnection) urlConnection;
    }

    private void configHttps(Request<?> request) {
        if (request.isHttps()) {
            SSLSocketFactory sslFactory = mConfig.getSslSocketFactory();
            // 配置https
            if (sslFactory != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory);
                HttpsURLConnection.setDefaultHostnameVerifier(mConfig.getHostnameVerifier());
            }

        }
    }

    private void setRequestHeaders(HttpURLConnection connection, Request<?> request) {
        Set<String> headersKeys = request.getHeaders().keySet();
        for (String headerName : headersKeys) {
            connection.addRequestProperty(headerName, request.getHeaders().get(headerName));
        }
    }

    protected void setRequestParams(HttpURLConnection connection, Request<?> request)
            throws ProtocolException, IOException {
        Request.HttpMethod method = request.getHttpMethod();
        connection.setRequestMethod(method.toString());
        // add params
        byte[] body = request.getBody();
        if (body != null) {
            // enable output
            connection.setDoOutput(true);
            // set content type
            connection.addRequestProperty(Request.HEADER_CONTENT_TYPE, request.getBodyContentType());
            // write params data to connection
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.write(body);
            dataOutputStream.close();
        }
    }

    private Response fetchResponse(HttpURLConnection connection) throws IOException {

        // Initialize HttpResponse with data from the HttpURLConnection.
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(connection.getContentEncoding()).append("\n")
                .append(connection.getContentType()).append("\n")
                .append(connection.getContentLength()).append("\n");
        Log.i(TAG,stringBuffer.toString());
        // 构建response
        Response response = new Response(responseCode,connection.getResponseMessage());
        response.setContentEncoding(connection.getContentEncoding());
        response.setContentType(connection.getContentType());
        response.setContentLength(connection.getContentLength());
        InputStream inputStream = null;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            inputStream = connection.getErrorStream();
        }
        response.setResponseStream(inputStream);

        addHeadersToResponse(response, connection);
        return response;
    }


    private void addHeadersToResponse(Response response, HttpURLConnection connection) {
        ArrayMap<String, String> headers=new ArrayMap<>();
        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                headers.put(header.getKey(),header.getValue().get(0));
            }
        }
        response.setHeaders(headers);
    }

}
