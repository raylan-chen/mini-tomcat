package lab.minitomcat.chapter6;

public class HttpHeader {
    // ----------------------------------------------------------- 成员变量

    /**
     * HTTP 请求头格式(name: value)
     */
    public static final int INITIAL_NAME_SIZE = 64;
    public static final int INITIAL_VALUE_SIZE = 512;
    /**
     * max size
     */
    public static final int MAX_NAME_SIZE = 128;
    public static final int MAX_VALUE_SIZE = 1024;
    /**
     * char[] 存储 name/value
     */
    public char[] name;
    public int nameEnd;
    public char[] value;
    public int valueEnd;

    protected int hashCode = 0;

    // ----------------------------------------------------------- 构造函数

    public HttpHeader() {
        this(
                new char[INITIAL_NAME_SIZE], 0,
                new char[INITIAL_VALUE_SIZE], 0
        );
    }

    public HttpHeader(char[] name, int nameEnd, char[] value, int valueEnd) {
        this.name = name;
        this.nameEnd = nameEnd;
        this.value = value;
        this.valueEnd = valueEnd;
    }

    // ----------------------------------------------------------- 成员方法

    public void recycle() {
        nameEnd = 0;
        valueEnd = 0;
        hashCode = 0;
    }
}
