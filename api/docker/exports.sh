#!/usr/bin/env bash

# abc-accounting DB 데이터 exports 스크립트
# 사용법
# ./configure.sh DB_HOST DB_PASSWORD DB_USER DB_NAME PGPORT

# PostgreSQL 접속 정보
DB_HOST=${1:-localhost}          # PostgreSQL 호스트 (기본값: localhost)
DB_PASSWORD=${2:-abc_accounting} # PostgreSQL 비밀번호 (기본값: abc_accounting)
DB_USER=${3:-abc_accounting}     # PostgreSQL 사용자 이름 (기본값: abc_accounting)
DB_NAME=${4:-abc_accounting}     # PostgreSQL 데이터베이스 이름 (기본값: abc_accounting)
PGPORT=${5:-5432}                # PostgreSQL 포트 (기본값: 5432)
EXPORTS_SKIP_FILE=${6:-exports-skip} # 스킵할 테이블 목록 파일 (기본값: exports_skip)

SCHEMA_DIR="./init-scripts"  # 현재 디렉토리 기준 init-scripts
EXPORT_BASE_DIR="./exports"   # 현재 디렉토리 기준 export 디렉토리

# 환경변수 설정 (보안상 직접 패스워드 노출을 피하기 위해 사용 가능)
export PGPASSWORD="$DB_PASSWORD"

# 디렉토리 존재 여부 확인 후 생성
mkdir -p "$EXPORT_BASE_DIR"

# exports_skip 파일에서 스킵할 테이블 목록을 배열로 저장 (Bash 3.x 호환)
SKIP_TABLES=()
if [[ -f "$EXPORTS_SKIP_FILE" ]]; then
    while IFS= read -r line; do
        SKIP_TABLES+=("$line")
    done < "$EXPORTS_SKIP_FILE"
fi

# SQL 파일 순회 (`{숫자}.schema-{쿼리유형}.sql` 형식만 처리, 정확히 두 자리 숫자 체크)
for sql_file in "$SCHEMA_DIR"/*.sql; do
    filename=$(basename "$sql_file")

    # 정확히 두 자리 숫자로 시작하고, `schema`가 포함된 파일만 허용
    if [[ ! "$filename" =~ ^[0-9]{3}\.schema-[a-zA-Z0-9_]+\.sql$ ]]; then
        echo "⏭️ Skipping non-schema file: $filename"
        continue
    fi

    # {쿼리유형} 추출
    query_type=$(echo "$filename" | sed -E 's/[0-9]{3}\.schema-([a-zA-Z0-9_]+)\.sql/\1/')

    # 쿼리 유형별 디렉토리 생성
    EXPORT_DIR="$EXPORT_BASE_DIR/$query_type"
    mkdir -p "$EXPORT_DIR"

#    # `CREATE TABLE IF NOT EXISTS` 패턴을 찾아 주석이 아닌 테이블 이름만 추출
#    table_names=$(grep -E 'CREATE TABLE IF NOT EXISTS' "$sql_file" | \
#                  sed -E 's/CREATE TABLE IF NOT EXISTS[[:space:]]+"?([a-zA-Z0-9_]+)"?/\1/' | \
#                  grep -Ev '^\s*(--|#|/\*|\*/)' )  # 주석 제거
#    echo "📌 Found tables in $sql_file: $table_names"  # 디버깅을 위한 출력

    # `CREATE TABLE IF NOT EXISTS` 패턴을 찾아 주석이 아닌 테이블 이름만 추출 (주석 제거 포함)
    table_names=()
    while IFS= read -r line; do
        # 테이블 이름 추출
        table_name=$(echo "$line" | sed -E 's/CREATE TABLE IF NOT EXISTS[[:space:]]+"?([a-zA-Z0-9_]+)"?/\1/')

        # 주석(`--`, `#`, `/* */`)이 포함된 줄 제거
        if [[ "$line" =~ ^\s*(--|#|/\*|\*/) ]]; then
          continue
        fi

        # exports_skip에 포함된 테이블인지 확인
        for skip in "${SKIP_TABLES[@]}"; do
            if [[ "$table_name" == "$skip" ]]; then
                echo "⏭️ Skipping export due to exports_skip file: $table_name"
                continue 2
            fi
        done

        # 테이블이 정상적으로 추출된 경우 추가
        if [[ -n "$table_name" ]]; then
            table_names+=("$table_name")
        fi
    done < <(grep -E 'CREATE TABLE IF NOT EXISTS' "$sql_file")

    echo "📌 Found tables in $sql_file: ${table_names[*]}"  # 디버깅을 위한 출력

    # 각 테이블의 데이터를 CSV로 내보내기
    for table in "${table_names[@]}"; do
        EXPORT_FILE="$EXPORT_DIR/$table.csv"

        # 파일이 이미 존재하는 경우 스킵
        if [ -f "$EXPORT_FILE" ]; then
            echo "⏭️ Skipping export: $EXPORT_FILE already exists."
            continue
        fi

        echo "📤 Exporting table: $table to $EXPORT_FILE"

        # PostgreSQL COPY 명령 실행 (환경변수 PGPASSWORD를 이용)
        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "\COPY $table TO '$EXPORT_FILE' WITH CSV HEADER"

        # 성공 여부 확인
        if [ $? -eq 0 ]; then
            echo "✅ $table 테이블의 데이터가 $EXPORT_FILE 파일에 저장되었습니다."
        else
            echo "❌ $table 테이블 데이터 내보내기에 실패했습니다."
        fi
    done
done

# 보안상 PGPASSWORD 삭제
unset PGPASSWORD

echo "📁 모든 테이블 데이터 내보내기 완료 (이미 존재하는 파일은 스킵됨)."