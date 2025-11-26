package lab.minitomcat.chapter2;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Response {

    private static final int BUFFER_SIZE = 1024;

    private OutputStream output;

    private Request request;

    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * 构造函数
     */
    public Response(OutputStream output) {
        this.output = output;
    }

    /**
     * 发送静态资源
     */
    public void sendStaticResource() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    int read = fis.read(buffer, 0, BUFFER_SIZE);
                    while (read != -1) {
                        output.write(buffer, 0, read);
                        read = fis.read(buffer, 0, BUFFER_SIZE);
                    }
                    output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String body = "<h1>File Not Found</h1>";
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                        "\r\n" +
                        body;
                output.write(errorMessage.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
