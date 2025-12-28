<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Новое ревью" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Создание ревью</h1>
        <p class="page-subtitle">Проект: <c:out value="${project.name}" /></p>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/reviews?projectId=${project.id}" 
       style="color: #2563eb; text-decoration: none;">
        Назад к списку ревью
    </a>
</p>

<c:if test="${not empty param.error}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>

<div class="section">
    <form action="${pageContext.request.contextPath}/reviews/new" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="projectId" value="${project.id}" />

        <div class="form-group">
            <label class="form-label">Заголовок ревью:</label>
            <input type="text" 
                   name="title" 
                   class="form-input" 
                   required 
                   placeholder="Краткое описание изменений" />
        </div>

        <div class="form-group">
            <label class="form-label">Описание ревью (общее):</label>
            <textarea name="content" 
                      class="form-input" 
                      rows="4" 
                      placeholder="Подробное описание целей и контекста этого ревью"></textarea>
        </div>

        <div class="section-header">Файл с кодом (опционально)</div>
        <p style="font-size: 14px; color: #6b7280; margin-bottom: 16px;">
            Можно сразу прикрепить один файл с кодом, который относится к этому ревью.
        </p>

        <div class="form-group">
            <label class="form-label">Имя файла (например, <code style="color: #2563eb;">src/main.c</code>):</label>
            <input type="text" 
                   name="filename" 
                   class="form-input" 
                   value="${empty fileName ? '' : fileName}" 
                   placeholder="path/to/file.ext" />
        </div>

        <div class="form-group">
            <label class="form-label">Содержимое файла:</label>
            <textarea name="fileContent" 
                      class="form-input" 
                      rows="12" 
                      style="font-family: monospace; font-size: 13px;"><c:out value="${empty fileContent ? '' : fileContent}" /></textarea>
        </div>

        <div style="display: flex; gap: 12px;">
            <button type="submit" class="btn btn-primary">Создать ревью</button>
            <a href="${pageContext.request.contextPath}/reviews?projectId=${project.id}" 
               class="btn btn-secondary">
                Отмена
            </a>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
