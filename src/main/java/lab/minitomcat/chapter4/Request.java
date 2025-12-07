package lab.minitomcat.chapter4;

import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP Server-Request
 */
public class Request implements ServletRequest {

    private InputStream input;
    private String uri;

    /**
     * 构造函数
     */
    public Request(InputStream input) {
        this.input = input;
    }

    public String getUri() {
        return uri;
    }

    /**
     * 从Socket输入流中提取HTTP协议信息, 获取请求的uri
     */
    public void parse() {
        // byte 转 String 容器
        StringBuilder requestStr = new StringBuilder(2048);
        // 读取的字节数
        int bytes = 0;
        // input 缓冲
        byte[] buffer = new byte[1024];
        // IO 读取
        try {
            // TODO: 出现过一直阻塞的情况?
            bytes = input.read(buffer);
            // 将读取的数据添加到请求字符串中
            for (int i = 0; i < bytes; i++) {
                requestStr.append((char) buffer[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            bytes = -1;
        }
        uri = parseUri(requestStr.toString());
    }

    /**
     * 获取请求的uri
     */
    private String parseUri(String requestStr) {
        int index1, index2;
        // GET [path] HTTP/1.1
        index1 = requestStr.indexOf(' ');
        if (index1 != -1) {
            index2 = requestStr.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                return requestStr.substring(index1 + 1, index2);
            }
        }
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String s) {
        return "";
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Map.of();
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    @Override
    public void setAttribute(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return "";
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "";
    }

    @Override
    public String getLocalAddr() {
        return "";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
