#!/usr/bin/env bash
# Скрипт для упаковки проекта под Code Review Platform.
# Использует git для учёта .gitignore, если доступен.
# Иначе упаковывает все файлы рекурсивно (кроме очевидного мусора).

set -e

OUTFILE="$1"

if [ -z "$OUTFILE" ]; then
  echo "Использование: $0 output.zip"
  echo "Пример: $0 my-project.zip"
  exit 1
fi

rm -f "$OUTFILE"

if command -v git >/dev/null 2>&1 && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Обнаружен git-репозиторий, используем git ls-files (учитываем .gitignore)..."
  
  FILES=$(git ls-files --cached --others --exclude-standard | grep -v "^crp-upload\.sh$")
  
  if [ -z "$FILES" ]; then
    echo "Не найдено файлов для упаковки."
    exit 0
  fi
  
  printf "%s\n" $FILES | zip -q -@ "$OUTFILE"
  
else
  echo "Git не найден или не в репозитории, упаковываем все файлы рекурсивно..."
  
  zip -q -r "$OUTFILE" . \
    -x "crp-upload.sh" \
    -x "*.git*" \
    -x "*node_modules/*" \
    -x "*target/*" \
    -x "*build/*" \
    -x "*dist/*" \
    -x "*.class" \
    -x "*.jar" \
    -x "*.war" \
    -x "*__pycache__/*" \
    -x "*.pyc" \
    -x "*venv/*" \
    -x "*env/*" \
    -x "*.DS_Store" \
    -x "*Thumbs.db"
fi

echo "Готово! Проект упакован в: $OUTFILE"
echo "Теперь загрузите этот файл на сайт в форме импорта проекта."

