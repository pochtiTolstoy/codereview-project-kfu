<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Участники проекта" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
<c:set var="currentUser" value="${sessionScope.user}" />

<div class="page-header">
    <div>
        <h1 class="page-title">Участники проекта</h1>
        <p class="page-subtitle"><c:out value="${project.name}" /></p>
    </div>
</div>

<p class="mb-2">
    <c:choose>
        <c:when test="${currentUser != null && currentUser.role == 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/admin" 
               style="color: #2563eb; text-decoration: none;">
                Назад к админ-панели
            </a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/projects" 
               style="color: #2563eb; text-decoration: none;">
                Назад к моим проектам
            </a>
        </c:otherwise>
    </c:choose>
</p>

<c:if test="${not empty param.error}">
    <div class="alert alert-error mb-2">
        <c:out value="${param.error}" />
    </div>
</c:if>


<div class="section">
    <div class="section-header">Текущие участники</div>
    
    <c:if test="${empty members}">
        <p class="text-muted">Участников нет.</p>
    </c:if>

    <c:if test="${not empty members}">
        <div class="table-wrapper">
            <table class="table">
                <thead>
                    <tr>
                        <th>Пользователь</th>
                        <th>Email</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${members}">
                        <tr>
                            <td><strong><c:out value="${u.username}" /></strong></td>
                            <td><c:out value="${u.email}" /></td>
                            <td>
                                <div class="table-actions">
                                    <form action="${pageContext.request.contextPath}/admin/project-members" 
                                          method="post" 
                                          style="display:inline;"
                                          onsubmit="return confirm('Удалить пользователя ${u.username} из проекта?');">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                        <input type="hidden" name="action" value="remove"/>
                                        <input type="hidden" name="projectId" value="${project.id}"/>
                                        <input type="hidden" name="userId" value="${u.id}"/>
                                        <button type="submit" class="btn btn-danger btn-small">Удалить</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:if>
</div>


<div class="section">
    <div class="section-header">Добавить участника</div>
    <form action="${pageContext.request.contextPath}/admin/project-members" 
          method="post"
          style="display: flex; align-items: flex-end; gap: 12px;">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="action" value="add"/>
        <input type="hidden" name="projectId" value="${project.id}"/>

        <div class="form-group mb-0">
            <label class="form-label">Выберите пользователя:</label>
            <select name="userId" class="form-input" required style="min-width: 250px;">
                <option value="">-- Выберите --</option>
                <c:forEach var="u" items="${users}">
                    <option value="${u.id}">
                        <c:out value="${u.username}" /> (<c:out value="${u.email}" />)
                    </option>
                </c:forEach>
            </select>
        </div>

        <button type="submit" class="btn btn-primary">Добавить</button>
    </form>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
