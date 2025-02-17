# 사전준비
## 1. 설치 준비
다음 경로로 접속하여 Docker Desktop on Mac 설치를 위한 .dmg 파일을 다운로드\

https://www.docker.com/products/docker-desktop/
## 2. 실행 실습(튜토리얼 문서 참고)
https://docs.microsoft.com/ko-kr/visualstudio/docker/tutorials/docker-tutorial

docker run -d -p 80:80 docker/getting-started


# Billing 프로젝트 디렉토리 구조 설명
## 프로젝트 디렉토리 컨셉
### app-{projectname}으로 명명되어 있는 모듈은 컨테이너로 구성되어 배포되어야 할 모듈

## 프로젝트 디렉토리 설명
- api : 빌링 시스템과 외부 시스템 통신을 위한 API 구현 모듈
- buildSrc : 프로젝트 컴파일 시 라이브러리 및 버전 관리를 위한 빌드 관리 모듈
- common : 프로젝트 전반에 사용될 에러 처리, resilient,log 등의 로직 구현 모듈
- domain : postgres 관련 Repository, Redis Repository 등 구현 모듈
- docker : 로컬 테스트를 위한 도커 구성 스크립트 모음
- docs : 빌링 시스템의 시퀀스 모델링 위한 파일들 모음
- gradle : 프로젝트 빌드시 필요한 자동화 로직 구현 모듈


# Billing 프로젝트 로컬 테스트 방법
    1. docker desktop 실행    
    2. docker 디렉토리 이동
    3. docker 데몬 실행 명령 : ./docker.sh up
    4. docker 데몬 중지 명령 : ./docker.sh down

# Billing 프로젝트 minikube 구성 및 테스트 방법
    1. 추후 업데이트 예정

# TEMPORARY LINE
# TEMPORARY LINE2