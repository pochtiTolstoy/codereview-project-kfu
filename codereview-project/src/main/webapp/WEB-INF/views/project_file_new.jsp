<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Новый файл проекта" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

    <h1>Новый файл в проекте: <c:out value="${project.name}" /></h1>

    <c:if test="${not empty param.error}">
        <p style="color:red;">Ошибка: <c:out value="${param.error}" /></p>
    </c:if>

    <form action="${pageContext.request.contextPath}/project/file/new" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="projectId" value="${project.id}" />

        <p>
            <label>Имя файла:<br/>
                <input type="text" name="filename" size="60" required />
            </label>
        </p>

        <p>
            <label>Содержимое файла:<br/>
                <textarea name="content" rows="20" cols="80"></textarea>
            </label>
        </p>

        <button type="submit">Создать файл</button>
    </form>

    <p>
        <a href="${pageContext.request.contextPath}/project?id=${project.id}">
            Назад к проекту
        </a>
    </p>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />

