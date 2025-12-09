package lab.minitomcat.chapter5;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;

public class HttpConnector implements Runnable {
    /**
     * 相关配置参数
     */
    private int minProcessors = 3;
    private int maxProcessors = 10;
    private int curProcessors = 0;
    /**
     * 池化, 减少创建对象开销
     */
    private Deque<HttpProcessor> processors = new ArrayDeque<>();

    /**
     * 启动 HttpConnector
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        // 初始化 HttpProcessor 池
        initializeProcessors();
        // 等待请求
        await();
    }

    /**
     * 初始化 HttpProcessor 池
     */
    private void initializeProcessors() {
        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor processor = new HttpProcessor();
            processors.push(processor);
        }
    }

    /**
     * 等待请求
     */
    public void await() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            Socket socket = null;
            try {
                // 接收请求连接
                socket = serverSocket.accept();
                // 10s 读取超时
                socket.setSoTimeout(10000);
                // Http Processor
                HttpProcessor httpProcessor = getProcessors();
                // no processor
                if (httpProcessor == null) {
                    socket.close();
                    continue;
                }
                // 处理请求
                httpProcessor.process(socket);
                // 处理完毕放回池子
                processors.push(httpProcessor);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取 HttpProcessor
     * @return
     */
    private HttpProcessor getProcessors() {
        synchronized (processors) {
            // 池中存在空闲的 HttpProcessor
            if (!processors.isEmpty()) {
                return processors.pop();
            }
            // 达到最大限制数
            if (curProcessors == maxProcessors) {
                return null;
            }
            // create new
            HttpProcessor processor = new HttpProcessor();
            curProcessors++;
            return processor;
        }
    }
}
