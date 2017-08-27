package kisdy.net;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import kisdy.net.lib.Request;
import kisdy.net.lib.RequestQueue;
import kisdy.net.lib.StringRequest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue queue = new RequestQueue();
        queue.start();

        String url = "http://www.qq.com/";
        Request request = new StringRequest(url, listener);
        queue.addRequest(request);

        url = "https://www.baidu.com/";
        request = new StringRequest(url, listener);
        queue.addRequest(request);

    }


    private Request.RequestListener<String> listener = new Request.RequestListener<String>() {
        @Override
        public void onComplete(int serialNum, int stCode, String response, String errMsg) {
            Log.i(TAG, "serialNum" + serialNum + ",response" + response);
        }
    };
}
