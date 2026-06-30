# Candidate Profile Builder (CPB)

An internal candidate data normalization tool built with **Spring Boot**, **React + Vite**, and **MySQL**.

The system accepts unstructured candidate recruiter notes (PDF) alongside structured system records (CSV), parses both files, normalizes data fields, resolves discrepancies, assigns confidence scores, tracks data provenance, and creates a consolidated **Canonical Candidate Profile**.

---

## 📌 Problem Statement & Context

In modern recruitment and HR operations, candidate records originate from diverse channels (job boards, agency submissions, internal spreadsheets, and direct recruiter note uploads). This leads to several challenges:

- **Data Fragmentation**: Candidate details exist across multiple formats and systems.
- **Inconsistent Formatting**: Email cases, phone formats, and skill names vary widely (e.g., `ReactJS` vs `React`, `+91 9876543210` vs `9876543210`).
- **Data Discrepancies**: The job title in recruiter notes may differ from the role submitted in a recruiter spreadsheet.

**Candidate Profile Builder** solves this by providing an automated rule-based ingestion and normalization engine that creates a transparent, auditable, single source of truth for candidate data.

---

## 🏗 System Architecture

```
                  ┌────────────────────────┐
                  │   React + Vite Frontend │
                  └───────────┬────────────┘
                              │ HTTP / REST
                              v
                  ┌────────────────────────┐
                  │  Spring Boot Backend   │
                  └───────────┬────────────┘
                              │
       ┌──────────────────────┼──────────────────────┐
       │                      │                      │
       v                      v                      v
┌──────────────┐      ┌──────────────┐      ┌──────────────────┐
│ PDFBox       │      │ Commons CSV  │      │ Normalization &  │
│ Parser       │      │ Parser       │      │ Merge Engine     │
└──────────────┘      └──────────────┘      └─────────┬────────┘
                                                       │
                                                       v
                                            ┌──────────────────┐
                                            │ MySQL Database   │
                                            └──────────────────┘
```

---

## 🔄 Core Data Processing Pipeline

1. **Upload**: User uploads Candidate Recruiter Notes (`.pdf`) and a Data File (`.csv`).
2. **Parsing**:
   - The recruiter notes parser uses Apache PDFBox to extract text and regex rules to discover Name, Emails, Phones, Headline, Skills, Education, and Experience.
   - `CsvParserService` uses Apache Commons CSV to extract mapped candidate columns (`name`, `email`, `phone`, `title`, `skills`).
3. **Normalization**:
   - **Emails**: Trimmed and lowercased.
   - **Phones**: Non-digits removed; 10-digit Indian numbers converted to standard `+91` format.
   - **Skills**: Canonical mapping applied (e.g., `ReactJS` → `React`, `SpringBoot` → `Spring Boot`, `JS` → `JavaScript`).
   - **Names**: Extra whitespace removed; exact vs. partial initial matching analyzed.
4. **Merging & Conflict Resolution**:
   - Emails and Phones are merged and deduplicated.
   - Skills are combined across sources.
   - Discrepancies in Name or Headline trigger conflict records (preferring self-described recruiter notes data).
5. **Confidence Scoring & Provenance Tracking**:
   - Each extracted field is assigned a confidence score and provenance trail.
   - Overall confidence is calculated dynamically.
6. **Persistence**: The canonical profile is stored in MySQL with cascading relations.

---

## 📊 Business Rules & Logic

### 1. Merge & Confidence Rules

| Field             | Match Condition                       | Confidence Score                        | Provenance / Source          |
| :---------------- | :------------------------------------ | :-------------------------------------- | :--------------------------- |
| **Skill**         | Appears in both Recruiter Notes & CSV | `0.95`                                  | `["RECRUITER_NOTES", "CSV"]` |
| **Skill**         | Appears in Recruiter Notes only       | `0.80`                                  | `["RECRUITER_NOTES"]`        |
| **Skill**         | Appears in CSV only                   | `0.75`                                  | `["CSV"]`                    |
| **Email / Phone** | Present in both sources               | `0.95`                                  | `["RECRUITER_NOTES", "CSV"]` |
| **Email / Phone** | Present in single source              | `0.80` (Recruiter Notes) / `0.75` (CSV) | Single Source                |
| **Headline**      | Exact match                           | `0.95`                                  | Both Sources                 |
| **Headline**      | Conflict (Disagreement)               | `0.80` (Selected Recruiter Notes)       | Conflict Recorded            |
| **Name**          | Exact / Partial match                 | `0.95` / `0.80`                         | Both Sources                 |

### 2. Conflict Tracking

When sources disagree (e.g., Recruiter Notes Headline: `Java Full Stack Developer` vs. CSV Title: `Java Developer`), a conflict record is generated:

- `fieldName`: Field in conflict (e.g., `headline`)
- `recruiterNotesValue`: Raw value from recruiter notes
- `csvValue`: Raw value from CSV
- `selectedValue`: Value chosen for canonical profile
- `reason`: Explanation of resolution logic

---

## 🔌 API Endpoints

### 1. File Upload & Normalization

- **POST** `/api/candidates/upload`
  - **Form Data**: `resumeFile` (Recruiter Notes PDF), `csvFile` (CSV)
  - **Response**: `UploadResponse` containing canonical profile ID and structured data.

### 2. Candidate Retrieval

- **GET** `/api/candidates`
  - Returns list of all normalized candidate profile summaries.
- **GET** `/api/candidates/{id}`
  - Returns full candidate profile including skills, education, experience, provenance audit trail, and conflict resolution logs.

---

## 🛠 Tech Stack

- **Frontend**: React 18, Vite, React Router DOM, Vanilla CSS (Custom Design System with Glassmorphism)
- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA, Apache PDFBox 3.0.2, Apache Commons CSV 1.11.0, Jackson
- **Database**: MySQL 8.0+

---

## 🚀 How to Run locally

### Prerequisites

- JDK 17+
- Node.js 18+ & npm
- MySQL Server running on `localhost:3306` (default root password `root` or updated in `application.properties`)

### 1. Backend Setup (Spring Boot)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

_The backend will automatically create the `candidate_profile_builder` database in MySQL if it doesn't exist._

### 2. Frontend Setup (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

_Access the Web UI at `http://localhost:5173`._

---

## 🧪 Testing with Sample Data

Pre-configured sample files are located in the `sample-data/` folder:

1. `sample-data/sample_candidate.csv`
2. `sample-data/sample_recruiter_notes.pdf` (Recruiter Notes PDF)

### Quick Upload Demo:

1. Open `http://localhost:5173/upload`.
2. Select `sample_recruiter_notes.pdf` for Recruiter Notes PDF.
3. Select `sample_candidate.csv` for CSV File.
4. Click **Upload & Process**.
5. Explore the generated canonical profile, confidence meters, provenance log, and conflict resolution details.

---

## ⚙️ Custom Configuration Output

The backend also supports custom runtime JSON projection. This satisfies the requirement to emit the default canonical JSON and at least one custom-config JSON output.

### Endpoint

- **POST** `/api/candidates/{id}/project`

### Sample Request

```json
{
  "fields": [
    { "path": "candidate_name", "from": "fullName" },
    { "path": "primary_email", "from": "emails[0]" },
    { "path": "primary_phone", "from": "phones[0]" },
    { "path": "headline", "from": "headline" },
    { "path": "skills", "from": "skills[].name" }
  ],
  "includeConfidence": true,
  "onMissing": "null"
}
```

### Sample Response

```json
{
  "candidate_name": "Baskar S",
  "primary_email": "baskar@gmail.com",
  "primary_phone": "+919876543210",
  "headline": "Java Full Stack Developer",
  "skills": ["Java", "Spring Boot", "React"],
  "overall_confidence": 0.91
}
```

The frontend Candidate Details page displays both:

1. **Canonical JSON Output** - the default backend schema.
2. **Custom Config JSON Output** - a projected schema generated from the runtime config.

A sample config is available at:

```text
sample-data/custom-config.json
```
