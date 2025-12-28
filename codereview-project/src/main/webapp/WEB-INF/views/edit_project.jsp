<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Редактирование проекта: ${project.name}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Редактирование проекта</h1>
        <p class="page-subtitle"><c:out value="${project.name}" /></p>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/project?id=${project.id}" 
       style="color: #2563eb; text-decoration: none;">
        Назад к проекту
    </a>
</p>

<c:if test="${param.error != null}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>

<div class="section">
    <form action="${pageContext.request.contextPath}/projects/edit" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${project.id}" />
        
        <div class="form-group">
            <label class="form-label">Название проекта:</label>
            <input type="text" 
                   name="name" 
                   class="form-input" 
                   value="${project.name}" 
                   required 
                   maxlength="255" />
        </div>
        
        <div class="form-group">
            <label class="form-label">Описание:</label>
            <textarea name="description" 
                      class="form-input" 
                      rows="5"><c:out value="${project.description != null ? project.description : ''}" /></textarea>
        </div>
        
        <div style="display: flex; gap: 12px;">
            <button type="submit" class="btn btn-primary">Сохранить изменения</button>
            <a href="${pageContext.request.contextPath}/project?id=${project.id}" 
               class="btn btn-secondary">
                Отмена
            </a>
        </div>
    </form>
</div>

<div class="section" style="border-left: 4px solid #dc2626;">
    <div class="section-header" style="color: #dc2626;">Опасная зона</div>
    <p style="color: #6b7280; font-size: 14px; margin-bottom: 16px;">
        Удаление проекта является необратимым действием. Все файлы и код-ревью будут удалены.
    </p>
    <form action="${pageContext.request.contextPath}/projects/delete" 
          method="post" 
          onsubmit="return confirm('Вы уверены, что хотите удалить этот проект? Это действие нельзя отменить!');">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="id" value="${project.id}" />
        <button type="submit" class="btn btn-danger">Удалить проект</button>
    </form>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
