#!/usr/bin/env bash

# abc-accounting DB 환경의 초기 구축을 위한 스크립트
# 사용법
# ./configure.sh PGHOST PGPASSWORD

# PostgreSQL 접속 정보
PGHOST=${1:-localhost}          # PostgreSQL 호스트 (기본값: localhost)
DB_PASSWORD=${2:-abc_accounting} # PostgreSQL 비밀번호 (기본값: abc_accounting)
PGUSER=${3:-abc_accounting}     # PostgreSQL 사용자 이름 (기본값: abc_accounting)
PGDATABASE=${4:-abc_accounting} # PostgreSQL 데이터베이스 이름 (기본값: abc_accounting)
PGPORT=${5:-5432}               # PostgreSQL 포트 (기본값: 5432)

# 환경변수 설정 (보안상 직접 패스워드 노출을 피하기 위해 사용 가능)
export PGPASSWORD="$DB_PASSWORD"

# 현재 디렉토리의 절대 경로 가져오기
CURRENT_DIR=$(pwd)

# init-scripts 디렉토리 내의 모든 SQL 파일 실행
if [ -d "$CURRENT_DIR/init-scripts" ]; then
  echo "Executing scripts in init-scripts directory..."
  find "$CURRENT_DIR/init-scripts" -type f -name "*.sql" | sort | while read -r SQL_FILE; do
    if echo "$SQL_FILE" | grep -qi "schema"; then
      echo "Executing schema script: $SQL_FILE..."
      PGPASSWORD="$DB_PASSWORD" psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" < "$SQL_FILE"
      if [ $? -ne 0 ]; then
        echo "Error occurred while executing schema script: $SQL_FILE. Exiting."
        exit 1
      fi
    elif echo "$SQL_FILE" | grep -qi "import"; then
      echo "Processing import script: $SQL_FILE..."

      cp "$SQL_FILE" "$SQL_FILE.bak"

      # /data/ 경로를 현재 디렉토리의 csv 디렉토리로 변경
      sed -i '' "s|/data/|$CURRENT_DIR/csv/|g" "$SQL_FILE.bak"
       # COPY를 \COPY로 변경
      sed -i '' "s|COPY|\\\\COPY|g" "$SQL_FILE.bak"
      # \COPY 명령어를 한 줄로 변환 (주석 제외)
      awk '
        BEGIN { in_copy = 0 }
        /^[[:space:]]*--/ { print; next }  # 한 줄 주석 건너뛰기
        /^[[:space:]]*\/\*/ { in_comment = 1 }  # 블록 주석 시작
        in_comment {
          print
          if (/\*\//) { in_comment = 0 }  # 블록 주석 끝
          next
        }
        /\\COPY/ { in_copy = 1; copy_line = $0; next }
        in_copy {
          copy_line = copy_line " " $0
          if (/;/) {
            print copy_line
            in_copy = 0
          }
          next
        }
        { print }
      ' "$SQL_FILE.bak" > "$SQL_FILE.tmp" && mv "$SQL_FILE.tmp" "$SQL_FILE.bak"
      echo "Executing import script: $SQL_FILE.bak..."

      PGPASSWORD="$DB_PASSWORD" psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -f "$SQL_FILE.bak"
      if [ $? -ne 0 ]; then
        echo "Error occurred while executing import script: $SQL_FILE. Exiting."
        rm "$SQL_FILE.bak"
        exit 1
      fi
      # 백업 파일 삭제
      rm "$SQL_FILE.bak"
    else
      echo "Skipping unrecognized script: $SQL_FILE"
    fi
  done
else
  echo "No init-scripts directory found, skipping execution."
fi

# 보안상 PGPASSWORD 삭제
unset PGPASSWORD

# 완료 메시지 출력
echo "All schema and import scripts have been executed successfully!"
