

# kotlin-hr-auto-assigner-time-forecast
### 코드 기능, 기술 구조 및 설명 링크
https://www.notion.so/146f3ccd4fb580d5845ae013583d412e
### [HR tech]업무 자동 할당 및 업무 완수 예측 API에서 구체적인 설명 첨부

tasks(업무), Ttasks_history(할당된 업무 기록), team_member(멤버) 3개의 기록들을 수치화하여 작업 스케쥴 추출, 작업자 작업 능력 수치화, 작업 기간 예측

<img width="814" alt="20250318_151220" src="https://github.com/user-attachments/assets/78c87fbb-0ac0-47d9-8bbf-2ad9a39d841e" />


### *WorkStream에 작성된 작업을 텍스트로 입력시 해당 작업을 정의된 작업들로 요약

### *최적의 작업 시간으로 작업자들을 작업에 자동 할당

### *할당된 작업들의 작업 완수 시간 예측

### * 작업자들의 가용 인원 확인

### (주의!!) 본 프로젝트 실행하기 이전에 해야할 fast api : db 생성기+텍스트 분석기 기능 링크
- https://github.com/Jinhan-Han-Jeremy/fast-api-text-analysis-for-hr
- DB 생성기+텍스트 분석기 포트 : 8000, mysql 8.0 서버 포트 : 3306
- 메인 서버 어플리케이션 포트 : 8050
