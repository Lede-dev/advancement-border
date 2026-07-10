# AdvancementBorder

Minecraft Java Edition 26.2용 Fabric 모드입니다. 화면에 표시되는 비루트 발전과제 완료 수에 따라 오버월드, 네더, 엔드의 월드보더를 함께 확장하거나 축소합니다.

플레이어용 설치·설정·명령어·문제 해결 안내는 [한국어 플레이어 가이드](docs/PLAYER_GUIDE_KO.md)를 참고하세요.

보더 축소로 플레이어를 이동해야 할 때는 현재 Y와 가까운 동굴이 아니라 목표 보더 안의 지표면 안전 지점을 우선합니다. 네더에서는 기반암 천장 위를 피하고 천장 아래의 가장 높은 안전 지점을 사용합니다.

## 명령어

- `/advboard set <x> <y> <z>`: 일반 플레이어도 사용할 수 있으며, 오버월드 기준 중심과 진행 소유자를 설정합니다.
- `/advboard status`: 현재 설정과 보더 상태를 표시합니다.
- `/advboard recalc`: 발전과제 진행도를 다시 계산합니다.
- `/advboard reload`: `config/advancementborder.json`을 다시 불러옵니다.
- `/advboard config`: 일반 플레이어 권한으로 현재 사전 구성 값을 표시합니다.
- `/advboard config initialDiameter <blocks>`: 초기 보더 크기를 변경하고 저장합니다.
- `/advboard config growthPerAdvancement <blocks>`: 발전과제당 증가량을 변경하고 저장합니다.
- `/advboard config endCenterBlock <x> <z>`: 엔드 중심 블록을 변경하고 저장합니다.

## 빌드와 검증

```powershell
./gradlew.bat clean build
./gradlew.bat runGametest
```

개발 클라이언트는 `./gradlew.bat runClient`로 실행합니다. 실행 직전에 개발용 `run/options.txt`의 마스터 볼륨이 자동으로 `0.0`으로 설정됩니다.
