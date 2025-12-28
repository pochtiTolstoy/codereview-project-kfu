<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Редактирование код-ревью: ${review.title}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Редактирование код-ревью</h1>
        <p class="page-subtitle">Проект: <c:out value="${project.name}" /></p>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/review?id=${review.id}" 
       style="color: #2563eb; text-decoration: none;">
        Назад к ревью
    </a>
</p>

<c:if test="${param.error != null}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>

<div class="section">
    <form action="${pageContext.request.contextPath}/reviews/edit" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${review.id}" />
        
        <div class="form-group">
            <label class="form-label">Заголовок:</label>
            <input type="text" 
                   name="title" 
                   class="form-input" 
                   value="<c:out value='${review.title}' />" 
                   required 
                   maxlength="255" />
        </div>
        
        <div class="form-group">
            <label class="form-label">Содержимое:</label>
            <textarea name="content" 
                      class="form-input" 
                      rows="10"><c:out value="${review.content != null ? review.content : ''}" /></textarea>
        </div>
        
        <div style="display: flex; gap: 12px;">
            <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            <a href="${pageContext.request.contextPath}/review?id=${review.id}" 
               class="btn btn-secondary">
                Отмена
            </a>
        </div>
    </form>
</div>

<div class="section" style="border-left: 4px solid #dc2626;">
    <div class="section-header" style="color: #dc2626;">Опасная зона</div>
    <p style="color: #6b7280; font-size: 14px; margin-bottom: 16px;">
        Удаление ревью является необратимым действием.
    </p>
    <form action="${pageContext.request.contextPath}/reviews/delete" 
          method="post" 
          onsubmit="return confirm('Вы уверены, что хотите удалить это код-ревью? Это действие нельзя отменить!');">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${review.id}" />
        <button type="submit" class="btn btn-danger">Удалить код-ревью</button>
    </form>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
