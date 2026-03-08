package lab.minitomcat.chapter8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StaticResourceProcessor {
    private static final int BUFFER_SIZE = 1024;

    private static final String FILE_NOT_FOUND_MESSAGE = """
            HTTP/1.1 404 File Not Found\r
            Content-Type: text/html\r
            Content-Length: 23\r
            \r
            <h1>File Not Found</h1>
            """;

    private static final String OK_MESSAGE = """
            HTTP/1.1 ${StatusCode} ${StatusName}\r
            Content-Type: text/html\r
            Content-Length: ${ContentLength}\r
            Server: minit\r
            Date: ${ZonedDateTime}\r
            Connection: close\r
            \r
            """;

    /**
     * 处理静态资源响应
     */
    public void process(HttpRequest request, HttpResponse response) {
        byte[] buffer = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        OutputStream outputStream = null;
        try {
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            outputStream = response.getOutput();
            if (file.exists()) {
                // 响应头
                String header = composeResponseHeader(file);
                outputStream.write(header.getBytes(StandardCharsets.UTF_8));
                // 响应体
                fis = new FileInputStream(file);
                int read = fis.read(buffer, 0, BUFFER_SIZE);
                while (read != -1) {
                    outputStream.write(buffer, 0, read);
                    read = fis.read(buffer, 0, BUFFER_SIZE);
                }
                outputStream.flush();
            } else {
                // 文件不存在
                outputStream.write(FILE_NOT_FOUND_MESSAGE.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            }
        }
    }

    /**
     * 组装响应头
     */
    private String composeResponseHeader(File file) {
        return OK_MESSAGE.replace("${StatusCode}", "200")
                .replace("${StatusName}", "OK")
                .replace("${ContentLength}", String.valueOf(file.length()))
                .replace("${ZonedDateTime}", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
    }
}
