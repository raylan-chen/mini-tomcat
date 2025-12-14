package chapter5;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class HelloServlet implements Servlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            // 模拟业务请求耗时
            Thread.sleep(30_000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String doc = """
                <!DOCTYPE html> \n
                <html>\n
                <head><meta charset=\"utf-8\"><title>Test</title></head>\n
                <body bgcolor=\"#f0f0f0\">\n
                <h1 align=\"center\">Hello World 你好 世界</h1>\n
                """;
        // 可以在 service 中调用 setCharacterEncoding 方法来设置字符集
        servletResponse.getWriter().println(doc);
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public void destroy() {

    }
}
