# Diagrams

[Back to README](../README.md) | [All diagrams](diagrams.md)

All diagrams of CosmoScan in one place. Mermaid diagrams are rendered by GitHub directly; the
PlantUML diagrams are the ones shown on the web portal.

- [System architecture](#system-architecture)
- [UC-1: submit a work](#uc-1-submit-a-work)
- [TS-2: technical check](#ts-2-technical-check)
- [TS-3: building the word cloud](#ts-3-building-the-word-cloud)
- [TS-5: data model](#ts-5-data-model)
- [Portal diagrams (PlantUML)](#portal-diagrams-plantuml)

## System architecture

```mermaid
flowchart TB
    Client[Client / Postman]
    UI[web-ui :8094]
    GW[API Gateway :8090]

    SUB[submission-service :8091]
    FS[file-storing-service :8092]
    ANA[analysis-service :8093]

    SUBDB[(PostgreSQL submission :5433)]
    ANADB[(PostgreSQL analysis :5434)]
    FSDISK[(File storage)]
    RPTDISK[(Reports on disk)]

    Client --> GW
    UI -->|fetch API| GW
    GW -->|POST /works| SUB
    GW -->|GET /works/*/reports| ANA
    GW -->|GET /works/*/word-cloud| ANA
    GW -->|POST/GET /files| FS

    SUB --> SUBDB
    SUB -->|store file| FS
    SUB -->|"DELETE /internal/files/{id} (on failure)"| FS
    SUB -->|POST /internal/analysis| ANA

    ANA --> ANADB
    ANA -->|"GET /files/{id}"| FS
    ANA --> RPTDISK
    FS --> FSDISK
```

Services and ports: [README](../README.md#services). Description: [architecture.md](architecture.md).

## UC-1: submit a work

```mermaid
sequenceDiagram
    actor Student
    participant GW as Gateway
    participant SUB as submission-service
    participant FS as file-storing
    participant ANA as analysis-service

    Student->>GW: POST /works (multipart)
    GW->>SUB: POST /works
    SUB->>FS: POST /files
    FS-->>SUB: fileId, metadata
    SUB->>SUB: save Work
    SUB->>ANA: POST /internal/analysis
    ANA->>FS: GET /files/{fileId}
    ANA->>ANA: technical check + word cloud
    ANA-->>SUB: TechnicalReportResponse
    alt save or analysis failed
        SUB->>FS: DELETE /internal/files/{fileId}
        SUB-->>GW: error (work rolled back)
        GW-->>Student: 4xx / 5xx
    else success
        SUB-->>GW: WorkResponse
        GW-->>Student: 201 Created
    end
```

Scenario: [user-scenarios.md](user-scenarios.md#uc-1-a-student-submits-a-work).

## TS-2: technical check

```mermaid
flowchart LR
    A[AnalyzeWorkRequest] --> B{file exists?}
    B -->|no| X[400 Bad Request]
    B -->|yes| C[TechnicalCheckService]
    C --> D{remarks empty?}
    D -->|yes| E[ACCEPTED]
    D -->|no| F[NEEDS_REVISION]
    E --> G[Save TechnicalReport]
    F --> G
    G --> H[Write report JSON]
```

Description: [architecture.md](architecture.md#ts-2-technical-check-without-parsing-the-text).

## TS-3: building the word cloud

```mermaid
flowchart TD
    A[After the technical check] --> B[Download file bytes]
    B --> C[TextExtractionService - Apache Tika]
    C --> D{text empty?}
    D -->|yes| E[status NO_TEXT]
    D -->|no| F[WordFrequencyService]
    F --> G{any terms?}
    G -->|no| E
    G -->|yes| H[status READY + terms]
    B -.->|exception| I[status FAILED]
    E --> J[(work_word_clouds)]
    H --> J
    I --> J
```

Description: [architecture.md](architecture.md#ts-3-building-the-word-cloud).

## TS-5: data model

Logical model over two separate databases (no foreign keys between the PostgreSQL instances).

```mermaid
erDiagram
    WORKS ||--o{ TECHNICAL_REPORTS : "work_id"
    TECHNICAL_REPORTS ||--|{ TECHNICAL_REPORT_REMARKS : "report_id"
    WORKS ||--o| WORK_WORD_CLOUDS : "work_id"
    WORK_WORD_CLOUDS ||--|{ WORK_WORD_CLOUD_TERMS : "word_cloud_id"

    WORKS {
        uuid id PK
        string student_full_name
        timestamptz submitted_at
        string original_file_name
        uuid file_id
        string content_type
        bigint file_size_bytes
    }

    TECHNICAL_REPORTS {
        uuid id PK
        uuid work_id
        uuid file_id
        timestamptz checked_at
        string status
        string report_file_path
    }

    WORK_WORD_CLOUDS {
        uuid id PK
        uuid work_id UK
        uuid file_id
        timestamptz generated_at
        string status
    }

    WORK_WORD_CLOUD_TERMS {
        uuid word_cloud_id PK
        int term_order PK
        string term
        int weight
    }
```

Description: [architecture.md](architecture.md#ts-5-data-storage-per-service).

## Portal diagrams (PlantUML)

These PNGs are shown on the web portal. Sources are in
[`web-ui/diagrams/plantuml/`](../web-ui/diagrams/plantuml/); after changing a source, regenerate the
images with `./gradlew renderDiagrams`.

### Architecture

![CosmoScan architecture](../web-ui/assets/images/cosmoscan-architecture.png)

### UC-1 sequence

![UC-1 sequence](../web-ui/assets/images/cosmoscan-uc1-sequence.png)
