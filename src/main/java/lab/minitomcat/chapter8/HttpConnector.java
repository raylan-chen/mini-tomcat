package lab.minitomcat.chapter8;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
     * session map 存储 session
     */
    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

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
            processor.start();
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
        } catch (IOException ex) {
            ex.printStackTrace();
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
                // 便于后续回收 httpProcessor
                httpProcessor.setConnector(this);
                // 处理请求
                httpProcessor.assign(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取 HttpProcessor
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
            processor.start();
            curProcessors++;
            return processor;
        }
    }

    /**
     * 回收 HttpProcessor
     */
    public void recycle(HttpProcessor processor) {
        synchronized (processors) {
            processors.push(processor);
        }
    }

    /**
     * 创建 HttpSession
     */
    public static HttpSession createSession() {
        Session session = new Session();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * 以随机方式生成byte数组, 形成sessionId
     */
    protected static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        // 线程安全
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0x0f);
            if (b1 < 10) {
                stringBuffer.append((char) ('0' + b1));
            } else {
                stringBuffer.append((char) ('A' + b1 - 10));
            }
            if (b2 < 10) {
                stringBuffer.append((char) ('0' + b2));
            } else {
                stringBuffer.append((char) ('A' + b2 -10));
            }
        }
        return stringBuffer.toString();
    }
}
