package kisdy.net.lib;

/**
 * Created by Administrator on 2017/8/26.
 */

public interface IHttpSender {
    /**
     * 执行Http请求
     *
     * @param request 待执行的请求
     * @return
     */
    public Response sendRequest(Request<?> request);
}
