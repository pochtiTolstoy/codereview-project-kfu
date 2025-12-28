<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="pageTitle" value="Файл проекта: ${file.filename}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<style>
    /* Расширяем контейнер для просмотра файла */
    .main-container {
        max-width: 1400px !important;
    }
</style>

<div class="page-header">
    <div>
        <h1 class="page-title"><c:out value="${file.filename}" /></h1>
        <p class="page-subtitle">Проект: <c:out value="${project.name}" /></p>
    </div>
</div>

<p class="mb-2">
    <a href="${pageContext.request.contextPath}/project?id=${project.id}" 
       style="color: #2563eb; text-decoration: none;">
        Назад к проекту
    </a>
</p>

<c:set var="langClass" value="language-plaintext" />
<c:if test="${fn:endsWith(file.filename, '.c') or fn:endsWith(file.filename, '.h')}">
    <c:set var="langClass" value="language-c" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.cpp') or fn:endsWith(file.filename, '.hpp') or fn:endsWith(file.filename, '.cc') or fn:endsWith(file.filename, '.cxx')}">
    <c:set var="langClass" value="language-cpp" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.java')}">
    <c:set var="langClass" value="language-java" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.py')}">
    <c:set var="langClass" value="language-python" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.js')}">
    <c:set var="langClass" value="language-javascript" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.html') or fn:endsWith(file.filename, '.htm')}">
    <c:set var="langClass" value="language-html" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.css')}">
    <c:set var="langClass" value="language-css" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.sql')}">
    <c:set var="langClass" value="language-sql" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.xml')}">
    <c:set var="langClass" value="language-xml" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.json')}">
    <c:set var="langClass" value="language-json" />
</c:if>
<c:if test="${fn:endsWith(file.filename, '.sh') or fn:endsWith(file.filename, '.bash')}">
    <c:set var="langClass" value="language-bash" />
</c:if>

<div class="section section-plain">
    <pre class="code-view"><code class="${langClass}"><c:out value="${file.content}" /></code></pre>
</div>

<p class="text-muted" style="font-size: 13px; margin-top: 16px;">
    Изменять/удалять файлы можно только через код-ревью.
</p>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
