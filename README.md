# Code Review Platform

## Требования

* **Java 11**
* **Maven 3.8+**
* **Apache Tomcat 10.1.x** (важно: Jakarta Servlet 6)
* **PostgreSQL 14+** (или любой актуальный)
* Linux/macOS (скрипты `.sh`). На Windows можно запускать команды вручную.

## Структура репозитория

* `src/` - исходники (Servlets / Services / DAO / JSP)
* `init.sql` - создание схемы БД
* `seed.sql` - демо-данные (пользователи + демо-проект/ревью)
* `reset-bd.sh` - пересоздание БД и применение init/seed
* `run-fast.sh` - `rebuild.sh` + `redeploy.sh` + `run_app.sh`
* `rebuild.sh` - сборка проекта (Maven)
* `redeploy.sh` - деплой WAR в Tomcat (копирует в webapps и перезапускает)

## Быстрый старт (рекомендуемый порядок)
Запустить 'reset-bd.sh' + 'run_fast.sh', в случае если скрипты не сработали
можно перейти к ручному запуску

### 1) Поднять PostgreSQL и создать БД

```bash
sudo systemctl status postgresql
```

```bash
./reset-bd.sh
```

### 2) Собрать WAR

```bash
mvn clean package
```

* `target/codereview-project.war`

### 3) Задеплоить в Tomcat

Вариант A (скриптом, если настроен путь к Tomcat как в `redeploy.sh`):

```bash
./redeploy.sh
```

Вариант B (вручную):
Пример:

```bash
sudo cp target/codereview-project.war /opt/tomcat/webapps/
sudo /opt/tomcat/bin/shutdown.sh
sudo /opt/tomcat/bin/startup.sh
```

### 4) Открыть приложение

* `http://localhost:8080/codereview-project/`

## Демо-аккаунты (seed.sql)

После `./reset-bd.sh` доступны пользователи:

* `admin / admin123` (роль ADMIN)
* `userA / pass123`
* `userB / pass123`
* `userC / pass123`
* `userD / pass123`
* `userE / pass123`
* `userF / pass123`

## Конфигурация БД

Приложение подключается по JDBC к:

* база: `codereview-db`
* пользователь: `reviewuser`
* хост: `localhost`
* порт: `5432`
