<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="pageTitle" value="Код-ревью: ${review.title}" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<style>
    /* Расширяем контейнер для страницы ревью */
    .main-container {
        max-width: none !important;
        width: calc(100% - 32px);
    }
</style>

<script>
    
    document.addEventListener('DOMContentLoaded', function () {
        document.body.dataset.reviewId = '${review.id}';
    });
</script>
    <script>
        
        document.addEventListener('DOMContentLoaded', function () {
            const globalReviewId = document.body.dataset.reviewId || '';
            const forms = document.querySelectorAll('form.js-ajax-comment');
            forms.forEach(function (form) {
                let hid = form.querySelector('input[name="reviewId"]');
                if (!hid) {
                    hid = document.createElement('input');
                    hid.type = 'hidden';
                    hid.name = 'reviewId';
                    form.appendChild(hid);
                }
                hid.value = globalReviewId;
            });
            forms.forEach(function (form) {
                form.addEventListener('submit', function (e) {
                    e.preventDefault();
                    const formData = new FormData(form);
                    const rid = document.body.dataset.reviewId;
                    if (rid) {
                        formData.set('reviewId', rid);
                    }
                    const container = form.closest('.comment-thread') || document.querySelector('.comment-thread-list');

                    
                    const params = new URLSearchParams();
                    for (const [key, value] of formData.entries()) {
                        params.append(key, value);
                    }
                    
                    fetch(form.action, {
                        method: 'POST',
                        headers: { 
                            'Accept': 'application/json',
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        body: params.toString()
                    }).then(async resp => {
                        if (!resp.ok) {
                            let msg = 'Ошибка сети';
                            try {
                                const err = await resp.json();
                                if (err && err.error) msg = err.error;
                            } catch (_) {
                                msg = resp.statusText || msg;
                            }
                            throw new Error(msg);
                        }
                        return resp.json();
                    }).then(data => {
                        
                        
                        form.reset();
                        const successMsg = document.createElement('div');
                        successMsg.style.cssText = 'color: green; padding: 8px; margin: 8px 0; background: #e6ffed; border: 1px solid #22863a; border-radius: 4px;';
                        successMsg.textContent = '✓ Комментарий добавлен! Обновление страницы...';
                        form.parentNode.insertBefore(successMsg, form);
                        setTimeout(() => window.location.reload(), 400);
                    }).catch(err => {
                        alert('Не удалось отправить комментарий: ' + err.message);
                    });
                });
            });
        });
    </script>
    <script>
        
        

        
        document.addEventListener('click', function (event) {
            let targetEl = event.target;

            
            if (targetEl && targetEl.nodeType === Node.TEXT_NODE) {
                targetEl = targetEl.parentElement;
            }
            if (!targetEl || !targetEl.closest) {
                return;
            }

            
            const commentLink = targetEl.closest('a.comment-indicator-link');
            if (commentLink) {
                const hash = commentLink.getAttribute('href');
                if (hash && hash.startsWith('#')) {
                    const targetId = hash.substring(1);
                    const commentElement = document.getElementById(targetId);
                    if (commentElement) {
                        event.preventDefault();
                        commentElement.scrollIntoView({
                            block: 'center',
                            inline: 'nearest'
                        });
                        if (history.replaceState) {
                            history.replaceState(null, '', hash);
                        }
                        return;
                    }
                }
            }

            
            const link = targetEl.closest('a[data-scroll-to-line="true"]');
            if (!link) {
                return;
            }

            const hash = link.getAttribute('href');
            if (!hash || !hash.startsWith('#')) {
                return;
            }

            const targetId = hash.substring(1);
            const codeLine = document.getElementById(targetId);
            if (!codeLine) {
                return;
            }

            event.preventDefault();

            
            codeLine.scrollIntoView({
                block: 'center',
                inline: 'nearest'
            });

            
            if (history.replaceState) {
                history.replaceState(null, '', hash);
            }

            
            document.querySelectorAll('.code-line-focused').forEach(function (el) {
                el.classList.remove('code-line-focused');
            });

            
            codeLine.classList.add('code-line-focused');

            
            setTimeout(function () {
                codeLine.classList.remove('code-line-focused');
            }, 1500);
        });

        
        document.addEventListener('DOMContentLoaded', function () {
            let unresolvedThreads = [];
            let currentIndex = -1;

            function collectUnresolvedThreads() {
                unresolvedThreads = Array.from(
                    document.querySelectorAll('.comment-thread[data-resolved="false"]')
                );
                
                unresolvedThreads.sort(function (a, b) {
                    const aTop = a.getBoundingClientRect().top + window.pageYOffset;
                    const bTop = b.getBoundingClientRect().top + window.pageYOffset;
                    return aTop - bTop;
                });
                currentIndex = -1;
            }

            function scrollToThread(thread) {
                if (!thread) return;
                const rect = thread.getBoundingClientRect();
                const absoluteTop = rect.top + window.pageYOffset;
                const offset = absoluteTop - (window.innerHeight / 2) + (rect.height / 2);
                window.scrollTo({
                    top: offset,
                    behavior: 'auto'
                });
                
                document.querySelectorAll('.comment-thread-focused').forEach(function (el) {
                    el.classList.remove('comment-thread-focused');
                });
                thread.classList.add('comment-thread-focused');
                setTimeout(function () {
                    thread.classList.remove('comment-thread-focused');
                }, 1500);
            }

            function goToNextUnresolved() {
                if (unresolvedThreads.length === 0) {
                    return;
                }
                const scrollY = window.pageYOffset;
                
                let idx = unresolvedThreads.findIndex(function (el) {
                    const top = el.getBoundingClientRect().top + window.pageYOffset;
                    return top > scrollY + 10; 
                });
                if (idx === -1) {
                    
                    idx = 0;
                }
                currentIndex = idx;
                scrollToThread(unresolvedThreads[currentIndex]);
            }

            function goToPrevUnresolved() {
                if (unresolvedThreads.length === 0) {
                    return;
                }
                const scrollY = window.pageYOffset;
                
                let idx = -1;
                for (let i = 0; i < unresolvedThreads.length; i++) {
                    const top = unresolvedThreads[i].getBoundingClientRect().top + window.pageYOffset;
                    if (top < scrollY - 10) {
                        idx = i;
                    } else {
                        break;
                    }
                }
                if (idx === -1) {
                    
                    idx = unresolvedThreads.length - 1;
                }
                currentIndex = idx;
                scrollToThread(unresolvedThreads[currentIndex]);
            }

            
            const nextBtn = document.getElementById('next-unresolved-comments');
            const prevBtn = document.getElementById('prev-unresolved-comments');
            if (nextBtn || prevBtn) {
                collectUnresolvedThreads();
                if (nextBtn) {
                    nextBtn.addEventListener('click', function () {
                        goToNextUnresolved();
                    });
                }
                if (prevBtn) {
                    prevBtn.addEventListener('click', function () {
                        goToPrevUnresolved();
                    });
                }
            }

            
            document.addEventListener('keydown', function (e) {
                if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                    return; 
                }
                if (e.key === 'j') {
                    goToNextUnresolved();
                } else if (e.key === 'k') {
                    goToPrevUnresolved();
                }
            });
        });
    </script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
</head>
<body>
    <c:set var="reviewStatusName" value="${review.status.name()}" />
    <c:set var="effectiveSelectedRevision"
           value="${not empty selectedRevision ? selectedRevision : (not empty currentRevision ? currentRevision : review.currentRevisionNumber)}" />
    <c:set var="effectiveBaseRevision"
           value="${selectedBaseRevision != null ? selectedBaseRevision : 0}" />
    <c:set var="effectiveBaseLabel"
           value="${not empty baseRevisionLabel ? baseRevisionLabel : 'Базовый код проекта'}" />
    <c:set var="latestRevisionFilesList"
           value="${not empty currentRevisionFiles ? currentRevisionFiles : reviewFiles}" />
    <c:set var="canEditCurrentRevision"
           value="${canEditCurrentRevision == null ? true : canEditCurrentRevision}" />
    
    <div class="page-header">
        <div>
            <h1 class="page-title"><c:out value="${review.title}" /></h1>
            <div style="display: flex; align-items: center; gap: 12px; margin-top: 8px;">
                <c:set var="statusClass" value="badge-wip" />
                <c:if test="${reviewStatusName == 'ACTIVE'}">
                    <c:set var="statusClass" value="badge-active" />
                </c:if>
                <c:if test="${reviewStatusName == 'CHANGES_REQUIRED'}">
                    <c:set var="statusClass" value="badge-changes" />
                </c:if>
                <c:if test="${reviewStatusName == 'APPROVED'}">
                    <c:set var="statusClass" value="badge-approved" />
                </c:if>
                <c:if test="${reviewStatusName == 'CLOSED'}">
                    <c:set var="statusClass" value="badge-closed" />
                </c:if>
                <c:if test="${reviewStatusName == 'ABANDONED'}">
                    <c:set var="statusClass" value="badge-abandoned" />
                </c:if>
                <span class="badge ${statusClass}">
                    <c:out value="${review.status.label}" />
                </span>
                <span class="badge badge-wip" style="background: #6366f1;">
                    Ревизия #<c:out value="${effectiveSelectedRevision}" />
                </span>
            </div>
        </div>
        <div style="display: flex; gap: 8px;">
            <button type="button" id="prev-unresolved-comments" class="btn btn-secondary btn-small">
                Пред. комм.
            </button>
            <button type="button" id="next-unresolved-comments" class="btn btn-secondary btn-small">
                След. комм.
            </button>
        </div>
    </div>

    <c:if test="${hasUnresolvedComments}">
        <div class="alert alert-error mb-2">
            Открытых комментариев: ${unresolvedCount}. Закройте их перед апрувом/мерджем.
        </div>
    </c:if>

    <c:if test="${isAuthor && not canEditCurrentRevision}">
        <div class="alert alert-error mb-2">
            Текущая ревизия #${review.currentRevisionNumber} уже опубликована. Создайте новую ревизию перед тем, как править файлы.
        </div>
    </c:if>

    <c:if test="${not empty revisions}">
        <div class="mb-2">
            <form method="get"
                  action="${pageContext.request.contextPath}/review"
                  class="revision-toolbar">
                <input type="hidden" name="id" value="${review.id}" />
                <div class="form-group mb-0">
                    <label class="form-label">Показать ревизию:</label>
                    <select name="rev" class="form-input">
                        <c:forEach var="revNumber" items="${revisions}">
                            <option value="${revNumber}"
                                    <c:if test="${revNumber == effectiveSelectedRevision}">selected</c:if>>
                                Rev #${revNumber}
                                <c:if test="${revNumber == review.currentRevisionNumber}">(текущая)</c:if>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group mb-0">
                    <label class="form-label">Сравнить с:</label>
                    <select name="baseRev" class="form-input">
                        <option value="0" <c:if test="${effectiveBaseRevision == 0}">selected</c:if>>
                            Мастером (project_files)
                        </option>
                        <c:forEach var="revNumber" items="${revisions}">
                            <c:if test="${revNumber < effectiveSelectedRevision}">
                                <option value="${revNumber}"
                                        <c:if test="${revNumber == effectiveBaseRevision}">selected</c:if>>
                                    Ревизия #${revNumber}
                                </option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Показать</button>
            </form>
        </div>
    </c:if>

    <div class="review-status-actions" id="review-status-actions">
        <c:if test="${not empty param.error}">
            <p class="status-error">Ошибка: <c:out value="${param.error}" /></p>
        </c:if>
        <c:if test="${param.info == 'revision-started'}">
            <p class="status-info">Создана новая ревизия. Прикрепите обновлённые файлы.</p>
        </c:if>

        <c:if test="${isAuthor && reviewStatusName == 'WIP'}">
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="ready" />
                <button type="submit" class="btn-status btn-primary">
                    Открыть ревью (ACTIVE)
                </button>
            </form>
        </c:if>

        <c:if test="${isReviewer && (reviewStatusName == 'ACTIVE' || reviewStatusName == 'APPROVED')}">
            <c:set var="approveDisabled" value="${hasUnresolvedComments || hasOwnUnresolvedThreads}" />
            <c:set var="approveTitle" value="" />
            <c:choose>
                <c:when test="${hasUnresolvedComments}">
                    <c:set var="approveTitle" value="Сперва закройте комментарии" />
                </c:when>
                <c:when test="${hasOwnUnresolvedThreads}">
                    <c:set var="approveTitle" value="Сначала закройте свои комментарии" />
                </c:when>
            </c:choose>
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="approve" />
                <button type="submit"
                        class="btn-status btn-approve"
                        <c:if test="${approveDisabled}">disabled title="${approveTitle}"</c:if>>
                    Поставить плюс
                </button>
            </form>
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="requestChanges" />
                <button type="submit" class="btn-status btn-request-changes">Запросить правки</button>
            </form>
        </c:if>

        <c:if test="${isAuthor && reviewStatusName == 'CHANGES_REQUIRED'}">
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="markUpdated" />
                <button type="submit"
                        class="btn-status btn-primary"
                        <c:if test="${hasUnresolvedComments}">disabled title="Закройте комментарии, затем отправляйте снова"</c:if>>
                    Я внёс правки
                </button>
            </form>
        </c:if>

        <c:if test="${isAuthor && !review.status.terminal}">
            <form method="post" action="${pageContext.request.contextPath}/review/revision">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <button type="submit" class="btn-status btn-secondary">
                    Новая ревизия (Patchset)
                </button>
            </form>
        </c:if>

        <c:if test="${(isAuthor || isProjectOwner || isAdmin) && reviewStatusName == 'APPROVED'}">
            <c:set var="mergeDisabled" value="${hasUnresolvedComments || !allReviewersApproved}" />
            <c:set var="mergeTitle" value="" />
            <c:choose>
                <c:when test="${hasUnresolvedComments}">
                    <c:set var="mergeTitle" value="Нельзя закрыть с открытыми тредами" />
                </c:when>
                <c:when test="${!allReviewersApproved}">
                    <c:set var="mergeTitle" value="Дождитесь плюсов от всех ревьюеров" />
                </c:when>
            </c:choose>
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="merge" />
                <button type="submit"
                        class="btn-status btn-close"
                        <c:if test="${mergeDisabled}">disabled title="${mergeTitle}"</c:if>>
                    Замержить
                </button>
            </form>
        </c:if>

        <c:if test="${(isAuthor || isProjectOwner || isAdmin) && !review.status.terminal}">
            <form method="post" action="${pageContext.request.contextPath}/review/status">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                <input type="hidden" name="reviewId" value="${review.id}" />
                <input type="hidden" name="action" value="abandon" />
                <button type="submit" class="btn-status btn-abandon">
                    Отменить ревью
                </button>
            </form>
        </c:if>
    </div>
    
    <div class="review-participants">
        <div class="participants-row">
            <span class="label">Проект:</span>
            <a href="${pageContext.request.contextPath}/project?id=${project.id}">
                <c:out value="${project.name}" />
            </a>
        </div>
        <div class="participants-row">
            <span class="label">Автор:</span>
            <span class="chip chip-author">
                <c:if test="${author != null}">
                    <c:out value="${author.username}" /> (<c:out value="${author.email}" />)
                </c:if>
                <c:if test="${author == null}">
                    Неизвестный
                </c:if>
            </span>
        </div>
        <div class="participants-row reviewers-row">
            <span class="label">Ревьюеры:</span>
            <c:choose>
                <c:when test="${empty reviewers}">
                    <span class="chip chip-muted">Не назначены</span>
                </c:when>
                <c:otherwise>
                    <c:forEach var="revUser" items="${reviewers}">
                        <span class="chip chip-reviewer">
                            <c:out value="${revUser.username}" />
                            <c:if test="${canManageReviewers}">
                                <form method="post"
                                      action="${pageContext.request.contextPath}/review/reviewers"
                                      class="chip-remove-form">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                    <input type="hidden" name="reviewId" value="${review.id}" />
                                    <input type="hidden" name="action" value="remove" />
                                    <input type="hidden" name="userId" value="${revUser.id}" />
                                    <button type="submit" class="chip-remove" title="Удалить ревьюера">
                                        &times;
                                    </button>
                                </form>
                            </c:if>
                        </span>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${canManageReviewers}">
            <div class="participants-row add-reviewer-row">
                <form method="post"
                      action="${pageContext.request.contextPath}/review/reviewers"
                      class="add-reviewer-form">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                    <input type="hidden" name="reviewId" value="${review.id}" />
                    <input type="hidden" name="action" value="add" />
                    <input type="text" name="user" placeholder="логин или email" />
                    <button type="submit" class="btn btn-secondary small">Добавить ревьюера</button>
                </form>
            </div>
        </c:if>
        <c:if test="${not empty param.reviewerError}">
            <div class="reviewer-flash reviewer-error">
                <c:out value="${param.reviewerError}" />
            </div>
        </c:if>
        <c:if test="${not empty param.reviewerInfo}">
            <div class="reviewer-flash reviewer-info">
                <c:out value="${param.reviewerInfo}" />
            </div>
        </c:if>
        <c:if test="${not empty reviewers}">
            <div class="review-votes">
                <div class="votes-header">
                    <span class="label">Code-Review:</span>
                    <c:choose>
                        <c:when test="${empty codeReviewVotes}">
                            <span class="chip chip-muted">голосов пока нет</span>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${hasBlockingCodeReview}">
                                <span class="vote-summary vote-summary-blocking">есть блокирующие голоса</span>
                            </c:if>
                            <c:if test="${!hasBlockingCodeReview && maxCodeReview >= 1}">
                                <span class="vote-summary vote-summary-ok">есть одобряющие голоса</span>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:forEach var="revUser" items="${reviewers}">
                    <c:set var="crValue" value="${codeReviewVotes[revUser.id]}" />
                    <div class="vote-row">
                        <span class="vote-user"><c:out value="${revUser.username}" /></span>
                        <span class="vote-value
                                <c:if test='${crValue != null && crValue >= 1}'> vote-value-plus</c:if>
                                <c:if test='${crValue != null && crValue < 0}'> vote-value-minus</c:if>
                            ">
                            <c:choose>
                                <c:when test="${crValue != null}">
                                    CR: <c:out value="${crValue}" />
                                </c:when>
                                <c:otherwise>
                                    нет голоса
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </c:forEach>
            </div>
        </c:if>
        <div class="participants-row">
            <span class="label">Статус:</span> <c:out value="${review.status.label}" />
        </div>
        <div class="participants-row">
            <span class="label">Создано:</span>
            <c:if test="${review.createdAt != null}">
                <fmt:formatDate value="${review.createdAt}" pattern="dd.MM.yyyy HH:mm" />
            </c:if>
            <c:if test="${review.createdAt == null}">
                Не указана
            </c:if>
        </div>
    </div>

    <div class="section">
        <div class="section-header">Содержимое</div>
        <div class="text-muted" style="white-space: pre-wrap;">
            <c:out value="${review.content != null ? review.content : 'Содержимое отсутствует'}" />
        </div>
    </div>

    
    <c:if test="${isAuthor || isProjectOwner}">
        <div class="section">
            <div class="section-header">Файлы проекта</div>

            <c:if test="${empty projectFiles}">
                <p class="text-muted">В этом проекте пока нет файлов.</p>
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
                            <c:forEach var="pf" items="${projectFiles}">
                                <tr>
                                    <td>
                                        <span style="font-family: monospace;"><c:out value="${pf.filename}" /></span>
                                    </td>
                                    <td>
                                        <div class="table-actions">
                                            <c:set var="alreadyInReview" value="false" />
                                            <c:forEach var="rf" items="${latestRevisionFilesList}">
                                                <c:if test="${rf.filename == pf.filename}">
                                                    <c:set var="alreadyInReview" value="true" />
                                                </c:if>
                                            </c:forEach>
                                            <c:choose>
                                                <c:when test="${alreadyInReview}">
                                                    <span class="text-muted">(уже в ревью)</span>
                                                    <c:if test="${isAuthor && canEditCurrentRevision}">
                                                        <c:url var="editExistingFileUrl" value="/reviews/file/new">
                                                            <c:param name="reviewId" value="${review.id}" />
                                                            <c:param name="fileName" value="${pf.filename}" />
                                                        </c:url>
                                                        <a href="${editExistingFileUrl}">
                                                            Редактировать
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${isAuthor && not canEditCurrentRevision}">
                                                        <span class="locked-hint">Создайте новую ревизию</span>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:if test="${canEditCurrentRevision}">
                                                        <c:url var="addFileUrl" value="/reviews/file/new">
                                                            <c:param name="reviewId" value="${review.id}" />
                                                            <c:param name="fileName" value="${pf.filename}" />
                                                        </c:url>
                                                        <a href="${addFileUrl}">
                                                            Добавить в ревью
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${not canEditCurrentRevision}">
                                                        <span class="locked-hint">Недоступно без новой ревизии</span>
                                                    </c:if>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>
    </c:if>

    
    <div>
        <h2>Код в ревью — ревизия #<c:out value="${effectiveSelectedRevision}" /></h2>

        <c:if test="${empty reviewFiles}">
            <p>К этому ревью пока не прикреплено файлов.</p>
        </c:if>

        <c:if test="${not empty reviewFiles}">
            <c:forEach var="reviewFile" items="${reviewFiles}">
                <c:set var="langClass" value="language-plaintext" />
                <c:if test="${fn:endsWith(reviewFile.filename, '.c') or fn:endsWith(reviewFile.filename, '.h')}">
                    <c:set var="langClass" value="language-c" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.cpp') or fn:endsWith(reviewFile.filename, '.hpp') or fn:endsWith(reviewFile.filename, '.cc') or fn:endsWith(reviewFile.filename, '.cxx')}">
                    <c:set var="langClass" value="language-cpp" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.java')}">
                    <c:set var="langClass" value="language-java" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.py')}">
                    <c:set var="langClass" value="language-python" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.js')}">
                    <c:set var="langClass" value="language-javascript" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.html') or fn:endsWith(reviewFile.filename, '.htm')}">
                    <c:set var="langClass" value="language-html" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.css')}">
                    <c:set var="langClass" value="language-css" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.sql')}">
                    <c:set var="langClass" value="language-sql" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.xml')}">
                    <c:set var="langClass" value="language-xml" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.json')}">
                    <c:set var="langClass" value="language-json" />
                </c:if>
                <c:if test="${fn:endsWith(reviewFile.filename, '.sh') or fn:endsWith(reviewFile.filename, '.bash')}">
                    <c:set var="langClass" value="language-bash" />
                </c:if>
                
                <div class="code-diff-file">
                    <div class="code-diff-file-header">
                        <h3><c:out value="${reviewFile.filename}" /></h3>
                        <c:if test="${isAuthor && canEditCurrentRevision}">
                            <c:url var="editReviewFileUrl" value="/reviews/file/new">
                                <c:param name="reviewId" value="${review.id}" />
                                <c:param name="fileName" value="${reviewFile.filename}" />
                            </c:url>
                            <a href="${editReviewFileUrl}"
                               class="btn-edit-file">
                                Редактировать файл
                            </a>
                        </c:if>
                        <c:if test="${isAuthor && not canEditCurrentRevision}">
                            <span class="locked-hint">Недоступно в опубликованной ревизии</span>
                        </c:if>
                    </div>
                    <c:set var="newline" value="\n" />
                    <c:set var="diffLines" value="${diffMap[reviewFile.id]}" />
                    <c:choose>
                        <c:when test="${not empty diffLines}">
                            
                            <div class="code-diff-panels">
                                <div class="code-panel code-panel-original">
                                    <div class="code-panel-title">${effectiveBaseLabel}</div>
                                    <div class="code-block">
                                        <table class="diff-table">
                                            <c:forEach var="d" items="${diffLines}">
                                                <tr class="<c:choose><c:when test="${d.type.name() == 'ADDED'}">diff-line-added</c:when><c:when test="${d.type.name() == 'REMOVED'}">diff-line-removed</c:when><c:otherwise>diff-line-same</c:otherwise></c:choose>">
                                                    <td class="line-num">
                                                        <c:if test="${d.leftLineNumber != null}">
                                                            <c:out value="${d.leftLineNumber}" />
                                                        </c:if>
                                                    </td>
                                                    <td class="code-cell">
                                                        <c:if test="${d.leftText != null}">
                                                            <span class="code-line" id="file-${reviewFile.id}-orig-line-${d.leftLineNumber}" data-line="${d.leftLineNumber}">
                                                                <code class="code-code ${langClass}"><c:out value="${d.leftText}" /></code>
                                                            </span>
                                                        </c:if>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </div>
                                </div>
                                <div class="code-panel code-panel-changed">
                                    <div class="code-panel-title">Ревизия #${effectiveSelectedRevision}</div>
                                    <div class="code-block">
                                        <table class="diff-table">
                                            <c:forEach var="d" items="${diffLines}">
                                                <tr class="<c:choose><c:when test="${d.type.name() == 'ADDED'}">diff-line-added</c:when><c:when test="${d.type.name() == 'REMOVED'}">diff-line-removed</c:when><c:otherwise>diff-line-same</c:otherwise></c:choose>">
                                                    <td class="line-num">
                                                        <c:if test="${d.rightLineNumber != null}">
                                                            <c:set var="fileCommentInfo" value="${commentInfoByFileAndLine[reviewFile.id]}" />
                                                            <div class="line-num-container">
                                                                <c:if test="${not empty fileCommentInfo}">
                                                                    <c:set var="lineInfo" value="${fileCommentInfo[d.rightLineNumber]}" />
                                                                    <c:if test="${lineInfo != null && lineInfo.hasAny && lineInfo.firstThreadId != null}">
                                                                        <a href="#comment-${lineInfo.firstThreadId}" class="comment-indicator-link">
                                                                            <span class="comment-indicator
                                                                                <c:if test='${lineInfo.hasUnresolved}'> comment-indicator-open</c:if>
                                                                            "></span>
                                                                        </a>
                                                                    </c:if>
                                                                </c:if>
                                                                <a href="#file-${reviewFile.id}-rev-line-${d.rightLineNumber}"
                                                                   data-scroll-to-line="true"
                                                                   class="line-link">
                                                                    <span class="line-number-text">${d.rightLineNumber}</span>
                                                                </a>
                                                            </div>
                                                        </c:if>
                                                    </td>
                                                    <td class="code-cell">
                                                        <c:if test="${d.rightText != null}">
                                                            <span class="code-line" id="file-${reviewFile.id}-rev-line-${d.rightLineNumber}" data-line="${d.rightLineNumber}">
                                                                <code class="code-code ${langClass}"><c:out value="${d.rightText}" /></code>
                                                            </span>
                                                        </c:if>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            
                            <div class="code-diff-panels">
                                <div class="code-panel code-panel-original">
                                    <div class="code-panel-title">${effectiveBaseLabel}</div>
                                    <c:set var="baseContentRaw" value="${baseContents[reviewFile.filename]}" />
                                    <c:choose>
                                        <c:when test="${baseContentRaw != null}">
                                            <c:set var="baseLines" value="${fn:split(baseContentRaw, newline)}" />
                                            <div class="code-block"><c:forEach var="line" items="${baseLines}" varStatus="st"><span id="file-${reviewFile.id}-orig-line-${st.index + 1}" class="code-line" data-line="${st.index + 1}"><span class="line-number">${st.index + 1}</span><code class="code-code ${langClass}"><c:out value="${line}" /></code></span>
</c:forEach></div>
                                        </c:when>
                                        <c:otherwise>
                                            <p><em>Это новый файл, в базовом проекте его ещё нет.</em></p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="code-panel code-panel-changed">
                                    <div class="code-panel-title">Ревизия #${effectiveSelectedRevision}</div>
                                    <c:set var="reviewContent" value="${reviewFile.content}" />
                                    <c:set var="reviewLines" value="${fn:split(reviewContent, newline)}" />
                                    <div class="code-block"><c:forEach var="line" items="${reviewLines}" varStatus="st"><span id="file-${reviewFile.id}-rev-line-${st.index + 1}" class="code-line" data-line="${st.index + 1}"><span class="line-number">${st.index + 1}</span><code class="code-code ${langClass}"><c:out value="${line}" /></code></span>
</c:forEach></div>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    
                    <div class="file-comments">
                        <h4>Комментарии к файлу <c:out value="${reviewFile.filename}" /></h4>
                        <c:set var="fileCommentMap" value="${commentThreadsByFile[reviewFile.id]}" />
                        <c:choose>
                            <c:when test="${not empty fileCommentMap}">
                                <c:forEach var="lineEntry" items="${fileCommentMap}">
                                    <c:set var="lineNumber" value="${lineEntry.key}" />
                                    <c:set var="lineThreads" value="${lineEntry.value}" />
                                    <div class="line-thread-block">
                                        <div class="line-thread-label">
                                            <c:choose>
                                                <c:when test="${lineNumber != null}">
                                                    <a href="#file-${reviewFile.id}-rev-line-${lineNumber}"
                                                       data-scroll-to-line="true">
                                                        Строка ${lineNumber}
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <em>(весь файл)</em>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <c:forEach var="thread" items="${lineThreads}">
                                            <c:set var="threadClass" value="comment-thread" />
                                            <c:if test="${thread.resolved}">
                                                <c:set var="threadClass" value="${threadClass} comment-thread-resolved" />
                                            </c:if>
                                            <c:if test="${thread.outdated}">
                                                <c:set var="threadClass" value="${threadClass} comment-thread-outdated" />
                                            </c:if>
                                            <div class="${threadClass}"
                                                 id="comment-${thread.root.id}"
                                                 data-root-id="${thread.root.id}"
                                                 data-resolved="${thread.resolved}"
                                                 data-line-number="${thread.root.lineNumber != null ? thread.root.lineNumber : ''}"
                                                 data-file-id="${reviewFile.id}"
                                                 data-revision="${thread.root.revisionNumber}">
                                                <div class="comment-root">
                                                    <div class="comment-meta">
                                                        <span>
                                                            <c:choose>
                                                                <c:when test="${not empty authorNames[thread.root.authorId]}">
                                                                    <c:out value="${authorNames[thread.root.authorId]}" />
                                                                </c:when>
                                                                <c:otherwise>
                                                                    Автор ID: <c:out value="${thread.root.authorId}" />
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                        <span> | Создано:
                                                            <fmt:formatDate value="${thread.root.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                                        </span>
                                                        <span class="badge-revision">Rev #${thread.root.revisionNumber}</span>
                                                        <c:if test="${thread.outdated}">
                                                            <span class="badge-outdated">Outdated</span>
                                                        </c:if>
                                                        <c:if test="${thread.resolved}">
                                                            <span class="badge-resolved">Resolved</span>
                                                        </c:if>
                                                    </div>
                                                    <div class="comment-text">
                                                        <c:out value="${thread.root.content}" />
                                                    </div>
                                                    <div class="comment-actions">
                                                        <c:if test="${currentUser != null
                                                                     && (currentUser.id == thread.root.authorId
                                                                         || currentUser.role == 'ADMIN')}">
                                                            <form action="${pageContext.request.contextPath}/reviews/comment/delete"
                                                                  method="post"
                                                                  onsubmit="return confirm('Удалить комментарий?');">
                                                                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                                                <input type="hidden" name="commentId" value="${thread.root.id}" />
                                                                <input type="hidden" name="reviewId" value="${review.id}" />
                                                                <button type="submit">Удалить</button>
                                                            </form>
                                                        </c:if>
                                                        <c:if test="${thread.canResolve}">
                                                            <form action="${pageContext.request.contextPath}/comments/resolve"
                                                                  method="post"
                                                                  style="display:inline-block; margin-left: 8px;">
                                                                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                                                <input type="hidden" name="reviewId" value="${review.id}" />
                                                                <input type="hidden" name="rootId" value="${thread.root.id}" />
                                                                <input type="hidden" name="action"
                                                                       value="${thread.resolved ? 'reopen' : 'resolve'}" />
                                                                <button type="submit" class="btn-link">
                                                                    <c:choose>
                                                                        <c:when test="${thread.resolved}">Reopen</c:when>
                                                                        <c:otherwise>Resolve</c:otherwise>
                                                                    </c:choose>
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                </div>
                                                <c:forEach var="reply" items="${thread.replies}">
                                                    <div class="comment-reply">
                                                        <div class="comment-meta">
                                                    <span>
                                                        <c:choose>
                                                            <c:when test="${not empty authorNames[reply.authorId]}">
                                                                <c:out value="${authorNames[reply.authorId]}" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                Автор ID: <c:out value="${reply.authorId}" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                            <span> | Создано:
                                                                <fmt:formatDate value="${reply.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                                            </span>
                                                        </div>
                                                        <div class="comment-text">
                                                            <c:out value="${reply.content}" />
                                                        </div>
                                                        <div class="comment-actions">
                                                            <c:if test="${currentUser != null
                                                                         && (currentUser.id == reply.authorId
                                                                             || currentUser.role == 'ADMIN')}">
                                                                <form action="${pageContext.request.contextPath}/reviews/comment/delete"
                                                                      method="post"
                                                                      onsubmit="return confirm('Удалить комментарий?');">
                                                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                                                    <input type="hidden" name="commentId" value="${reply.id}" />
                                                                    <input type="hidden" name="reviewId" value="${review.id}" />
                                                                    <button type="submit">Удалить</button>
                                                                </form>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                                <div class="comment-reply-form-wrapper">
                        <form action="${pageContext.request.contextPath}/reviews/comment" method="post" class="js-ajax-comment">
                                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                                        <input type="hidden" name="reviewId" value="${review.id}" />
                                                        <input type="hidden" name="reviewFileId"
                                                               value="${thread.root.reviewFileId != null ? thread.root.reviewFileId : ''}" />
                                                        <input type="hidden" name="lineNumber"
                                                               value="${thread.root.lineNumber != null ? thread.root.lineNumber : ''}" />
                                                        <input type="hidden" name="parentId" value="${thread.root.id}" />
                                                        <textarea name="content" rows="2" placeholder="Ответить..." required></textarea>
                                                        <button type="submit">Ответить</button>
                                                    </form>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <p class="no-comments text-muted" style="font-style: italic;">Пока нет комментариев к этому файлу.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    
                    <div class="add-line-comment">
                        <h4>Добавить комментарий к строке</h4>
                        <form action="${pageContext.request.contextPath}/reviews/comment" method="post" class="js-ajax-comment">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                            <input type="hidden" name="reviewId" value="${review.id}" />
                            <input type="hidden" name="reviewFileId" value="${reviewFile.id}" />
                            <input type="hidden" name="parentId" value="" />
                            <label>
                                Номер строки (опционально):
                                <input type="number" name="lineNumber" min="1" style="width: 80px; margin-left: 8px;" />
                            </label><br/><br/>
                            <label>
                                Текст комментария:<br/>
                            <textarea name="content" rows="3" required style="width: 100%; max-width: 600px;"></textarea>
                            </label><br/><br/>
                            <button type="submit" class="btn btn-primary">Добавить комментарий</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </c:if>

        <c:if test="${(isAuthor || isProjectOwner) && canEditCurrentRevision}">
            <p>
                <a href="${pageContext.request.contextPath}/reviews/file/new?reviewId=${review.id}">
                    Добавить новый файл (которого ещё нет в проекте)
                </a>
            </p>
        </c:if>
        <c:if test="${(isAuthor || isProjectOwner) && not canEditCurrentRevision}">
            <p class="locked-hint">Добавление новых файлов доступно после создания новой ревизии.</p>
        </c:if>
    </div>

    
    <c:if test="${not empty param.error}">
        <p style="color:red;">Ошибка: <c:out value="${param.error}"/></p>
    </c:if>

    
    <div class="section">
        <div class="section-header">Общие комментарии к ревью</div>

        <c:if test="${empty generalCommentThreads}">
            <p class="text-muted">Общих комментариев пока нет.</p>
        </c:if>

        <c:if test="${not empty generalCommentThreads}">
            <div class="comment-thread-list">
                <c:forEach var="thread" items="${generalCommentThreads}">
                    <c:set var="threadClass" value="comment-thread" />
                    <c:if test="${thread.resolved}">
                        <c:set var="threadClass" value="${threadClass} comment-thread-resolved" />
                    </c:if>
                    <c:if test="${thread.outdated}">
                        <c:set var="threadClass" value="${threadClass} comment-thread-outdated" />
                    </c:if>
                    <div class="${threadClass}"
                         id="comment-${thread.root.id}"
                         data-root-id="${thread.root.id}"
                         data-resolved="${thread.resolved}"
                         data-line-number=""
                         data-file-id=""
                         data-revision="${thread.root.revisionNumber}">
                        <div class="comment-root">
                            <div class="comment-meta">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty authorNames[thread.root.authorId]}">
                                            <c:out value="${authorNames[thread.root.authorId]}" />
                                        </c:when>
                                        <c:otherwise>
                                            Автор ID: <c:out value="${thread.root.authorId}" />
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                                <span> | Создано:
                                    <fmt:formatDate value="${thread.root.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                </span>
                                <span class="badge-revision">Rev #${thread.root.revisionNumber}</span>
                                <c:if test="${thread.outdated}">
                                    <span class="badge-outdated">Outdated</span>
                                </c:if>
                                <c:if test="${thread.resolved}">
                                    <span class="badge-resolved">Resolved</span>
                                </c:if>
                            </div>
                            <div class="comment-text">
                                <c:out value="${thread.root.content}" />
                            </div>
                            <div class="comment-actions">
                                <c:if test="${currentUser != null
                                             && (currentUser.id == thread.root.authorId
                                                 || currentUser.role == 'ADMIN')}">
                                    <form action="${pageContext.request.contextPath}/reviews/comment/delete"
                                          method="post"
                                          onsubmit="return confirm('Удалить комментарий?');">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                        <input type="hidden" name="commentId" value="${thread.root.id}" />
                                        <input type="hidden" name="reviewId" value="${review.id}" />
                                        <button type="submit">Удалить</button>
                                    </form>
                                </c:if>
                                            <c:if test="${thread.canResolve}">
                                    <form action="${pageContext.request.contextPath}/comments/resolve"
                                          method="post"
                                          style="display:inline-block; margin-left: 8px;">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                        <input type="hidden" name="reviewId" value="${review.id}" />
                                        <input type="hidden" name="rootId" value="${thread.root.id}" />
                                        <input type="hidden" name="action"
                                               value="${thread.resolved ? 'reopen' : 'resolve'}" />
                                        <button type="submit" class="btn-link">
                                            <c:choose>
                                                <c:when test="${thread.resolved}">Reopen</c:when>
                                                <c:otherwise>Resolve</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>

                        <c:forEach var="reply" items="${thread.replies}">
                            <div class="comment-reply">
                                <div class="comment-meta">
                                    <span>
                                        <c:choose>
                                            <c:when test="${not empty authorNames[reply.authorId]}">
                                                <c:out value="${authorNames[reply.authorId]}" />
                                            </c:when>
                                            <c:otherwise>
                                                Автор ID: <c:out value="${reply.authorId}" />
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span> | Создано:
                                        <fmt:formatDate value="${reply.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                    </span>
                                </div>
                                <div class="comment-text">
                                    <c:out value="${reply.content}" />
                                </div>
                                <div class="comment-actions">
                                    <c:if test="${currentUser != null
                                                 && (currentUser.id == reply.authorId
                                                     || currentUser.role == 'ADMIN')}">
                                        <form action="${pageContext.request.contextPath}/reviews/comment/delete"
                                              method="post"
                                              onsubmit="return confirm('Удалить комментарий?');">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                            <input type="hidden" name="commentId" value="${reply.id}" />
                                            <input type="hidden" name="reviewId" value="${review.id}" />
                                            <button type="submit">Удалить</button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>

                        <div class="comment-reply-form-wrapper">
                            <form action="${pageContext.request.contextPath}/reviews/comment" method="post" class="js-ajax-comment">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
                                <input type="hidden" name="reviewId" value="${review.id}" />
                                <input type="hidden" name="reviewFileId" value="" />
                                <input type="hidden" name="lineNumber" value="" />
                                <input type="hidden" name="parentId" value="${thread.root.id}" />
                                <textarea name="content" rows="2" placeholder="Ответить..." required></textarea>
                                <button type="submit">Ответить</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>
    </div>

    <c:if test="${not empty legacyCommentThreads}">
        <div class="legacy-comments">
            <h2>Комментарии к старым ревизиям</h2>
            <div class="comment-thread-list">
                <c:forEach var="thread" items="${legacyCommentThreads}">
                    <c:set var="threadClass" value="comment-thread comment-thread-outdated" />
                    <div class="${threadClass}"
                         id="comment-${thread.root.id}"
                         data-root-id="${thread.root.id}"
                         data-revision="${thread.root.revisionNumber}">
                        <div class="comment-root">
                            <div class="comment-meta">
                                <span>Файл:
                                    <c:out value="${empty thread.root.reviewFileName ? 'Удалённый файл' : thread.root.reviewFileName}" />
                                </span>
                                <c:if test="${thread.root.lineNumber != null}">
                                    <span> | Строка ${thread.root.lineNumber}</span>
                                </c:if>
                                <span> | Патчсет #${thread.root.revisionNumber}</span>
                                <span> |
                                    <c:choose>
                                        <c:when test="${not empty authorNames[thread.root.authorId]}">
                                            <c:out value="${authorNames[thread.root.authorId]}" />
                                        </c:when>
                                        <c:otherwise>
                                            Автор ID: <c:out value="${thread.root.authorId}" />
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                                <span> | Создано:
                                    <fmt:formatDate value="${thread.root.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                </span>
                                <span class="badge-outdated">Outdated</span>
                                <c:if test="${thread.resolved}">
                                    <span class="badge-resolved">Resolved</span>
                                </c:if>
                            </div>
                            <div class="comment-text">
                                <c:out value="${thread.root.content}" />
                            </div>
                            <div class="comment-actions">
                                <c:if test="${currentUser != null
                                             && (currentUser.id == thread.root.authorId
                                                 || currentUser.role == 'ADMIN')}">
                                    <form action="${pageContext.request.contextPath}/reviews/comment/delete"
                                          method="post"
                                          onsubmit="return confirm('Удалить комментарий?');">
                                        <input type="hidden" name="commentId" value="${thread.root.id}" />
                                        <input type="hidden" name="reviewId" value="${review.id}" />
                                        <button type="submit">Удалить</button>
                                    </form>
                                </c:if>
                                    <c:if test="${thread.canResolve}">
                                    <form action="${pageContext.request.contextPath}/comments/resolve"
                                          method="post"
                                          style="display:inline-block; margin-left: 8px;">
                                        <input type="hidden" name="reviewId" value="${review.id}" />
                                        <input type="hidden" name="rootId" value="${thread.root.id}" />
                                        <input type="hidden" name="action"
                                               value="${thread.resolved ? 'reopen' : 'resolve'}" />
                                        <button type="submit" class="btn-link">
                                            <c:choose>
                                                <c:when test="${thread.resolved}">Reopen</c:when>
                                                <c:otherwise>Resolve</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                        <c:forEach var="reply" items="${thread.replies}">
                            <div class="comment-reply">
                                <div class="comment-meta">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty authorNames[reply.authorId]}">
                                            <c:out value="${authorNames[reply.authorId]}" />
                                        </c:when>
                                        <c:otherwise>
                                            Автор ID: <c:out value="${reply.authorId}" />
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                                    <span> | Создано:
                                        <fmt:formatDate value="${reply.createdAt}" pattern="dd.MM.yyyy HH:mm" />
                                    </span>
                                </div>
                                <div class="comment-text">
                                    <c:out value="${reply.content}" />
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <hr/>

    
    <div class="section">
        <div class="section-header">Добавить комментарий</div>
        <form action="${pageContext.request.contextPath}/reviews/comment" method="post" class="js-ajax-comment">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}" />
            <input type="hidden" name="reviewId" value="${review.id}" />
            <input type="hidden" name="parentId" value="" />
            <textarea name="content" rows="4" required style="width: 100%;"></textarea><br/><br/>
            <button type="submit" class="btn btn-primary">Отправить комментарий</button>
        </form>
    </div>

    <div class="review-footer-links section section-plain">
        <c:if test="${currentUser.id == review.authorId}">
            <p>
                <a href="${pageContext.request.contextPath}/reviews/edit?id=${review.id}">
                    Редактировать код-ревью
                </a>
            </p>
        </c:if>
        <p>
            <a href="${pageContext.request.contextPath}/reviews?projectId=${project.id}">
                Вернуться к списку ревью проекта
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/project?id=${project.id}">
                Вернуться к проекту
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/projects">
                Вернуться к списку проектов
            </a>
        </p>
    </div>
<jsp:include page="/WEB-INF/views/includes/footer.jsp" />

