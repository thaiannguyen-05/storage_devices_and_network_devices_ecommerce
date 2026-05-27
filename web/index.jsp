<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.sendRedirect(ctx + "/home");
%>
