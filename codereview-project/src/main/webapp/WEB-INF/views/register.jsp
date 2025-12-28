<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Регистрация" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />
</div> 

<div class="page-full-height">
    <div class="auth-wrapper">
        <div class="auth-card">
            <h1 class="auth-title">Регистрация</h1>
            <p class="auth-subtitle">
                Создайте аккаунт, чтобы создавать проекты и отправлять код на ревью.
            </p>

    <c:if test="${param.error == '1'}">
                <div class="alert alert-error">
                    Пользователь с таким именем уже существует
                </div>
    </c:if>

            <div id="reg-alert" class="alert alert-error" style="display: none;"></div>

            <form id="register-form" action="${pageContext.request.contextPath}/register" method="post" novalidate>
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <div class="form-group">
                    <label class="form-label" for="reg-username">Имя пользователя</label>
                    <input class="form-input"
                           type="text"
                           name="username"
                           id="reg-username"
                           required />
                </div>

                <div class="form-group">
                    <label class="form-label" for="reg-email">Email</label>
                    <input class="form-input"
                           type="email"
                           name="email"
                           id="reg-email"
                           required />
                </div>

                <div class="form-group">
                    <label class="form-label" for="reg-password">Пароль</label>
                    <input class="form-input"
                           type="password"
                           name="password"
                           id="reg-password"
                           required />
                </div>

                <div class="form-group">
                    <label class="form-label" for="reg-password-confirm">Повторите пароль</label>
                    <input class="form-input"
                           type="password"
                           id="reg-password-confirm"
                           required />
                </div>

                <button type="submit" class="btn btn-primary btn-full auth-submit-spaced">
                    Зарегистрироваться
                </button>
    </form>

            <div class="auth-footer">
                Уже есть аккаунт?
                <a href="${pageContext.request.contextPath}/login">Войти</a>
            </div>
        </div>
    </div>
</div>

<script>
    (function () {
        const form = document.getElementById('register-form');
        if (!form) return;

        const username = document.getElementById('reg-username');
        const email = document.getElementById('reg-email');
        const password = document.getElementById('reg-password');
        const passwordConfirm = document.getElementById('reg-password-confirm');
        const globalAlert = document.getElementById('reg-alert');

        function showAlert(message) {
            if (!globalAlert) return;
            if (message) {
                globalAlert.textContent = message;
                globalAlert.style.display = 'block';
            } else {
                globalAlert.style.display = 'none';
                globalAlert.textContent = '';
            }
        }

        function validate() {
            showAlert();

            const u = username.value.trim();
            const e = email.value.trim();
            const p = password.value;
            const pc = passwordConfirm.value;

            let message = '';

            if (!u) {
                message = 'Введите имя пользователя';
            } else if (!e) {
                message = 'Введите email';
            } else if (!p) {
                message = 'Введите пароль';
            } else if (p.length < 6) {
                message = 'Пароль должен быть не короче 6 символов';
            } else if (!pc) {
                message = 'Повторите пароль';
            } else if (p !== pc) {
                message = 'Пароли не совпадают';
            }

            showAlert(message);
            return !message;
        }

        form.addEventListener('submit', function (e) {
            if (!validate()) {
                e.preventDefault();
            }
        });
    })();
</script>

<div class="main-container"> 
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
