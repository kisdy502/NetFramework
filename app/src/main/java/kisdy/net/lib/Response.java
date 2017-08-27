package kisdy.net.lib;

import android.support.v4.util.ArrayMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 响应类
 * Created by Administrator on 2017/8/25.
 */

public class Response {

    public Response(int statuscode,String msg){
        statusCode=statuscode;
        message=msg;
        hreaders=new ArrayMap<String,String>();
    }

    private int statusCode;
    private String message;

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }


    private long contentLength;
    private String contentType;
    private String contentEncoding;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength(){
        return contentLength;
    }
    public void setContentLength(long length){
        contentLength=length;
    }

    public String getContentEncoding(){
        return contentEncoding;
    }

    public void setContentEncoding(String encoding) {
        this.contentEncoding = encoding;
    }


    public InputStream getResponseStream() {
        return responseStream;
    }

    public void setResponseStream(InputStream responseStream) {
        this.responseStream = responseStream;
        rawData=toByteArray(responseStream);
    }

    private InputStream responseStream;
    private byte[] rawData;

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public ArrayMap<String, String> getHreaders() {
        return hreaders;
    }

    public void setHeaders(ArrayMap<String, String> hreaders) {
        this.hreaders = hreaders;
    }

    private ArrayMap<String,String> hreaders;


    public  byte[] toByteArray(final InputStream instream) {
        if (instream == null) {
            return null;
        }
        if (contentLength > 1024*1024*256) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        int i = (int)contentLength;
        if (i < 0) {
            i = 4096;
        }
        ByteArrayOutputStream  buffer = new ByteArrayOutputStream(i);
        try {
            byte[] tmp = new byte[4096];
            int l;
            while((l = instream.read(tmp)) != -1) {
                buffer.write(tmp, 0, l);
            }
        }catch (IOException e){
            e.printStackTrace();
            return new byte[0];
        }finally {
            try {
                instream.close();
            }catch (IOException ex){

            }
        }
        return buffer.toByteArray();
    }

    @Override
    public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append(statusCode).append("\n")
                .append(message).append("\n")
                .append(contentLength).append("\n")
                .append(contentType).append("\n")
                .append(contentEncoding).append("\n");
        return sb.toString();
    }



    private void cacleContentEncoding(){

    }
}
