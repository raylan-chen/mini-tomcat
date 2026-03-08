package lab.minitomcat.chapter7;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequest implements HttpServletRequest {

    // --------------------------------------------------------- 成员变量

    InetAddress address;
    int port;

    private SocketInputStream socketInputStream;
    HttpRequestLine httpRequestLine = new HttpRequestLine();

    private String uri;
    private String queryString;
    private boolean parsed = false;
    private Map<String, String[]> parameters = new ConcurrentHashMap<>();

    private InputStream inputStream;

    private HashMap<String, String> headers = new HashMap<>();

    Cookie[] cookies;
    String sessionid;
    HttpSession session;
    SessionFacade sessionFacade;

    //  --------------------------------------------------------- 构造方法

    public HttpRequest(InputStream inputStream) {
        this.inputStream = inputStream;
        this.socketInputStream = new SocketInputStream(this.inputStream, 2048);
    }

     //  --------------------------------------------------------- 公共方法

     /**
     * 解析请求
     */
    public void parse(Socket socket) {
        try {
            // 1.解析 地址和端口
            parseConnection(socket);
            // 2.解析 请求头行
            this.socketInputStream.readRequestLine(httpRequestLine);
            // 从请求行中提取 uri, query string, sessionid
            parseRequestLine();
            // 3.解析 请求头, 提取 cookie, sessionid
            parseHeaders();
        } catch (IOException ex) {
            ex.printStackTrace();
        }  catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseParameters(Map<String, String[]> parameters, byte[] queryStringBytes, String encoding)
            throws UnsupportedEncodingException {
        if (parsed) {
            return;
        }
        if (queryStringBytes != null && queryStringBytes.length > 0) {
            int pos = 0;  // 当前参数段的起始位置
            int ix = 0;  // 读取索引
            int ox = 0;  // 写入偏移（相对于pos）
            String name = null;
            String value = null;
            while (ix < queryStringBytes.length) {
                byte c = queryStringBytes[ix++];
                switch (c) {
                    case '=':  // key=value
                        name = new String(queryStringBytes, pos, ox, encoding);
                        pos = ix;
                        ox = 0;
                        break;
                    case '&':  // key=value&key=value
                        value = new String(queryStringBytes, pos, ox, encoding);
                        if (name != null) {
                            putMapEntry(parameters, name, value);
                            name = null;
                        }
                        pos = ix;
                        ox = 0;
                        break;
                    case '+':  // key=value+with+spaces
                        queryStringBytes[pos + ox++] = (byte) ' ';
                        break;
                    case '%':  // key=value%20with%20spaces
                        if (ix + 1 < queryStringBytes.length) {
                            queryStringBytes[pos + ox++] = (byte) ((convertHexDigit(queryStringBytes[ix++]) << 4)
                                    + convertHexDigit(queryStringBytes[ix++]));
                        } else {
                            // 不完整的编码，保持原样或忽略(GET /servlet?test=hello%, GET /servlet?test=hello%2)
                            queryStringBytes[pos + ox++] = (byte) '%';
                            if (ix < queryStringBytes.length) {
                                queryStringBytes[pos + ox++] = queryStringBytes[ix++];
                            }
                        }
                        break;
                    default:
                        queryStringBytes[pos + ox++] = c;
                }
            }
            // 最后一个参数没有 '&' 结尾
            if (name != null) {
                value = new String(queryStringBytes, pos, ox, encoding);
                putMapEntry(parameters, name, value);
            }
        }
        parsed = true;
    }

    /**
     * 解析 Cookie 请求头, 从 headerValue 中提取 Cookie 对象数组
     */
    public Cookie[] parseCookieHeader(String headerValue) {
        if (headerValue == null || headerValue.length() < 1) {
            return new Cookie[0];
        }
        ArrayList<Cookie> cookieList = new ArrayList<>();
        while (headerValue.length() > 0) {
            // name=value; name2=value2
            int semicolonIndex = headerValue.indexOf(';');
            if (semicolonIndex < 0) {
                semicolonIndex = headerValue.length();
            }
            if (semicolonIndex == 0) {
                break;
            }
            String token = headerValue.substring(0, semicolonIndex);
            if (semicolonIndex < headerValue.length()) {
                headerValue = headerValue.substring(semicolonIndex + 1);
            } else {
                headerValue = "";
            }
            try {
                int equalsIndex = token.indexOf('=');
                if (equalsIndex > 0) {
                    String name = token.substring(0, equalsIndex).trim();
                    String value = token.substring(equalsIndex + 1).trim();
                    cookieList.add(new Cookie(name, value));
                }
            } catch (Throwable ex) {

            }
        }
        return cookieList.toArray(new Cookie[0]);
    }

    public String getUri() {
        return this.uri;
    }

    public String getSessionId() {
        return this.sessionid;
    }

    // ---------------------------------------------------------- protected 方法

    /**
     * 解析参数, 从 query string 和 body 中提取参数, 存储在 parameters 中
     */
    protected void parseParameters() {
        if (parsed) {
            return;
        }
        // parameters(url)
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            // 单字节编码, ISO-8859-1/latin-1, 不支持中文
            encoding = "ISO-8859-1";
        }
        String queryString = getQueryString();
        if (queryString != null) {
            byte[] queryStringBytes = new byte[queryString.length()];
            try {
                queryStringBytes = queryString.getBytes(encoding);
                parseParameters(this.parameters, queryStringBytes, encoding);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }

        // content type(for POST parameters parsing)
        String contentType = getContentType();
        if (contentType == null) {
            contentType = "";
        }
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex > 0) {
            contentType = contentType.substring(0, semicolonIndex).trim();
        } else {
            contentType = contentType.trim();
        }

        // parameters(POST, content-type: "...urlencoded", body)
        if ("POST".equals(getMethod()) && (getContentLength() > 0) && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int hasRead = 0;
                byte[] body = new byte[max];
                ServletInputStream servletInputStream = getInputStream();
                while (hasRead < max) {
                    int readCount = servletInputStream.read(body, hasRead, max - hasRead);
                    if (readCount < 0) {
                        break;
                    }
                    hasRead += readCount;
                }
                servletInputStream.close();
                if (hasRead < max) {
                    throw new IOException("Content length mismatch");
                }
                parseParameters(parameters, body, encoding);
            } catch (UnsupportedEncodingException ue) {

            } catch (IOException ie) {
                throw new RuntimeException("Content read fail");
            }
        }
    }

    //  --------------------------------------------------------- 私有方法

    /**
     * 解析地址和端口
     * @param socket
     */
    private void parseConnection(Socket socket) {
        address = socket.getInetAddress();
        port = socket.getPort();
    }

    /**
     * 解析请求头
     * @throws IOException
     * @throws ServletException
     */
    private void parseHeaders() throws IOException,ServletException {
        while (true) {
            HttpHeader httpHeader = new HttpHeader();
            // 读取请求头
            socketInputStream.readHeader(httpHeader);
            // 边界条件
            if (httpHeader.nameEnd == 0) {
                if (httpHeader.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }
            String name = new String(httpHeader.name, 0, httpHeader.nameEnd);
            String value = new String(httpHeader.value, 0, httpHeader.valueEnd);
            // 存储请求头
            headers.put(name, value);
            // 处理 cookie 和 sessionid
            if (DefaultHeaders.COOKIE_NAME.equals(name)) {
                Cookie[] cookies = parseCookieHeader(value);
                this.cookies = cookies;
                for (int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if ("jsessionid".equals(cookie.getName())) {
                        this.sessionid = cookie.getValue();
                    }
                }
            }
        }
    }

    /**
     * 解析请求行, 从 uri[] 中提取 uri 和 query string
     */
    private void parseRequestLine() {
        // 自定义 indexOf, 从 uri 数组中查找 '?' 字符的位置
        int queryIndex = httpRequestLine.indexOf(new char[]{'?'});
        if (queryIndex >= 0) {
            // /test/TestServlet;jsessionid=5AC6268DD8D4D5D1FDF5D41E9F2FD960?curAlbumID=9
            queryString = new String(httpRequestLine.uri, queryIndex + 1, httpRequestLine.uriEnd - queryIndex - 1);
            uri = new String(httpRequestLine.uri, 0, queryIndex);
            // 处理 jsessionid
            int semicolon = uri.indexOf(DefaultHeaders.JSESSIONID_NAME);
            if (semicolon > 0) {
                sessionid = uri.substring(semicolon + DefaultHeaders.JSESSIONID_NAME.length());
                uri = uri.substring(0, semicolon);
            }
        } else {
            // /test/TestServlet;jsessionid=5AC6268DD8D4D5D1FDF5D41E9F2FD960
            queryString = null;
            uri = new String(httpRequestLine.uri, 0, httpRequestLine.uriEnd);
            int semicolon = httpRequestLine.indexOf(DefaultHeaders.JSESSIONID_NAME);
            if (semicolon > 0) {
                sessionid = uri.substring(semicolon + DefaultHeaders.JSESSIONID_NAME.length());
                uri = uri.substring(0, semicolon);
            }
        }
    }

    private void putMapEntry(Map<String, String[]> parameters, String name, String value) {
        String[] newValues = null;
        String[] oldValues = parameters.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        parameters.put(name, newValues);
    }

    private byte convertHexDigit(byte b) {
        if (b >= '0' && b <= '9') return (byte) (b - '0');
        if (b >= 'a' && b <= 'f') return (byte) (b - 'a' + 10);
        if (b >= 'A' && b <= 'F') return (byte) (b - 'A' + 10);
        return 0;
    }

    //  --------------------------------------------------------- HttpServletRequest 接口实现方法

    @Override
    public String getAuthType() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return "";
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return new String(this.httpRequestLine.method, 0, httpRequestLine.methodEnd);
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (sessionFacade != null) {
            return sessionFacade;
        }
        // 从 url/headers 中获取 sessionid
        if (sessionid != null) {
            session = HttpConnector.sessions.get(sessionid);
            if (session == null) {
                session = HttpConnector.createSession();
            }
            sessionFacade = new SessionFacade(session);
            return sessionFacade;
        }
        session = HttpConnector.createSession();
        sessionFacade = new SessionFacade(session);
        return sessionFacade;
    }

    @Override
    public HttpSession getSession() {
        return this.sessionFacade;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return List.of();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        // 此处存在错误(content-type, accept-charset)
        return headers.get(DefaultHeaders.TRANSFER_ENCODING_NAME);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        String contentLength = headers.get(DefaultHeaders.CONTENT_LENGTH_NAME);
        if (contentLength == null) {
            // 未知长度返回 -1
            return -1;
        }
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException nx) {
            return -1;
        }
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return headers.get(DefaultHeaders.CONTENT_TYPE_NAME);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.socketInputStream;
    }

    @Override
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        if (values != null) {
            return values[0];
        } else {
            return null;
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return this.parameters;
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
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

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
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
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
