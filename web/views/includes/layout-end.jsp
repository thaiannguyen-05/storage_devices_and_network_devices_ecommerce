<%@page contentType="text/html" pageEncoding="UTF-8"%>
        </main>
    <% if (session.getAttribute("authUserName") != null) { %>
    </div>
    <% } %>

    <% if (session.getAttribute("authUserName") != null) { %>
    <%@include file="footer.jsp" %>
    <% } %>
</body>
</html>
