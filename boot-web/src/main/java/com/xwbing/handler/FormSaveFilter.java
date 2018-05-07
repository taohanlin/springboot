package com.xwbing.handler;

import com.alibaba.fastjson.JSON;
import com.xwbing.util.RestMessage;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 项目名称: boot-module-demo
 * 创建时间: 2018/5/7 10:11
 * 作者: xiangwb
 * 说明:
 */
public class FormSaveFilter implements Filter {
    private String[] paths;
    private String[] types;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String excludePath = filterConfig.getInitParameter("excludePath");
        paths = excludePath.split(",");
        String excludeType = filterConfig.getInitParameter("excludeType");
        types = excludeType.split(",");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();
        for (String p : paths) {
            if (path.equals(p)) {
                chain.doFilter(request, response);
                return;
            }
        }
        for (String p : types) {
            if (path.contains(p)) {
                chain.doFilter(request, response);
                return;
            }
        }
        if (path.contains("save")) {
            HttpSession session = request.getSession();
            Object sign = session.getAttribute("sign");
            String signValue = request.getHeader("sign");
            if (sign != null && sign.equals(signValue)) {
                session.removeAttribute("sign");
                chain.doFilter(request, response);
            } else {
                getOutputStream(response,"请不要重复提交");
            }

        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {

    }

    private void getOutputStream(HttpServletResponse response, String msg) {
        try {
            OutputStream outputStream = response.getOutputStream();
            RestMessage restMessage = new RestMessage();
            restMessage.setMessage(msg);
            outputStream.write(JSON.toJSONString(restMessage).getBytes("utf-8"));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
