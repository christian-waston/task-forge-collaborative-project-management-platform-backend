# TaskForge Collaborative Project Management Platform — Backend

Mid-term project examination — **INSY 8322 WEB TECHNOLOGY AND INTERNET**, AUCA.

- **Student:** Abumukiza Leon Christian
- **Student ID:** 26478
- **Course:** Web Technology and Internet (INSY 8322)
- **Project:** TaskForge Collaborative Project Management Platform (backend)

TaskForge helps a software team plan and follow the work of its projects.
A **Project** owns **Tasks** carried out by **TeamMembers** who hold several
**Skills** and sit at a **Workstation**. A task moves from OPEN to IN_PROGRESS
and ends DONE or DROPPED. Once a task is DONE the team member records **WorkLogs**
showing the hours spent.

## Requirements

- Java 26 (JDK)
- Maven (wrapper included, `./mvnw`)
- PostgreSQL running on `localhost:5432`

## Setup steps

1. Create the database in PostgreSQL:

   ```sql
   CREATE DATABASE taskforge;
   ```

2. Adjust `src/main/resources/application.properties` if your PostgreSQL
   username/password is different:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskforge
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

3. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   The API starts on `http://localhost:8080`. The tables (`skill`,
   `workstation`, `team_member`, `team_member_skill`, `project`, `task`,
   `work_log`) are created automatically by Hibernate.

## Endpoint overview

| ID | Method | Path | Behaviour |
|----|--------|------|-----------|
| A2 | POST/GET/PUT/DELETE | `/api/{skills,workstations,team-members,projects,tasks}/save`, `/all`, `/{id}`, `/update/{id}`, `/delete/{id}` | generic CRUD, 201/200/404 with the exact messages |
| B1 | GET | `/api/projects/by-client-name?clientName=mtn rwanda` | DERIVED IgnoreCase + OrderBy |
| B2 | GET | `/api/tasks/by-status?status=OPEN` | DERIVED enum + OrderBy |
| B3 | GET | `/api/tasks/between?start=2026-10-01&end=2026-10-31` | DERIVED Between, dates parsed in the controller |
| C1 | GET | `/api/team-members/by-skill?name=backend` | JPQL JOIN collection + LOWER |
| C2 | GET | `/api/workstations/free` | JPQL NOT EXISTS subquery |
| C3 | GET | `/api/skills/unused` | JPQL IS EMPTY |
| C4 | GET | `/api/projects/of-team-member/{teamMemberId}` | JPQL DISTINCT path navigation, 404 if unknown |
| D1 | POST | `/api/tasks/save` | 400 past date, 409 duplicate due date, always starts OPEN |
| D2 | PATCH | `/api/tasks/{id}/status?status=IN_PROGRESS` | status flow, DONE/DROPPED final |
| D3 | PUT | `/api/team-members/{teamMemberId}/skills/{skillId}` | adds a skill, 404/409/200 |
| D4 | PATCH | `/api/team-members/{teamMemberId}/workstation?stationNumber=4` | moves the team member |
| E1 | POST | `/api/work-logs/save?taskId=...` | only for DONE tasks, loggedDate = today |
| E2 | GET | `/api/work-logs/project/{projectCode}` | JPQL path navigation, newest first |
| E3 | DELETE | `/api/work-logs/delete/{id}` + `/api/tasks/delete/{id}` | task with work logs gives 409 |
| F1 | GET | `/api/tasks/stats/by-status` | JPQL GROUP BY + COUNT, List&lt;Object[]&gt; |
| F2 | PATCH | `/api/tasks/cancel-day?teamMemberId=...&date=2026-10-20` | one JPQL UPDATE with @Modifying |
| Bonus | GET | `/api/dashboard/summary` | LinkedHashMap of six counters |
| Bonus | GET | `/api/tasks/page?page=0&size=5&sort=dueDate,desc` | Page&lt;Task&gt;, Pageable built in the controller |

## Seed data (Step 0)

Enter this through the save endpoints **right after part A**, before the
business rules of part D exist (a save endpoint with the D1 rules refuses
dates in the past, while the paper asks for tasks on dates such as
2026-10-20). If you build part by part, seed at the end of part A.

- **Skill:** Backend, Frontend, UI Design, Testing (nobody holds Testing).
- **Workstation:** four workstations numbered 1..4; workstation 4 has no team member.
- **TeamMember:** three team members, each with a workstation; one holds two
  skills, one holds exactly one.
- **Project:** five projects, two with `clientName = "MTN Rwanda"`, one with
  `projectCode = TF-2026-01` (note its UUID, you will use it a lot).
- **Task:** ten tasks spread over October and November 2026 using all four
  statuses; one project holds at least three tasks; the same team member has
  two tasks on the same date, one of them DROPPED — save the first task, use
  D2 `PATCH /api/tasks/{id}/status?status=DROPPED` on it (DROPPED ones do not
  block a new save), then save the second task on that same date; at least
  two tasks must be DONE because work logs can only be written for them.

## Testing

Import `task-forge-collaborative-project-management-platform-midterm.postman_collection.json`
at the root of this repository into Postman. Every request is named by its
requirement ID. To show a failing case during the live demo, change the id to
an unknown UUID (404), send a past dueDate (400) or a duplicate due date (409).
