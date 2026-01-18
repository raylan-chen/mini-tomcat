package lab.minitomcat.chapter6;

import java.io.IOException;
import java.io.InputStream;

public class SocketInputStream extends InputStream {
    // ---------------------------------------------------------- 成员变量

    /**
     * Carriage Return 回位
     */
    private static final byte CR = (byte) '\r';
    /**
     * Line Feed 换行
     */
    private static final byte LF = (byte) '\n';
    /**
     * Space 空格
     */
    private static final byte SP = (byte) ' ';
    /**
     * Horizontal Tabulation 水平制表符
     */
    private static final byte HT = (byte) '\t';
    private static final byte COLON =  (byte) ':';
    private static final int LOWER_LETTER_OFFSET = 'A' - 'a';

    protected InputStream inputStream;

    protected byte[] buf;
    protected int pos;
    protected int count;

    /**
     * 字符串常量
     */
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String PROTOCOL = "protocol";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    //  ---------------------------------------------------------- 成员方法

    /**
     * 构造函数
     */
    public SocketInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        this.buf = new byte[bufferSize];
    }

    /**
     * 从输入流中读取 HTTP 请求行
     */
    public void readRequestLine(HttpRequestLine httpRequestLine)
            throws IOException {
        int character = 0;
        int readCount = 0;
        int maxRead = 0;

        // 1.跳过行前的空白字符
        do {
            character = read();
        } while (character == CR ||  character == LF);

        // 2.method
        pos--;
        maxRead = httpRequestLine.method.length;
        while (true) {
            if (readCount >= maxRead) {
                httpRequestLine.method = ensureBufferCapacity(SocketInputStream.METHOD, httpRequestLine.method, HttpRequestLine.MAX_METHOD_SIZE);
                maxRead =  httpRequestLine.method.length;
            }
            character = read();
            if (character < 0) {
                throw new IOException("Unexpected end of stream while reading " + SocketInputStream.METHOD);
            }
            // method 以空格结束
            if (character == SP) {
                break;
            }
            httpRequestLine.method[readCount] = (char) character;
            readCount++;
        }
        httpRequestLine.methodEnd = readCount;

        // 3.uri
        readCount = 0;
        maxRead = httpRequestLine.uri.length;
        while (true) {
            if (readCount >= maxRead) {
                httpRequestLine.uri = ensureBufferCapacity(SocketInputStream.URI, httpRequestLine.uri, HttpRequestLine.MAX_URI_SIZE);
                maxRead = httpRequestLine.uri.length;
            }
            character = read();
            if (character < 0) {
                throw new IOException("Unexpected end of stream while reading " + SocketInputStream.URI);
            }
            if (character == SP) {
                break;
            }
            httpRequestLine.uri[readCount] = (char) character;
            readCount++;
        }
        httpRequestLine.uriEnd = readCount;

        // 4.protocol
        readCount = 0;
        maxRead = httpRequestLine.protocol.length;
        while (true) {
            if (readCount >= maxRead) {
                httpRequestLine.protocol = ensureBufferCapacity(SocketInputStream.PROTOCOL, httpRequestLine.protocol, HttpRequestLine.MAX_PROTOCOL_SIZE);
                maxRead = httpRequestLine.protocol.length;
            }
            character = read();
            if (character < 0) {
                throw new IOException("Unexpected end of stream while reading " + SocketInputStream.PROTOCOL);
            }
            if (character == CR) {
                // skip CR
            } else if (character == LF) {
                break;
            } else {
                httpRequestLine.protocol[readCount] = (char) character;
                readCount++;
            }
        }
        httpRequestLine.protocolEnd = readCount;
    }

     /**
     * 从输入流中读取 HTTP 请求头
     */
     public void readHeader(HttpHeader httpHeader)
             throws IOException {
         // Checking for a blank line
         int character = read();
         if (character == CR ||  character == LF) {
             if (character == CR) {
                 // skip LF
                 read();
             }
             httpHeader.nameEnd = 0;
             httpHeader.valueEnd = 0;
             return;
         }

         // 读取 name
         pos--;
         int readCount = 0;
         int maxRead = httpHeader.name.length;
         while (true) {
             if (readCount >= maxRead) {
                 httpHeader.name = ensureBufferCapacity(SocketInputStream.NAME, httpHeader.name, HttpHeader.MAX_NAME_SIZE);
                 maxRead = httpHeader.name.length;
             }
             character = read();
             if (character < 0) {
                 throw new IOException("Unexpected end of stream while reading " + SocketInputStream.NAME);
             }
             // name: value
             if (character == COLON) {
                 break;
             }
             char tmp = (char) character;
             if (tmp >= 'A' && tmp <= 'Z') {
                 tmp = (char) (tmp - LOWER_LETTER_OFFSET);
             }
             httpHeader.name[readCount] = tmp;
             readCount++;
         }
         httpHeader.nameEnd = readCount;

         // 读取 header 值(header 的 value 可以跨越多行)
         readCount = 0;
         maxRead = httpHeader.value.length;
         while (true) {
             // 跳过空格
             while (true) {
                 character = read();
                 if (character < 0) {
                     throw new IOException("Unexpected end of stream while reading " + SocketInputStream.VALUE);
                 }
                 if (character != SP && character != HT) {
                     pos--;
                     break;
                 }
             }
             // value
             while (true) {
                 if (readCount >= maxRead) {
                     httpHeader.value = ensureBufferCapacity(SocketInputStream.VALUE, httpHeader.value, HttpHeader.MAX_VALUE_SIZE);
                     maxRead = httpHeader.value.length;
                 }
                 character = read();
                 if (character < 0) {
                     throw new IOException("Unexpected end of stream while reading " + SocketInputStream.VALUE);
                 }
                 if (character == CR) {
                     // skip CR
                 } else if (character == LF) {
                     break;
                 } else {
                     httpHeader.value[readCount] = (char) character;
                     readCount++;
                 }
             }
             // 若下一行以 SP 或 HT 开头，则它是上一个 header 的折叠延续
             character = read();
             if (character != SP && character != HT) {
                 pos--;
                 break;
             }
             httpHeader.value[readCount] = ' ';
             readCount++;
         }
         httpHeader.valueEnd = readCount;
     }

    /**
     * 确保请求行字段的缓冲区有足够容量, 必要时进行扩容
     */
    private char[] ensureBufferCapacity(String fieldName, char[] buffer, int maxBufferSize)
            throws IOException {
        int curBufferSize = buffer.length;
        if ((2 * curBufferSize) <= maxBufferSize) {
            char[] newBuffer = new char[2 * curBufferSize];
            System.arraycopy(buffer, 0, newBuffer, 0, curBufferSize);
            return newBuffer;
        } else {
            throw new IOException(fieldName + " exceeds maximum length of " + maxBufferSize);
        }
    }

    //  ---------------------------------------------------------- 重写方法

    /**
     * 先从输入流中批量读取字节数据放到缓冲区中, 接着返回缓冲区的下一个字节
     */
    @Override
    public int read() throws IOException {
        // 倘若下一个字节索引等于缓冲区大小，则需要填充缓冲区
        if (pos >= count) {
            fill();
            if (pos >= count) {
                return -1;
            }
        }
        // 倘若缓冲区中还有未读字节，则直接从缓冲区中取出字节数据
        // & 0xff: 防止 byte 为负数转 int 时，byte 符号位被扩展为 int 符号位(示例: 0xff → 0xffff_ffff)
        return buf[pos++] & 0xff;
    }

    /**
     * 填充缓冲区
     */
    protected void fill() throws IOException {
        pos = 0;
        count = 0;
        int nRead = inputStream.read(buf, 0, buf.length);
        if (nRead > 0) {
            count = nRead;
        }
    }
}
