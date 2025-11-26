package lab.minitomcat.chapter3;

import java.io.OutputStream;

public class Response {

    private static final int BUFFER_SIZE = 1024;

    private OutputStream output;

    private Request request;

    /**
     * 构造函数
     */
    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OutputStream getOutput() {
        return output;
    }
}
