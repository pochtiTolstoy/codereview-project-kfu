<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Проект: ${project.name}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title"><c:out value="${project.name}" /></h1>
        <p class="page-subtitle"><c:out value="${project.description != null ? project.description : 'Описание отсутствует'}" /></p>
    </div>
    <div style="display: flex; gap: 8px;">
        <a href="${pageContext.request.contextPath}/reviews/new?projectId=${project.id}" 
           class="btn btn-primary">
            Создать ревью
        </a>
        <a href="${pageContext.request.contextPath}/projects/download?id=${project.id}" 
           class="btn btn-secondary">
            Скачать ZIP
        </a>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/projects" 
       style="color: #2563eb; text-decoration: none;">
        Назад к списку проектов
    </a>
</p>

<c:if test="${param.success != null}">
    <div class="alert alert-success mb-2">
        <c:out value="${param.success}" />
    </div>
</c:if>

<c:if test="${param.error != null}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>


<div class="section">
    <div class="section-header">Участники проекта</div>
    <c:if test="${empty members}">
        <p class="text-muted">Нет участников.</p>
    </c:if>
    <c:if test="${not empty members}">
        <div style="display: flex; flex-wrap: wrap; gap: 12px;">
            <c:forEach var="member" items="${members}">
                <div class="chip" style="padding: 8px 12px; font-size: 14px;">
                    <strong><c:out value="${member.username}" /></strong>
                    <c:if test="${member.id == project.ownerId}">
                        <span class="badge badge-approved" style="margin-left: 6px; font-size: 10px;">Владелец</span>
                    </c:if>
                </div>
            </c:forEach>
        </div>
    </c:if>
</div>


<c:if test="${canEditProject}">
    <div class="section">
        <div class="section-header">Быстрый импорт из ZIP-архива</div>
        <p class="text-muted" style="margin-bottom: 12px; font-size: 14px;">
            Загрузите ZIP-архив с файлами проекта. 
            Используйте <a href="${pageContext.request.contextPath}/resources/crp-upload.sh" download>скрипт crp-upload.sh</a> 
            для автоматической упаковки git-проекта.
        </p>
        <form action="${pageContext.request.contextPath}/project/import" 
              method="post" 
              enctype="multipart/form-data"
              style="display: flex; align-items: center; gap: 12px; justify-content: space-between;">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
            <input type="hidden" name="projectId" value="${project.id}" />
            <input type="file" 
                   name="zipFile" 
                   accept=".zip" 
                   required 
                   style="font-size: 14px; margin-right: auto;" />
            <button type="submit" class="btn btn-primary">
                Импортировать файлы
            </button>
        </form>
    </div>
</c:if>


<div class="section">
    <div class="section-header">Файлы проекта</div>
    
    <c:if test="${empty projectFiles}">
        <p class="text-muted">В проекте пока нет файлов.</p>
    </c:if>

    <c:if test="${not empty projectFiles}">
        <div class="table-wrapper">
            <table class="table">
                <thead>
                    <tr>
                        <th>Имя файла</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="file" items="${projectFiles}">
                        <tr>
                            <td>
                                <a href="${pageContext.request.contextPath}/project/file/view?id=${file.id}"
                                   style="color: #2563eb; text-decoration: none; font-family: monospace;">
                                    <c:out value="${file.filename}" />
                                </a>
                            </td>
                            <td>
                                <div class="table-actions">
                                    <a href="${pageContext.request.contextPath}/project/file/view?id=${file.id}">
                                        Просмотр
                                    </a>
                                    <a href="${pageContext.request.contextPath}/reviews/new?projectId=${project.id}&fileName=${fn:escapeXml(file.filename)}">
                                        Создать ревью
                                    </a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        
        <p class="text-muted" style="margin-top: 12px; font-size: 13px;">
            Изменять/добавлять файлы можно только через код-ревью.
        </p>
    </c:if>
</div>


<div class="section">
    <div class="section-header">Действия</div>
    <div style="display: flex; flex-wrap: wrap; gap: 12px;">
        <a href="${pageContext.request.contextPath}/reviews?projectId=${project.id}" 
           class="btn btn-primary">
            Просмотреть ревью
        </a>
        <a href="${pageContext.request.contextPath}/reviews/new?projectId=${project.id}" 
           class="btn btn-secondary">
            Создать ревью
        </a>
        <c:if test="${canEditProject}">
            <a href="${pageContext.request.contextPath}/projects/edit?id=${project.id}" 
               class="btn btn-secondary">
                Редактировать проект
            </a>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
