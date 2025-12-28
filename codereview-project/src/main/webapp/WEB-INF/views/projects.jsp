<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Мои проекты" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<c:set var="currentUser" value="${sessionScope.user}" />

<div class="page-header">
    <div>
        <h1 class="page-title">Мои проекты</h1>
        <p class="page-subtitle">
            Онлайн пользователей: <c:out value="${applicationScope.onlineUsers}" />
        </p>
    </div>
    <a href="${pageContext.request.contextPath}/projects/new" class="btn btn-primary">
        Создать проект
    </a>
</div>

<c:if test="${empty projects}">
    <div class="section text-center">
        <p class="text-muted">У вас пока нет проектов.</p>
        <a href="${pageContext.request.contextPath}/projects/new" class="btn btn-primary mt-2">
            Создать первый проект
        </a>
    </div>
</c:if>

<c:if test="${not empty projects}">
    <div class="card-list" style="grid-template-columns: 1fr;">
        <c:forEach var="project" items="${projects}">
            <div class="card">
                <h3>
                    <a href="${pageContext.request.contextPath}/project?id=${project.id}">
                        <c:out value="${project.name}" />
                    </a>
                </h3>
                
                <p class="mb-1">
                    <c:out value="${project.description}" />
                </p>
                
                <div class="card-actions">
                    <a href="${pageContext.request.contextPath}/project?id=${project.id}" 
                       class="btn btn-primary btn-small">
                        Открыть
                    </a>
                    <a href="${pageContext.request.contextPath}/reviews?projectId=${project.id}" 
                       class="btn btn-secondary btn-small">
                        Код-ревью
                    </a>
                    
                    <c:if test="${currentUser != null 
                                 && (currentUser.id == project.ownerId 
                                     || currentUser.role == 'ADMIN')}">
                        <a href="${pageContext.request.contextPath}/admin/project-members?projectId=${project.id}" 
                           class="btn btn-secondary btn-small">
                            Участники
                        </a>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
