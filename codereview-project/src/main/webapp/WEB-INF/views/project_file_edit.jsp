<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="pageTitle" value="Редактирование файла: ${file.filename}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

    <h1>Редактирование файла в проекте: <c:out value="${project.name}" /></h1>

    <c:if test="${not empty param.error}">
        <p style="color:red;">Ошибка: <c:out value="${param.error}" /></p>
    </c:if>

    <form action="${pageContext.request.contextPath}/project/file/edit" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${file.id}" />

        <p>
            <label>Имя файла:<br/>
                <input type="text" name="filename" size="60" value="${fn:escapeXml(file.filename)}" required />
            </label>
        </p>

        <p>
            <label>Содержимое файла:<br/>
                <textarea name="content" rows="20" cols="80">${fn:escapeXml(file.content)}</textarea>
            </label>
        </p>

        <button type="submit">Сохранить изменения</button>
    </form>

    <form action="${pageContext.request.contextPath}/project/file/delete" method="post"
          onsubmit="return confirm('Точно удалить этот файл?');">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${file.id}" />
        <button type="submit">Удалить файл</button>
    </form>

    <p>
        <a href="${pageContext.request.contextPath}/project/file/view?id=${file.id}">
            Назад к просмотру файла
        </a>
    </p>
    <p>
        <a href="${pageContext.request.contextPath}/project?id=${project.id}">
            Назад к проекту
        </a>
    </p>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />

