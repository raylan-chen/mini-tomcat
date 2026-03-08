package lab.minitomcat.chapter7;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {

    private boolean available = false;

    private Socket socket = null;

    private HttpConnector connector = null;

    public HttpProcessor() {}

    /**
     * 新建线程
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        // 循环等待 socket
        while (true) {
            await();
        }
    }

    /**
     * 等待 socket, 调用 process 处理业务请求
     */
    private synchronized void await() {
        // 初始值为 false, 进入循环等待 socket
        while (!available) {
            try {
                wait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 退出等待
        if (socket != null) {
            // 处理请求
            this.process(socket);
        }
        available = false;
        // 回收自身
        connector.recycle(this);
        notifyAll();
    }

    /**
     * connector 分配 socket, 处理请求
     */
    public synchronized void assign(Socket socket) {
        // 初始值为 false, 无法进入循环
        while (available) {
            try {
                wait();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 得到 socket, 停止等待
        this.socket = socket;
        available = true;
        notifyAll();
    }

    /**
     * 处理业务请求
     * @param socket
     */
    public void process(Socket socket) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            HttpRequest request = new HttpRequest(input);
            request.parse(socket);

            // handle session
            if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
                request.getSession(true);
            }

            HttpResponse response = new HttpResponse(output);
            response.setRequest(request);

            if (request.getUri().startsWith("/servlet")) {
                ServletProcessor processor = new ServletProcessor();
                // 处理业务请求
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

}
