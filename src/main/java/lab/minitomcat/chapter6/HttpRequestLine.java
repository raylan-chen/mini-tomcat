package lab.minitomcat.chapter6;

public class HttpRequestLine {
    /**
     * HTTP Request 头行规范: method uri protocol(示例: GET /hello.txt HTTP/1.1)
      */
    public static final int INITIAL_METHOD_SIZE = 8;
    public static final int INITIAL_URI_SIZE = 128;
    public static final int INITIAL_PROTOCOL_SIZE = 8;
    /**
     * max size
     */
    public static final int MAX_METHOD_SIZE = 32;
    public static final int MAX_URI_SIZE = 2048;
    public static final int MAX_PROTOCOL_SIZE = 32;
    /**
     * char[] 数组(存储 method/uri/protocol)及末尾字符索引
     */
    public char[] method;
    public int methodEnd;
    public char[] uri;
    public int uriEnd;
    public char[] protocol;
    public int protocolEnd;


    /**
     * 构造函数
     */
    public HttpRequestLine() {
        this(
                new char[INITIAL_METHOD_SIZE], 0,
                new char[INITIAL_URI_SIZE], 0,
                new char[INITIAL_PROTOCOL_SIZE], 0
        );
    }

    /**
     * 构造函数
     */
    public HttpRequestLine(
            char[] method, int methodEnd,
            char[] uri, int uriEnd,
            char[] protocol, int protocolEnd
    ) {
        this.method = method;
        this.methodEnd = methodEnd;
        this.uri = uri;
        this.uriEnd = uriEnd;
        this.protocol = protocol;
        this.protocolEnd = protocolEnd;
    }

    /**
     * 重置末尾索引
     */
    public void recycle() {
        methodEnd = 0;
        uriEnd = 0;
        protocolEnd = 0;
    }

    /**
     * 从 uri[] 中提取字符串
     */
    public int indexOf(String str) {
        return indexOf(str.toCharArray(), str.length());
    }

    /**
     * 从 uri[] 中提取字符串
     */
    public int indexOf(char[] buf) {
        return indexOf(buf, buf.length);
    }

    /**
     * 从 uri[] 中提取字符串
     */
    public int indexOf(char[] buf, int bufLen) {
        int pos = 0;
        while (pos < uriEnd) {
            // 定位首字符出现的位置
            pos = indexOf(buf[0], pos);
            if (pos < 0) {
                return -1;
            }
            // 校验长度
            if (uriEnd - pos < bufLen) {
                return -1;
            }
            // 字符比对
            for (int i = 1; i < bufLen; i++) {
                // 字符不一致, 重新寻找
                if (uri[pos + i] != buf[i]) {
                    break;
                }
                // 匹配成功
                if (i == bufLen - 1) {
                    return pos;
                }
            }
            pos++;
        }
        return -1;
    }

    /**
     * 从 uri[] 中查找字符 c 出现的位置
     */
    public int indexOf(char c, int start) {
        for (int i = start; i < uriEnd; i++) {
            if (uri[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
