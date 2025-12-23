# Docker Desktop Windows 설치 가이드

## 설치 방법

### 1. 사전 요구사항 확인
- Windows 10 64-bit: Pro, Enterprise, 또는 Education (빌드 19041 이상)
- 또는 Windows 11 64-bit
- WSL 2 기능 활성화 필요

### 2. WSL 2 설치 (필요한 경우)

PowerShell을 **관리자 권한**으로 실행하고 다음 명령어 실행:

```powershell
wsl --install
```

재부팅 후 WSL 2가 설치됩니다.

### 3. Docker Desktop 다운로드 및 설치

1. **다운로드**: https://www.docker.com/products/docker-desktop/
   - 또는 직접: https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

2. **설치**:
   - 다운로드한 설치 파일 실행
   - 설치 옵션에서 "Use WSL 2 instead of Hyper-V" 선택 (권장)
   - 설치 완료 후 재시작

3. **실행**:
   - Docker Desktop 실행
   - 시작 메뉴에서 "Docker Desktop" 검색하여 실행
   - 시스템 트레이에 Docker 아이콘이 나타나면 준비 완료

### 4. 설치 확인

PowerShell 또는 명령 프롬프트에서 실행:

```bash
docker --version
docker-compose --version
```

정상적으로 버전이 표시되면 설치 완료!

## 빠른 설치 스크립트 (PowerShell)

PowerShell을 관리자 권한으로 실행 후:

```powershell
# WSL 2 설치 확인 및 설치
wsl --install

# WSL 기본 버전을 2로 설정
wsl --set-default-version 2

# 재부팅 후 Docker Desktop 설치 파일 다운로드 및 실행
# (다운로드 링크는 위의 가이드를 참고하세요)
```

## 문제 해결

### Docker Desktop이 시작되지 않는 경우
- WSL 2가 제대로 설치되었는지 확인: `wsl --status`
- 가상화 기능이 활성화되어 있는지 확인 (BIOS 설정)

### "WSL 2 installation is incomplete" 오류
- Windows 업데이트 확인
- WSL 2 커널 업데이트: https://aka.ms/wsl2kernel

## 설치 확인 명령어

```bash
# Docker 버전 확인
docker --version

# Docker Compose 버전 확인
docker-compose --version

# Docker 데몬 상태 확인
docker info

# 간단한 테스트 (Hello World 컨테이너 실행)
docker run hello-world
```

설치가 완료되면 프로젝트 루트에서 다음 명령어로 인프라를 시작할 수 있습니다:

```bash
docker-compose up -d
```


