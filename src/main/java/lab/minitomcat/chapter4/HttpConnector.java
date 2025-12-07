package lab.minitomcat.chapter4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpConnector implements Runnable {

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        await();
    }

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
                HttpProcessor httpProcessor = new HttpProcessor();
                httpProcessor.process(socket);
                // close the socket
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
