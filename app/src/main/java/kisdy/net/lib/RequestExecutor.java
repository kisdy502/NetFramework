package kisdy.net.lib;

import android.util.Log;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Administrator on 2017/8/26.
 */

public class RequestExecutor implements Runnable {

    /**
     * 网络请求队列
     */
    private BlockingQueue<Request<?>> mRequestQueue;
    /**
     * 网络请求栈
     */
    private IHttpSender mHttpSender;
    /**
     * 结果分发器,将结果投递到主线程
     */
    private static ResponseDelivery mResponseDelivery = new ResponseDelivery();
    /**
     * 请求缓存
     */
    private static Cache<String, Response> mReqCache = new LruMemCache();
    /**
     * 是否停止
     */
    private boolean isStop = false;

    public RequestExecutor(BlockingQueue<Request<?>> queue, IHttpSender httpSender) {
        mRequestQueue = queue;
        mHttpSender = httpSender;
    }

    @Override
    public void run() {
        try {
            while (!isStop) {
                final Request<?> request = mRequestQueue.take();
                Log.i("executor", "有请求来了,ID:"+request.getSerialNumber()+",url:"+request.getUrl());
                if (request.isCanceled()) {
                    Log.d("executor", "### 取消执行了");
                    continue;
                }
                Response response = null;

                response = mHttpSender.sendRequest(request);

                Log.i("executor", response.toString());

                // 分发请求结果
                mResponseDelivery.deliveryResponse(request, response);
            }
        } catch (InterruptedException e) {
            Log.i("", "### 请求分发器退出");
        }

    }

    private boolean isSuccess(Response response) {
        return response != null && response.getStatusCode() == 200;
    }



    public void quit() {
        isStop = true;
    }
}
