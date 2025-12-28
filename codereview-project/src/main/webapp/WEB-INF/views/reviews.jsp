<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Ревью проекта" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title"><c:out value="${project.name}" /></h1>
        <p class="page-subtitle"><c:out value="${project.description}" /></p>
    </div>
</div>

<div class="page-actions mb-2">
    <a href="${pageContext.request.contextPath}/projects" 
       style="color: #2563eb; text-decoration: none;">
        Назад к списку проектов
    </a>
    <a href="${pageContext.request.contextPath}/reviews/new?projectId=${project.id}" 
       class="btn btn-primary">
        Создать ревью
    </a>
</div>

<c:if test="${not empty param.error}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>

<c:if test="${empty reviews}">
    <div class="section text-center">
        <p class="text-muted">Для этого проекта пока нет ревью.</p>
        <a href="${pageContext.request.contextPath}/reviews/new?projectId=${project.id}" 
           class="btn btn-primary mt-2">
            Создать первое ревью
        </a>
    </div>
</c:if>

<c:if test="${not empty reviews}">
    <div class="table-wrapper">
        <table class="table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Заголовок</th>
                    <th>Автор</th>
                    <th>Статус</th>
                    <th>Создано</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="r" items="${reviews}">
                    <tr>
                        <td><c:out value="${r.id}" /></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/review?id=${r.id}"
                               style="color: #2563eb; text-decoration: none; font-weight: 500;">
                                <c:out value="${r.title}" />
                            </a>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty authorNames[r.authorId]}">
                                    <c:out value="${authorNames[r.authorId]}" />
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${r.authorId}" />
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:set var="statusClass" value="badge-wip" />
                            <c:if test="${r.status.name() == 'ACTIVE'}">
                                <c:set var="statusClass" value="badge-active" />
                            </c:if>
                            <c:if test="${r.status.name() == 'CHANGES_REQUIRED'}">
                                <c:set var="statusClass" value="badge-changes" />
                            </c:if>
                            <c:if test="${r.status.name() == 'APPROVED'}">
                                <c:set var="statusClass" value="badge-approved" />
                            </c:if>
                            <c:if test="${r.status.name() == 'CLOSED'}">
                                <c:set var="statusClass" value="badge-closed" />
                            </c:if>
                            <c:if test="${r.status.name() == 'ABANDONED'}">
                                <c:set var="statusClass" value="badge-abandoned" />
                            </c:if>
                            
                            <span class="badge ${statusClass}">
                                <c:out value="${r.status.label}" />
                            </span>
                        </td>
                        <td><c:out value="${r.createdAt}" /></td>
                        <td>
                            <div class="table-actions">
                                <a href="${pageContext.request.contextPath}/review?id=${r.id}">
                                    Открыть
                                </a>
                                <c:if test="${sessionScope.user != null && sessionScope.user.id == r.authorId}">
                                    <a href="${pageContext.request.contextPath}/reviews/edit?id=${r.id}">
                                        Редактировать
                                    </a>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
