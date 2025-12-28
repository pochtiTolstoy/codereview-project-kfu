<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Вход" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
</div> 

<div class="page-full-height">
    <div class="auth-wrapper">
        <div class="auth-card">
            <h1 class="auth-title">Вход</h1>
            <p class="auth-subtitle">
                Войдите, чтобы продолжить работу с проектами и код-ревью.
            </p>

            <c:if test="${param.registered == '1'}">
                <div class="alert alert-success">
                    Регистрация успешна. Теперь войдите в аккаунт.
                </div>
            </c:if>
            <c:if test="${param.error == '1'}">
                <div class="alert alert-error">
                    Неверные логин или пароль. Попробуйте снова.
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <div class="form-group">
                    <label class="form-label" for="username">Имя пользователя</label>
                    <input class="form-input"
                           type="text"
                           id="username"
                           name="username"
                           required />
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Пароль</label>
                    <input class="form-input"
                           type="password"
                           id="password"
                           name="password"
                           required />
                </div>

                <button type="submit" class="btn btn-primary btn-full auth-submit-spaced">
                    Войти
                </button>
            </form>

            <div class="auth-footer">
                Нет аккаунта?
                <a href="${pageContext.request.contextPath}/register">Зарегистрироваться</a>
            </div>
        </div>
    </div>
</div>

<div class="main-container"> 
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
