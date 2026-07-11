# AdvancementBorder 사용 가이드

Minecraft Java Edition 26.2에서 AdvancementBorder를 설치하고 사용하는 방법을 설명합니다.

## 1. 설치

필요한 항목:

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 이상
- Fabric API 0.154.2+26.2
- Java 25

설치 순서:

1. Fabric Loader와 Fabric API를 설치합니다.
2. `advancementborder-1.0.0+26.2.jar`를 Minecraft의 `mods` 폴더에 넣습니다.
3. Fabric 프로필로 게임을 실행합니다.
4. 싱글플레이 월드에 입장합니다.

모든 `/advboard` 명령어는 치트 활성화 없이 일반 플레이어 권한으로 사용할 수 있습니다.

## 2. 빠른 시작

오버월드에서 시작점으로 사용할 블록 위에 선 뒤 다음 명령어를 입력합니다.

```mcfunction
/advboard set ~ ~ ~
```

현재 위치를 중심으로 월드보더가 적용됩니다. 이후 발전과제를 완료하면 월드보더가 초록색으로 표시되며 설정된 시간 동안 확장되고, 완료되면 다시 파란색으로 표시됩니다.

현재 상태를 확인하려면 다음 명령어를 입력합니다.

```mcfunction
/advboard status
```

## 3. 명령어

### 시작점 설정

현재 위치를 시작점으로 설정합니다.

```mcfunction
/advboard set ~ ~ ~
```

지정한 좌표를 시작점으로 설정하려면 X, Y, Z 좌표를 입력합니다.

```mcfunction
/advboard set 120 68 -35
```

이 명령어는 오버월드에서 플레이어가 직접 실행해야 합니다.

### 상태 확인

현재 보더 크기, 완료한 발전과제 수, 설정값과 차원별 중심을 표시합니다. 각 항목은 한 줄씩 출력됩니다.

```mcfunction
/advboard status
```

### 진행도 다시 계산

현재 발전과제 진행도를 다시 확인하고 월드보더를 동기화합니다.

```mcfunction
/advboard recalc
```

발전과제를 달성하거나 취소했는데 보더 크기가 맞지 않을 때 사용하세요.

### 설정 파일 다시 불러오기

`config/advancementborder.json`을 직접 수정한 뒤 변경 내용을 불러옵니다.

```mcfunction
/advboard reload
```

### 현재 설정 확인

초기 보더 크기, 발전과제당 증가량과 엔드 중심을 표시합니다.

```mcfunction
/advboard config
```

다음 명령어도 같은 기능을 실행합니다.

```mcfunction
/advboard config show
```

## 4. 인게임 설정 변경

### 초기 보더 크기

숫자 부분에 원하는 초기 보더 크기를 입력합니다.

```mcfunction
/advboard config initialDiameter 1
```

예를 들어 처음부터 5블록 크기로 시작하려면 다음과 같이 입력합니다.

```mcfunction
/advboard config initialDiameter 5
```

### 발전과제당 증가량

발전과제 하나를 완료할 때 늘어날 보더 길이를 입력합니다.

```mcfunction
/advboard config growthPerAdvancement 2
```

한 번에 1블록씩 늘어나게 하려면 다음과 같이 입력합니다.

```mcfunction
/advboard config growthPerAdvancement 1
```

### 보더 확장 시간

보더가 확장되는 시간을 초 단위로 입력합니다.

```mcfunction
/advboard config expansionDurationSeconds 3
```

### 엔드 중심

엔드 월드보더의 중심 X/Z 블록 좌표를 입력합니다.

```mcfunction
/advboard config endCenterBlock 100 0
```

인게임에서 변경한 설정은 설정 파일에도 저장되며 현재 월드에 바로 반영됩니다.

## 5. 설정 파일 사용

설정 파일 위치:

```text
config/advancementborder.json
```

기본 설정:

```json
{
  "schemaVersion": 1,
  "initialDiameter": 1,
  "growthPerAdvancement": 2,
  "expansionDurationSeconds": 3,
  "endCenterBlock": {
    "x": 100,
    "z": 0
  }
}
```

파일을 수정했다면 게임에서 다음 명령어를 입력합니다.

```mcfunction
/advboard reload
```

`initialDiameter`, `growthPerAdvancement`, `expansionDurationSeconds`에는 1 이상의 정수를 입력해야 합니다.

## 6. 문제 해결

### `/advboard set`이 빨간색으로 표시되는 경우

X, Y, Z 좌표를 모두 입력해야 합니다. 현재 위치를 사용하려면 다음과 같이 입력하세요.

```mcfunction
/advboard set ~ ~ ~
```

### 발전과제를 완료했지만 보더가 변하지 않는 경우

다음 명령어로 진행도를 다시 계산합니다.

```mcfunction
/advboard recalc
```

계속 반영되지 않으면 다음 명령어로 완료 발전과제 수와 오류 항목을 확인하세요.

```mcfunction
/advboard status
```

### 설정을 수정했지만 반영되지 않는 경우

설정 파일을 직접 수정했다면 다음 명령어를 입력합니다.

```mcfunction
/advboard reload
```

명령어가 실패하면 JSON 형식과 설정 숫자가 1 이상인지 확인하세요.

### 명령어 일부가 보이지 않는 경우

최신 모드 JAR이 `mods` 폴더에 설치되어 있는지 확인하고 게임을 완전히 종료한 뒤 다시 실행하세요.

## 빠른 명령어 모음

```mcfunction
# 현재 위치를 시작점으로 설정
/advboard set ~ ~ ~

# 현재 상태 확인
/advboard status

# 현재 설정 확인
/advboard config

# 초기 크기를 1블록으로 설정
/advboard config initialDiameter 1

# 발전과제마다 2블록씩 확장
/advboard config growthPerAdvancement 2

# 3초 동안 부드럽게 확장
/advboard config expansionDurationSeconds 3

# 진행도 다시 계산
/advboard recalc

# 설정 파일 다시 불러오기
/advboard reload
```
