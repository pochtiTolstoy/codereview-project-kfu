#!/bin/bash

# Путь до Tomcat
# TOMCAT_DIR="/opt/tomcat/apache-tomcat-10.1.48"
TOMCAT_DIR="/opt/tomcat"
WAR_NAME="codereview-project"
WAR_FILE="target/$WAR_NAME.war"

echo "Остановка Tomcat..."
sudo "$TOMCAT_DIR/bin/shutdown.sh"
sleep 2

echo "Удаление старого WAR и распакованной папки..."
sudo rm -rf "$TOMCAT_DIR/webapps/$WAR_NAME" "$TOMCAT_DIR/webapps/$WAR_NAME.war"

echo "Копирование нового .war файла..."
sudo cp "$WAR_FILE" "$TOMCAT_DIR/webapps/"

echo "Запуск Tomcat..."
sudo "$TOMCAT_DIR/bin/startup.sh"

echo "Готово! Открой http://localhost:8080/$WAR_NAME/"
