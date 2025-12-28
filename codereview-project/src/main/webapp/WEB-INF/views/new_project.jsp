<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Новый проект" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Создать новый проект</h1>
        <p class="page-subtitle">Заполните форму для создания проекта</p>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/projects" 
       style="color: #2563eb; text-decoration: none;">
        Назад к списку проектов
    </a>
</p>

<c:if test="${not empty param.error}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>

<div class="section">
    <form action="${pageContext.request.contextPath}/projects/new" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <div class="form-group">
            <label class="form-label">Название проекта:</label>
            <input type="text" 
                   name="name" 
                   class="form-input" 
                   required 
                   placeholder="Введите название проекта" />
        </div>

        <div class="form-group">
            <label class="form-label">Описание проекта:</label>
            <textarea name="description" 
                      class="form-input" 
                      rows="6" 
                      placeholder="Краткое описание проекта (необязательно)"></textarea>
        </div>

        <div style="display: flex; gap: 12px;">
            <button type="submit" class="btn btn-primary">Создать проект</button>
            <a href="${pageContext.request.contextPath}/projects" 
               class="btn btn-secondary">
                Отмена
            </a>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
