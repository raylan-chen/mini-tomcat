package lab.minitomcat.chapter7;

public class DefaultHeaders {
    static final String HOST_NAME = "host";
    // 连接类型(keep-alive/close)
    static final String CONNECTION_NAME = "connection";
    static final String ACCEPT_LANGUAGE_NAME = "accept-language";
    static final String CONTENT_LENGTH_NAME = "content-length";
    // Content-Type: text/html; charset=UTF-8
    static final String CONTENT_TYPE_NAME = "content-type";
    // 用于 chunked
    static final String TRANSFER_ENCODING_NAME = "transfer-encoding";
    // /servlet;jsessionid=123456?test=hello
    static final String JSESSIONID_NAME = ";jsessionid=";
    // Cookie: name=value; name2=value2
    static final String COOKIE_NAME = "Cookie";
}
