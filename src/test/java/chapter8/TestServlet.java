package chapter8;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class TestServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Enter doGet()");
        System.out.println("parameter name: " + req.getParameter("name"));
        HttpSession session = req.getSession(true);
        String user = (String) session.getAttribute("user");
        System.out.println("get user from session: "  + user);
        if (user == null || user.isEmpty()) {
            session.setAttribute("user", "yale");
        }
        resp.setCharacterEncoding("UTF-8");
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><Title>Test Servlet</Title></head>
                <body bdcolor="#f0f0f0">
                <h1 align="center">用户 ${user} 你好, 你的名字是 ${name}, 你的 session id 是 ${sessionId}</h1>
                """;
        doc = doc.replace("${user}", session.getAttribute("user").toString());
        doc = doc.replace("${name}", req.getParameter("name") == null ? "null" : req.getParameter("name"));
        doc = doc.replace("${sessionId}", session.getId());
        System.out.println(doc);
        resp.getWriter().println(doc);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("Enter doPost()");
        System.out.println("parameter name: " + req.getParameter("name"));
        resp.setCharacterEncoding("UTF-8");
        String doc = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><Title>Test Servlet</Title></head>
                <body bdcolor="#f0f0f0">
                <h1 align="center">Test 你好</h1>
                """;
        System.out.println(doc);
        resp.getWriter().println(doc);
    }
}
