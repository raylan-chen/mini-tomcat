package lab.minitomcat.chapter8;

import javax.servlet.Servlet;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class ServletProcessor {

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

        // 响应头
        try {
            response.setCharacterEncoding("UTF-8");
            response.sendHeaders();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        try {
            // 创建实例
            Servlet servlet = (Servlet) servletClass.newInstance();
            // 门面模式
            HttpRequestFacade httpRequestFacade = new HttpRequestFacade(request);
            HttpResponseFacade httpResponseFacade = new HttpResponseFacade(response);
            // 响应体
            servlet.service(httpRequestFacade, httpResponseFacade);
        } catch (Throwable throwable) {
            System.out.println(throwable.toString());
        }
    }
}
