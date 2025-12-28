<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Админ-панель" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

    <h1>Админ-панель</h1>

    <h2>Пользователи</h2>
    <c:if test="${empty users}">
        <p>Пользователей нет.</p>
    </c:if>
    <c:if test="${not empty users}">
        <table border="1" cellpadding="5" cellspacing="0">
            <tr>
                <th>ID</th>
                <th>Логин</th>
                <th>Email</th>
                <th>Роль</th>
            </tr>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td><c:out value="${u.id}" /></td>
                    <td><c:out value="${u.username}" /></td>
                    <td><c:out value="${u.email}" /></td>
                    <td><c:out value="${u.role}" /></td>
                </tr>
            </c:forEach>
        </table>
    </c:if>

    <h2 style="margin-top:30px;">Проекты</h2>
    <c:if test="${empty projects}">
        <p>Проектов нет.</p>
    </c:if>
    <c:if test="${not empty projects}">
        <table border="1" cellpadding="5" cellspacing="0">
            <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Описание</th>
                <th>Владелец (owner_id)</th>
                <th>Участники</th>
            </tr>
            <c:forEach var="p" items="${projects}">
                <tr>
                    <td><c:out value="${p.id}" /></td>
                    <td><c:out value="${p.name}" /></td>
                    <td><c:out value="${p.description}" /></td>
                    <td><c:out value="${p.ownerId}" /></td>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/project-members?projectId=${p.id}">
                            Управлять участниками
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />

