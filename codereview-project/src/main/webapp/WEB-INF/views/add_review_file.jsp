<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Добавить файл в ревью" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

    <h1>Добавить файл в ревью: <c:out value="${review.title}" /></h1>
    <p>Проект: <c:out value="${project.name}" /></p>
    <p>Текущая ревизия: #<c:out value="${review.currentRevisionNumber}" /></p>

    <c:if test="${not empty param.error}">
        <p style="color:red;">Ошибка: ${param.error}</p>
    </c:if>

    <c:if test="${editingExistingFile}">
        <p style="color:#2563eb;">
            Вы редактируете файл <strong>${fileName}</strong> в текущей ревизии.
            Изменения заменят содержимое файла в Patchset #${review.currentRevisionNumber}.
        </p>
        <p style="color:#b45309;">
            Если оставить содержимое пустым, файл будет помечен как удалённый в этой ревизии
            (и удалится при merge).
        </p>
    </c:if>

    <form id="review-file-form" action="${pageContext.request.contextPath}/reviews/file/new" method="post">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
        <input type="hidden" name="reviewId" value="${review.id}" />

        <label>Имя файла:<br/>
            <input type="text" name="fileName" size="60"
                   value="${empty fileName ? '' : fileName}" />
        </label><br/><br/>

        <label>Содержимое файла:<br/>
            <textarea name="fileContent" rows="12" cols="80"><c:out value="${empty fileContent ? '' : fileContent}" /></textarea>
        </label><br/><br/>

        <c:choose>
            <c:when test="${editingExistingFile}">
                <button type="submit">Сохранить изменения</button>
                <button type="button" id="btn-delete-file" style="margin-left: 8px; background:#fee2e2; border:1px solid #fca5a5;">
                    Пометить удалённым
                </button>
            </c:when>
            <c:otherwise>
                <button type="submit">Прикрепить файл</button>
            </c:otherwise>
        </c:choose>
    </form>

    <script>
        (function() {
            const delBtn = document.getElementById('btn-delete-file');
            const form = document.getElementById('review-file-form');
            if (delBtn && form) {
                delBtn.addEventListener('click', function() {
                    const area = form.querySelector('textarea[name="fileContent"]');
                    if (area) {
                        area.value = '';
                    }
                    form.submit();
                });
            }
        })();
    </script>

    <p>
        <a href="${pageContext.request.contextPath}/review?id=${review.id}">
            Назад к ревью
        </a>
    </p>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />

