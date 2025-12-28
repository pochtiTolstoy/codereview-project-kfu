<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8" />
    <title><c:out value="${pageTitle != null ? pageTitle : 'Code Review Platform'}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme-dark.css" />
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/res/styles/tokyo-night-dark.css" />
    <script src="${pageContext.request.contextPath}/res/highlight.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            if (window.hljs) {
                document.querySelectorAll('pre code, .code-code').forEach(function (el) {
                    hljs.highlightElement(el);
                });
            }
        });
    </script>
</head>
<body>
<header class="site-header">
    <div class="site-header-inner">
        <a href="${pageContext.request.contextPath}/projects" class="site-logo">Code Review Platform</a>
        <nav class="site-nav">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <span style="color: #d3d9e3;"><c:out value="${sessionScope.user.username}" /> (<c:out value="${sessionScope.user.role}" />)</span>
                    <c:if test="${sessionScope.user.role == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/admin">Админ</a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/help">Помощь</a>
                    <a href="${pageContext.request.contextPath}/logout">Выход</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login">Вход</a>
                    <a href="${pageContext.request.contextPath}/register">Регистрация</a>
                    <a href="${pageContext.request.contextPath}/help">Помощь</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>

<div class="main-container">
