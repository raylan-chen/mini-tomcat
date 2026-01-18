package lab.minitomcat.chapter6;

import javax.servlet.Servlet;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ServletProcessor {

    private static final String OK_MESSAGE = """
            HTTP/1.1 ${StatusCode} ${StatusName}\r
            Content-Type: ${ContentType}\r
            Server: minit\r
            Date: ${ZonedDateTime}\r
            Connection: close\r
            \r
            """;

    /**
     * 处理动态请求
     */
    public void process(HttpRequest request, HttpResponse response) {
        URLClassLoader urlClassLoader = null;
        // 获取uri
        String uri = request.getUri();
        // 根据最后的 "/" 来定位 servlet
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        PrintWriter printWriter = null;

        // URLClassLoader 类加载器
        try {
            URL[] urls = new URL[1];
            URLStreamHandler urlStreamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, urlStreamHandler);
            urlClassLoader = new URLClassLoader(urls);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        // 动态加载 servlet
        Class<?> servletClass = null;
        try {
            servletClass = urlClassLoader.loadClass(servletName);
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }

        try {
            response.setCharacterEncoding("UTF-8");
            printWriter = response.getWriter();
            // 响应头
            String header = composeResponseHeader();
            printWriter.println(header);
            // 创建实例
            Servlet servlet = (Servlet) servletClass.newInstance();
            // 响应体
            servlet.service(request, response);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * 组装响应头
     */
    private String composeResponseHeader() {
        return OK_MESSAGE.replace("${StatusCode}", "200")
                .replace("${StatusName}", "OK")
                .replace("${ContentType}", "text/html;charset=utf-8")
                .replace("${ZonedDateTime}", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()))
                ;
        // 避免出现粘包/拆包的措施:
        // 1. 设置 Content-Length
        // 2. 指定 transfer-encoding: chunked
        // 3. 使用 短连接
    }
}
