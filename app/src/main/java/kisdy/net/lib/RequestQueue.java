package kisdy.net.lib;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求队列
 * Created by Administrator on 2017/8/26.
 */

public final class RequestQueue {
    /**
     * 请求队列 [ Thread-safe ]
     */
    private BlockingQueue<Request<?>> mRequestQueue = new PriorityBlockingQueue<Request<?>>();
    /**
     * 请求的序列化生成器
     */
    private AtomicInteger mSerialNumGenerator = new AtomicInteger(0);

    /**
     * 默认的核心数
     */
    public static int DEFAULT_CORE_NUMS = Runtime.getRuntime().availableProcessors() + 1;
    /**
     * CPU核心数 + 1个分发线程数
     */
    private int mDispatcherNums = DEFAULT_CORE_NUMS;
    /**
     * NetworkExecutor,执行网络请求的线程
     */
    private RequestExecutor[] mDispatchers = null;
    /**
     * Http请求的真正执行者
     */
    private IHttpSender mHttpSender;

    ExecutorService fixedThreadPool;

    public RequestQueue(){
        this(DEFAULT_CORE_NUMS,null);
    }

    /**
     * @param coreNums 线程核心数
     * @param httpSender http执行器
     */
    public RequestQueue(int coreNums, IHttpSender httpSender) {
        mDispatcherNums = coreNums;
        mHttpSender = httpSender != null ? httpSender : HttpSenderFactory.createHttpSender();
        fixedThreadPool = Executors.newFixedThreadPool(mDispatcherNums);
    }

    /**
     * 启动NetworkExecutor
     */
    private final void startNetworkExecutors() {
        mDispatchers = new RequestExecutor[mDispatcherNums];
        for (int i = 0; i < mDispatcherNums; i++) {
            mDispatchers[i] = new RequestExecutor(mRequestQueue, mHttpSender);
            fixedThreadPool.execute(mDispatchers[i]);
        }
    }

    public void start() {
        stop();
        startNetworkExecutors();
    }

    /**
     * 停止NetworkExecutor
     */
    public void stop() {
        if (mDispatchers != null && mDispatchers.length > 0) {
            for (int i = 0; i < mDispatchers.length; i++) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * 不能重复添加请求
     *
     * @param request
     */
    public void addRequest(Request<?> request) {
        if (!mRequestQueue.contains(request)) {
            request.setSerialNumber(this.generateSerialNumber());
            mRequestQueue.add(request);
        } else {
            Log.d("RequestQueue", "### 请求队列中已经含有");
        }
    }

    public void clear() {
        mRequestQueue.clear();
    }

    public BlockingQueue<Request<?>> getAllRequests() {
        return mRequestQueue;
    }

    /**
     * 为每个请求生成一个系列号
     *
     * @return 序列号
     */
    private int generateSerialNumber() {
        return mSerialNumGenerator.incrementAndGet();
    }
}
