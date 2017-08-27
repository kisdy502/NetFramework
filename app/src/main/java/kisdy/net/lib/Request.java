package kisdy.net.lib;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求类
 * Created by Administrator on 2017/8/25.
 */

public abstract class Request<T> implements Comparable<Request<T>> {

    private final static String TAG = "Request";

    /**
     * http请求方法枚举,这里我们只有GET, POST, PUT, DELETE四种
     *
     * @author mrsimple
     */
    public static enum HttpMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        DELETE("DELETE");

        /**
         * http request type
         */
        private String mHttpMethod = "";

        private HttpMethod(String method) {
            mHttpMethod = method;
        }

        @Override
        public String toString() {
            return mHttpMethod;
        }
    }

    /**
     * 优先级枚举
     *
     * @author mrsimple
     */
    public static enum Priority {
        LOW,
        NORMAL,
        HIGN,
        IMMEDIATE
    }

    /**
     * Default Content-type
     */
    public final static String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * Default encoding for POST or PUT parameters. See
     * {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    /**
     * 请求序列号
     */
    protected int mSerialNum = 0;
    /**
     * 优先级默认设置为Normal
     */
    protected Priority mPriority = Priority.NORMAL;
    /**
     * 是否取消该请求
     */
    protected boolean isCancel = false;

    /**
     * 该请求是否应该缓存
     */
    private boolean mShouldCache = true;
    /**
     * 请求的url
     */
    private String mUrl = "";
    /**
     * 请求的方法
     */
    HttpMethod mHttpMethod = HttpMethod.GET;

    /**
     * 请求的header
     */
    private Map<String, String> mHeaders = new HashMap<String, String>();
    /**
     * 请求参数
     */
    private Map<String, String> mRequestParams = new HashMap<String, String>();

    /**
     * @param url
     * @param listener
     */
    public Request(String url, RequestListener<T> listener) {
        this(HttpMethod.GET, url, listener);
    }


    /**
     * @param method
     * @param url
     * @param listener
     */
    public Request(HttpMethod method, String url, RequestListener<T> listener) {
        mHttpMethod = method;
        mUrl = url;
        mRequestListener = listener;
    }


    /**
     * 从原生的网络请求中解析结果,子类覆写
     *
     * @param response
     * @return
     */
    public abstract T parseResponse(Response response);


    /**
     * 处理Response,该方法运行在UI线程.
     *
     * @param response
     */
    public final void deliveryResponse(Response response) {
        // 解析得到请求结果
        T result = parseResponse(response);
        if (mRequestListener != null) {
            int stCode = response != null ? response.getStatusCode() : -1;
            String msg = response != null ? response.getMessage() : "unkown error";
            mRequestListener.onComplete(mSerialNum, stCode, result, msg);
        }
    }

    public String getUrl() {
        return mUrl;
    }


    public int getSerialNumber() {
        return mSerialNum;
    }

    public void setSerialNumber(int mSerialNum) {
        this.mSerialNum = mSerialNum;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority mPriority) {
        this.mPriority = mPriority;
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public HttpMethod getHttpMethod() {
        return mHttpMethod;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public Map<String, String> getParams() {
        return mRequestParams;
    }

    public void cancel() {
        isCancel = true;
    }

    public boolean isCanceled() {
        return isCancel;
    }

    public boolean isHttps() {
        return mUrl.startsWith("https");
    }

    /**
     * 返回POST或者PUT请求时的Body参数字节数组
     */
    public byte[] getBody() {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * 将参数拼接到请求URL后面
     */
    public void joinUrlParameters() {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(mUrl);
        if (!mUrl.contains("?")) {          //mUrl不包含？
            sbUrl.append("?").append(joinParameters(getParams(), getParamsEncoding()));
        } else if (mUrl.contains("?") && !mUrl.endsWith("?")) {
            sbUrl.append("&").append(joinParameters(getParams(), getParamsEncoding()));
        } else {                             //以?号结尾
            sbUrl.append(joinParameters(getParams(), getParamsEncoding()));
        }
        mUrl = sbUrl.toString();
    }

    private String joinParameters(Map<String, String> params, String paramsEncoding) {
        if(params.entrySet().isEmpty())
            return "";
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            String strParams = encodedParams.substring(0, encodedParams.length() - 1);  //去掉最后个&号
            return strParams;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * 将参数转换为Url编码的参数串
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        try {
            String strParams = joinParameters(params, paramsEncoding);
            return strParams.getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }


    @Override
    public int compareTo(@NonNull Request<T> stringRequest) {
        return 0;
    }

    /**
     * 请求Listener
     */
    protected RequestListener<T> mRequestListener;

    /**
     * 网络请求Listener,会被执行在UI线程
     *
     * @param <T> 请求的response类型
     * @author mrsimple
     */
    public static interface RequestListener<T> {
        /**
         * 请求完成的回调
         *
         * @param response
         */
        public void onComplete(int serialNum, int statusCode, T response, String errMsg);
    }

}
