package lab.minitomcat.chapter8;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpResponse implements HttpServletResponse {
    // ----------------------------------------------------------- 成员变量

    HttpRequest request;

    OutputStream output;
    String charset = null;
    String characterEncoding = null;
    PrintWriter writer;

    String contentType = null;
    long contentLength = -1;

    String protocol = "HTTP/1.1";
    int status = HttpServletResponse.SC_OK;
    String message = getStatusMessage(HttpServletResponse.SC_OK);
    Map<String, String> headers = new ConcurrentHashMap<>();

    // ----------------------------------------------------------- 构造方法

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    // ----------------------------------------------------------- 公共方法

    public void sendHeaders() throws IOException {
        PrintWriter printWriter = getWriter();
        printWriter.print(getProtocol());
        printWriter.print(" ");
        printWriter.print(status);
        if (message != null) {
            printWriter.print(" ");
            printWriter.print(message);
        }
        printWriter.print("\r\n");
        if (contentType != null) {
            printWriter.print("Content-Type: ");
            printWriter.print(contentType);
            printWriter.print("\r\n");
        }
        if (contentLength != -1) {
            // 避免出现粘包/拆包的措施:
            // 1.设置 Content-Length, 2.指定 transfer-encoding: chunked, 3.使用 短连接
            printWriter.print("Content-Length: ");
            printWriter.print(contentLength);
            printWriter.print("\r\n");
        }

        for (String name : headers.keySet()) {
            printWriter.print(name);
            printWriter.print(": ");
            printWriter.print(headers.get(name));
            printWriter.print("\r\n");
        }

        printWriter.print("\r\n");
        printWriter.flush();
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getRequest() {
        return this.request;
    }

    public OutputStream getOutput() {
        return this.output;
    }

    public String getProtocol() {
        return this.protocol;
    }

    // ----------------------------------------------------------- protected 方法

    protected String getStatusMessage(int status) {
        switch (status) {
            case HttpServletResponse.SC_OK -> {
                return "OK";
            }
            case HttpServletResponse.SC_ACCEPTED -> {
                return "Accepted";
            }
            case HttpServletResponse.SC_BAD_GATEWAY -> {
                return "Bad Gateway";
            }
            case HttpServletResponse.SC_CONTINUE -> {
                return "Continue";
            }
            case HttpServletResponse.SC_FORBIDDEN -> {
                return "Forbidden";
            }
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR -> {
                return "Internal Server Error";
            }
            case HttpServletResponse.SC_METHOD_NOT_ALLOWED -> {
                return "Method Not Allowed";
            }
            case HttpServletResponse.SC_NOT_FOUND -> {
                return "Not Found";
            }
            case HttpServletResponse.SC_NOT_IMPLEMENTED -> {
                return "Not Implemented";
            }
            case HttpServletResponse.SC_REQUEST_URI_TOO_LONG -> {
                return "Request URI Too Long";
            }
            case HttpServletResponse.SC_SERVICE_UNAVAILABLE -> {
                return "Service Unavailable";
            }
            case HttpServletResponse.SC_UNAUTHORIZED -> {
                return "Unauthorized";
            }
            default -> {
                return ("HTTP Response Status " + status);
            }
        }
    }

    // ----------------------------------------------------------- 接口实现方法
    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return "";
    }

    @Override
    public String encodeRedirectURL(String url) {
        return "";
    }

    @Override
    public String encodeUrl(String url) {
        return "";
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return "";
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            contentLength = Long.parseLong(value);
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            contentType = value;
        }
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_LENGTH_NAME)) {
            contentLength = Long.parseLong(value);
        }
        if (name.equalsIgnoreCase(DefaultHeaders.CONTENT_TYPE_NAME)) {
            contentType = value;
        }
    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
        this.message = getStatusMessage(status);
    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return List.of();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(output, getCharacterEncoding()), true);
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = charset;
    }

    @Override
    public void setContentLength(int len) {
        this.contentLength = len;
    }

    @Override
    public void setContentLengthLong(long len) {
        this.contentLength = len;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
