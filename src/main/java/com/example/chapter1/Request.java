package com.example.chapter1;

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
            bytes = input.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            bytes = -1;
        }
        // byte 转 char
        for (int i = 0; i < bytes; i++) {
            requestStr.append((char) buffer[i]);
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
