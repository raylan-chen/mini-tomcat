package lab.minitomcat.chapter3;

import java.io.InputStream;

/**
 * HTTP Server-Request
 */
public class Request {

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
            // 循环读取直到找到HTTP请求结束标记(\r\n\r\n)或达到最大读取次数
            int maxReadAttempts = 10; // 最多读取10次
            int readAttempts = 0;
            
            while (readAttempts < maxReadAttempts) {
                // 如果仅使用这一行代码, 可能会导致一直阻塞
                bytes = input.read(buffer);
                readAttempts++;
                
                if (bytes == -1) {
                    break; // 连接已关闭
                }
                
                // 将读取的数据添加到请求字符串中
                for (int i = 0; i < bytes; i++) {
                    requestStr.append((char) buffer[i]);
                }
                
                // 检查是否已经读取了完整的HTTP请求头(以\r\n\r\n结尾)
                String currentRequest = requestStr.toString();
                if (currentRequest.contains("\r\n\r\n")) {
                    break; // 已经读取了完整的HTTP请求头
                }
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
}
