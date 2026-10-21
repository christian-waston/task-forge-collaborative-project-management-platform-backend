"""Runtime smoke test for the TaskForge midterm API (not part of the submission).

Seeds the Step 0 data through the save endpoints, then walks through every
requirement (A2, B1-B3, C1-C4, D1-D4, E1-E3, F1-F2 and the two bonus ones),
asserting the expected HTTP status and messages from the paper.

Member index mapping (sorted by firstName): 0=Eric (no skills), 1=Leon
(two skills), 2=Lilia (one skill). Task dates are in the future so the D1
rule accepts the seed saves.
"""
import json
import urllib.request
import urllib.error
from urllib.parse import quote

BASE = "http://localhost:8080"
results = []
FAILURES = []


def call(method, path, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    if data:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            raw = resp.read().decode()
            if not raw:
                return resp.status, None
            try:
                return resp.status, json.loads(raw)
            except json.decoder.JSONDecodeError:
                return resp.status, raw
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except json.decoder.JSONDecodeError:
            return e.code, raw


def check(name, ok, extra=""):
    mark = "PASS" if ok else "FAIL"
    results.append(f"[{mark}] {name} {extra}")
    if not ok:
        FAILURES.append(f"{name} {extra}")


def find_id(items, key, value):
    return next(x["id"] for x in items if x.get(key) == value)


def tasks_all():
    return call("GET", "/api/tasks/all")[1]


# ---------------- Step 0: seed data through the own save endpoints ----------
for name in ["Backend", "Frontend", "UI Design", "Testing"]:
    status, body = call("POST", "/api/skills/save", {"name": name})
    check(f"seed skill {name}: 201", status == 201 and body == "Skill saved successfully", str(body))

for i in range(1, 5):
    status, body = call("POST", "/api/workstations/save", {"stationNumber": i, "name": f"Workstation {i}"})
    check(f"seed workstation {i}: 201", status == 201 and body == "Workstation saved successfully", "")

skills = call("GET", "/api/skills/all")[1]
workstations = call("GET", "/api/workstations/all")[1]
skill_ids = {"Backend": find_id(skills, "name", "Backend"), "Frontend": find_id(skills, "name", "Frontend"),
             "UI Design": find_id(skills, "name", "UI Design"), "Testing": find_id(skills, "name", "Testing")}
ws_ids = {i: find_id(workstations, "stationNumber", i) for i in range(1, 5)}

members_payload = [
    {"firstName": "Leon", "lastName": "Abumukiza", "email": "leon@taskforge.rw"},
    {"firstName": "Lilia", "lastName": "MUHIRE", "email": "lilia@taskforge.rw"},
    {"firstName": "Eric", "lastName": "NKURUNZIZA", "email": "eric@taskforge.rw"},
]
for i, m in enumerate(members_payload, start=1):
    m["workstation"] = {"id": ws_ids[i]}
    m["skills"] = [{"id": skill_ids["Backend"]}, {"id": skill_ids["UI Design"]}] if i == 1 else (
        [{"id": skill_ids["Frontend"]}] if i == 2 else [])
    status, body = call("POST", "/api/team-members/save", m)
    check(f"seed member {i}: 201", status == 201 and body == "Team member saved successfully", "")

projects_payload = [
    {"projectName": "Mobile Banking App", "clientName": "MTN Rwanda", "projectCode": "TF-2026-01", "description": "MTN MoMo app"},
    {"projectName": "MoMo Referral Engine", "clientName": "MTN Rwanda", "projectCode": "TF-2026-02", "description": "Referrals"},
    {"projectName": "Core Lending Portal", "clientName": "Bank of Kigali", "projectCode": "TF-2026-03", "description": "Loans"},
    {"projectName": "Airtime Dashboard", "clientName": "Airtel", "projectCode": "TF-2026-04", "description": "Dashboards"},
    {"projectName": "Booking Website", "clientName": "Rwandair", "projectCode": "TF-2026-05", "description": "Tickets"},
]
for p in projects_payload:
    status, body = call("POST", "/api/projects/save", p)
    check(f"seed project {p['projectCode']}: 201", status == 201 and body == "Project saved successfully", "")

members = sorted(call("GET", "/api/team-members/all")[1], key=lambda x: x["firstName"])  # 0=Eric, 1=Leon, 2=Lilia
member_ids = [m["id"] for m in members]
proj_ids = {p["projectCode"]: p["id"] for p in call("GET", "/api/projects/all")[1]}

tasks_payload = [
    # project TF-2026-01 holds three tasks
    ("Write upload API", "2026-11-10", 2, "TF-2026-01", 1),        # stays OPEN
    ("Design payment screens", "2026-11-11", 1, "TF-2026-01", 2),
    ("Review payment flow", "2026-11-12", 3, "TF-2026-01", 2),     # -> IN_PROGRESS -> DONE
    ("Hook analytics", "2026-10-22", 2, "TF-2026-02", 1),          # -> IN_PROGRESS -> DONE
    ("Fix referral bug", "2026-10-25", 1, "TF-2026-02", 0),        # -> IN_PROGRESS -> DONE
    ("Split ledger task", "2026-11-05", 2, "TF-2026-03", 1),       # -> DROPPED, frees the date
    ("Refresh dashboard", "2026-11-15", 1, "TF-2026-04", 0),       # stays OPEN (deleted in E3)
    ("Cache ticket prices", "2026-11-16", 2, "TF-2026-04", 2),     # -> IN_PROGRESS (F2 cancels)
    ("Poll payment status", "2026-11-20", 2, "TF-2026-05", 2),     # stays OPEN
]
task_ids = {}
for title, due, prio, pcode, mi in tasks_payload:
    body = {"dueDate": due, "title": title, "priority": prio,
            "project": {"id": proj_ids[pcode]}, "teamMember": {"id": member_ids[mi]}}
    status, msg = call("POST", "/api/tasks/save", body)
    if status != 201:
        check(f"seed task '{title}'", False, f"{status} {msg}")
        break
    task_ids = {t["title"]: t["id"] for t in tasks_all()}
check("seed base tasks: 201", len(task_ids) == 9, f"{sorted(task_ids)}")
T = task_ids
status, body = call("GET", "/api/tasks/00000000-0000-0000-0000-000000000001")
check("A2 GET unknown id: 404 + message", status == 404 and body == "The task with that id does not exist", str(body))

# ---------------- D2 destructs the seeding order first ----------------------
status, body = call("PATCH", f"/api/tasks/{T['Split ledger task']}/status?status=DROPPED")
check("D2 OPEN->DROPPED: 200 (frees 2026-11-05)", status == 200 and body == "Task status updated successfully", str(body))

status, body = call("POST", "/api/tasks/save", {
    "dueDate": "2026-11-05", "title": "Second ledger task", "priority": 3,
    "project": {"id": proj_ids["TF-2026-03"]}, "teamMember": {"id": member_ids[1]}})
all_tasks = tasks_all()
task_ids["Second ledger task"] = next(t["id"] for t in all_tasks if t["title"] == "Second ledger task")
second_open = next(t for t in all_tasks if t["title"] == "Second ledger task")["status"] == "OPEN"
check("D1 second task on same date accepted (DROPPED one does not block)",
      status == 201 and second_open, str(body))

# the two tasks move through the flow; DONE goes through IN_PROGRESS first
for title, target in [("Fix referral bug", "DONE"), ("Hook analytics", "DONE"),
                      ("Review payment flow", "DONE"),
                      ("Design payment screens", "IN_PROGRESS"), ("Cache ticket prices", "IN_PROGRESS")]:
    if target == "DONE":
        call("PATCH", f"/api/tasks/{T[title]}/status?status=IN_PROGRESS")
    status_c, msg_c = call("PATCH", f"/api/tasks/{T[title]}/status?status={target}")
    check(f"transition {title} -> {target}", status_c == 200, str(msg_c))

# ---------------- B1-B3: derived queries ------------------------------------
status, body = call("GET", "/api/projects/by-client-name?clientName=" + quote("mtn rwanda"))
names = [p["projectName"] for p in body] if isinstance(body, list) else []
check("B1 by client (case-insensitive, sorted)", status == 200 and
      names == ["Mobile Banking App", "MoMo Referral Engine"], str(names))

status, body = call("GET", "/api/tasks/by-status?status=OPEN")
check("B2 by status OPEN, earliest first", status == 200 and isinstance(body, list) and
      [t["dueDate"] for t in body] == sorted(t["dueDate"] for t in body),
      str([t["title"] for t in body]) if isinstance(body, list) else str(body))

status, body = call("GET", "/api/tasks/by-status?status=WRONG")
check("B2 invalid enum handled", status in (400, 500), str(status))

status, body = call("GET", "/api/tasks/between?start=2026-11-01&end=2026-11-30")
check("B3 between inclusive + sorted", status == 200 and isinstance(body, list) and
      [t["dueDate"] for t in body] == sorted(t["dueDate"] for t in body) and
      all("2026-11-01" <= t["dueDate"] <= "2026-11-30" for t in body),
      str(len(body) if isinstance(body, list) else body))

# ---------------- C1-C4: JPQL with joins ------------------------------------
status, body = call("GET", "/api/team-members/by-skill?name=backend")
check("C1 by skill (LOWER match)", status == 200 and [m["firstName"] for m in body] == ["Leon"], str(body))

status, body = call("GET", "/api/workstations/free")
numbers = [w["stationNumber"] for w in body] if isinstance(body, list) else []
check("C2 free workstation is station 4", status == 200 and numbers == [4], str(numbers))

status, body = call("GET", "/api/skills/unused")
check("C3 unused skill is Testing", status == 200 and [s["name"] for s in body] == ["Testing"], str(body))

status, body = call("GET", f"/api/projects/of-team-member/{member_ids[1]}")
check("C4 projects of Leon (distinct)", status == 200 and
      sorted(p["projectCode"] for p in body) == ["TF-2026-01", "TF-2026-02", "TF-2026-03"], str(body if isinstance(body, list) else body))

status, body = call("GET", f"/api/projects/of-team-member/{member_ids[2]}")
check("C4 projects of Lilia (distinct)", status == 200 and
      sorted(p["projectCode"] for p in body) == ["TF-2026-01", "TF-2026-04", "TF-2026-05"], "")

status, body = call("GET", "/api/projects/of-team-member/00000000-0000-0000-0000-000000000001")
check("C4 unknown member: 404", status == 404 and body == "The team member with that id does not exist", str(body))

# ---------------- D1-D4: business rules on the live data --------------------
status, body = call("POST", "/api/tasks/save", {"dueDate": "2026-01-01", "title": "old", "priority": 1})
check("D1 past date: 400", status == 400 and body == "Task date cannot be in the past", str(body))

status, body = call("POST", "/api/tasks/save", {
    "dueDate": "2026-10-22", "title": "ledger clash", "priority": 1,
    "project": {"id": proj_ids["TF-2026-03"]}, "teamMember": {"id": member_ids[1]}})
check("D1 duplicate member/active date: 409", status == 409 and
      body == "Team member already has a task due on that date", str(body))

status, body = call("POST", "/api/tasks/save", {
    "dueDate": "2026-11-27", "title": "Force OPEN", "priority": 1, "status": "DONE",
    "project": {"id": proj_ids["TF-2026-05"]}, "teamMember": {"id": member_ids[2]}})
forced = next(t for t in tasks_all() if t["title"] == "Force OPEN")
check("D1 new task forced OPEN (status in body ignored)", status == 201 and forced["status"] == "OPEN", str(forced["status"]))

status, body = call("PATCH", f"/api/tasks/{forced['id']}/status?status=DONE")
check("D2 illegal OPEN->DONE: 400", status == 400 and body == "Cannot change status from OPEN to DONE", str(body))

status, body = call("PATCH", f"/api/tasks/{forced['id']}/status?status=IN_PROGRESS")
check("D2 OPEN->IN_PROGRESS: 200", status == 200 and body == "Task status updated successfully", str(body))

status, body = call("PATCH", f"/api/tasks/{forced['id']}/status?status=OPEN")
check("D2 illegal IN_PROGRESS->OPEN: 400", status == 400 and body == "Cannot change status from IN_PROGRESS to OPEN", str(body))

status, body = call("PATCH", f"/api/tasks/{forced['id']}/status?status=DONE")
check("D2 IN_PROGRESS->DONE: 200", status == 200, str(body))

status, body = call("PATCH", f"/api/tasks/{forced['id']}/status?status=OPEN")
check("D2 DONE is final: 400", status == 400 and body == "Cannot change status from DONE to OPEN", str(body))

status, body = call("PATCH", "/api/tasks/00000000-0000-0000-0000-000000000001/status?status=OPEN")
check("D2 unknown id: 404", status == 404 and body == "The task with that id does not exist", str(body))

status, body = call("PUT", f"/api/tasks/update/{forced['id']}",
                    {"dueDate": "2026-11-28", "title": "Force OPEN", "priority": 4,
                     "status": "OPEN",
                     "project": {"id": proj_ids["TF-2026-05"]}, "teamMember": {"id": member_ids[2]}})
updated = next(t for t in tasks_all() if t["title"] == "Force OPEN")
check("D2 PUT update keeps status OPEN-ish (not changed to OPEN)", status == 200 and updated["priority"] == 4 and updated["status"] == "DONE", str(updated["status"]))

status, body = call("PUT", f"/api/team-members/{member_ids[2]}/skills/{skill_ids['Testing']}")
check("D3 add skill: 200", status == 200 and body == "Skill added to the team member", str(body))

status, body = call("PUT", f"/api/team-members/{member_ids[2]}/skills/{skill_ids['Testing']}")
check("D3 duplicate skill: 409", status == 409 and body == "The team member already has that skill", str(body))

status, body = call("PUT", f"/api/team-members/00000000-0000-0000-0000-000000000001/skills/{skill_ids['Testing']}")
check("D3 unknown member: 404", status == 404 and body == "The team member with that id does not exist", str(body))

status, body = call("PUT", f"/api/team-members/{member_ids[2]}/skills/00000000-0000-0000-0000-000000000001")
check("D3 unknown skill: 404", status == 404 and body == "That skill does not exist", str(body))

status, body = call("PATCH", f"/api/team-members/{member_ids[2]}/workstation?stationNumber=4")
check("D4 move Lilia to station 4: 200", status == 200 and body == "Workstation changed successfully", str(body))

status, body = call("PATCH", f"/api/team-members/{member_ids[1]}/workstation?stationNumber=4")
check("D4 taken workstation: 409", status == 409 and body == "That workstation is taken by another team member", str(body))

status, body = call("PATCH", f"/api/team-members/{member_ids[2]}/workstation?stationNumber=99")
check("D4 unknown station: 404", status == 404 and body == "The workstation with that stationNumber does not exist", str(body))

status, body = call("PATCH", f"/api/team-members/{member_ids[2]}/workstation?stationNumber=2")
check("D4 move back to station 2: 200", status == 200, str(body))

status, body = call("GET", "/api/workstations/free")
numbers = [w["stationNumber"] for w in body] if isinstance(body, list) else []
check("C2 after D4 shuffle: station 4 free again", status == 200 and numbers == [4], str(numbers))

status, body = call("GET", "/api/skills/unused")
check("C3 after D3: no unused skill", status == 200 and body == [], str(body))

# ---------------- E1-E3: work logs ------------------------------------------
status, body = call("POST", f"/api/work-logs/save?taskId={T['Fix referral bug']}",
                    {"activity": "Code review", "comment": "Reviewed the login controller", "hoursSpent": 3})
check("E1 log on DONE task: 201", status == 201 and body == "Work log saved successfully", str(body))

status, body = call("POST", f"/api/work-logs/save?taskId={T['Write upload API']}",
                    {"activity": "Spike", "comment": "should be refused", "hoursSpent": 1})
check("E1 log on OPEN task: 400", status == 400 and body == "Work logs are only for DONE tasks", str(body))

status, body = call("POST", "/api/work-logs/save?taskId=00000000-0000-0000-0000-000000000001",
                    {"activity": "X", "comment": "y", "hoursSpent": 1})
check("E1 unknown task: 404", status == 404 and body == "The task with that id does not exist", str(body))

status, body = call("POST", f"/api/work-logs/save?taskId={T['Review payment flow']}",
                    {"activity": "Steps definition", "comment": "second log", "hoursSpent": 2})
check("E1 second log: 201", status == 201, str(body))

status, body = call("GET", "/api/work-logs/project/TF-2026-01")
logs = body if isinstance(body, list) else []
tasks_dates = [w["loggedDate"] for w in logs]
check("E2 project TF-2026-01 history", status == 200 and len(logs) == 1 and body[0]["task"]["project"]["projectCode"] == "TF-2026-01",
      f"{len(logs)} log(s), loggedDate={tasks_dates}")

status, body = call("GET", "/api/work-logs/project/TF-2026-09")
check("E2 unknown project code: empty list", status == 200 and body == [], str(body))

all_work_logs = call("GET", "/api/work-logs/all")[1]
log_id = all_work_logs[-1]["id"]

status, body = call("DELETE", f"/api/tasks/delete/{T['Fix referral bug']}")
check("E3 task with logs: 409", status == 409 and body == "That task has work logs, delete them first", str(body))

status, body = call("DELETE", f"/api/work-logs/delete/{log_id}")
check("E3 delete log: 200", status == 200 and body == "Work log deleted successfully", str(body))

status, body = call("DELETE", f"/api/work-logs/delete/{log_id}")
check("E3 delete same log again: 404", status == 404 and body == "The work log with that id does not exist", str(body))

status, body = call("DELETE", f"/api/tasks/delete/{T['Refresh dashboard']}")
check("E3 task without logs: 200", status == 200 and body == "Task deleted successfully", str(body))

# ---------------- F1-F2: reports and bulk updates ---------------------------
status, body = call("GET", "/api/tasks/stats/by-status")
flat = [[str(row[0]), row[1]] for row in body] if isinstance(body, list) else body
check("F1 stats per status", status == 200 and isinstance(body, list), str(flat))

status, body = call("PATCH", f"/api/tasks/cancel-day?teamMemberId={member_ids[2]}&date=2026-11-16")
check("F2 cancel-day Lilia 2026-11-16: 1", status == 200 and body == "1 tasks cancelled", str(body))

status, body = call("PATCH", f"/api/tasks/cancel-day?teamMemberId={member_ids[2]}&date=2026-01-01")
check("F2 cancel-day nothing: 0", status == 200 and body == "0 tasks cancelled", str(body))

lilia_tasks = [t for t in tasks_all() if t["teamMember"]["id"] == member_ids[2] and t["dueDate"] == "2026-11-16"]
check("F2 task became DROPPED", all(t["status"] == "DROPPED" for t in lilia_tasks), str([t["status"] for t in lilia_tasks]))

status, body = call("PATCH", f"/api/tasks/cancel-day?teamMemberId={member_ids[0]}&date=2026-10-25")
check("F2 DONE tasks are never cancelled: 0", status == 200 and body == "0 tasks cancelled", str(body))

# ---------------- Bonus ------------------------------------------------------
status, body = call("GET", "/api/dashboard/summary")
check("Bonus dashboard keys + shape", status == 200 and list(body.keys()) == [
    "projects", "teamMembers", "freeWorkstations", "tasksDueToday", "open", "workLogs"], str(body))

status, body = call("GET", "/api/tasks/page?page=0&size=5&sort=dueDate,desc")
check("Bonus paging shows totals", status == 200 and body.get("totalElements", 0) > 0 and
      "totalPages" in body and len(body.get("content", [])) == 5,
      f"totalElements={body.get('totalElements')} totalPages={body.get('totalPages')}")

print("\n".join(results))
print()
if FAILURES:
    print(f"{len(FAILURES)} FAILURES:")
    for f in FAILURES:
        print(" -", f)
else:
    print(f"ALL {len(results)} CHECKS PASSED")
