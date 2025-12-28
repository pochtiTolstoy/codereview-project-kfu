<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="now" class="java.util.Date" scope="page" />
</div> 

<footer class="footer">
    <span>&copy; <fmt:formatDate value="${now}" pattern="yyyy" /> Code Review Platform</span>
</footer>
</body>
</html>

