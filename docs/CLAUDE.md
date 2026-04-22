# Agent Instructions — AutoPrime

You're working inside the **WAT framework** (Workflows, Agents, Tools) adapted for AutoPrime, a Java/MySQL car dealership app built for ISTE 330 at RIT.

For what the app *is* (tech stack, endpoints, DB schema, file map), read `docs/PROJECT.md`.
This file tells you how to *operate* inside the project.

---

## The Three Layers

**Layer 1 — Workflows (`workflows/`)**
Markdown SOPs for recurring tasks. Each one defines the objective, inputs needed, which tool or command to run, and how to handle failure. Before doing any task that has a workflow, read it first.

**Layer 2 — Agent (you)**
You read workflows, make decisions, call tools in the right order, and handle edge cases. You do not hardcode decisions that belong in a workflow. If a workflow is missing or wrong, flag it and fix it — don't silently work around it.

**Layer 3 — Tools (`tools/`)**
Shell scripts and Python scripts that do deterministic work: running MySQL commands, hashing passwords, compiling the server, seeding data. These are fast, repeatable, and testable. Prefer running a tool over doing the same thing manually.

---

## Project-Specific Context

- **Language:** Java 17. Build with `mvn compile exec:java`. No Tomcat, no Spring.
- **Database:** MySQL on port 3306. Credentials in `db.properties`, keyed by OS username.
- **No `.env` yet.** If external API keys are ever needed, they go in `.env` (gitignored). The Wikipedia API used by `/api/carimage` is unauthenticated and needs no key.
- **Team:** 5 admins (ivan, danis, jurica, tomo, branko) + test employee/customer accounts. All passwords are `password123` (SHA-256 hashed in DB).
- **Frontend:** Plain HTML + CSS + vanilla JS in `src/main/webapp/`. No build step, no npm.
- **Logs:** The app writes to `logs/` at runtime. Check there first when diagnosing server errors.

---

## Workflows Available

| Workflow | What it does |
|---|---|
| `workflows/reset_database.md` | Wipe and reseed the cars table back to sample data |
| `workflows/add_user.md` | Add a new user with a hashed password directly in MySQL |
| `workflows/run_server.md` | Compile and start the Java HTTP server |
| `workflows/fetch_images.md` | Bulk-fetch Wikipedia images for cars with no imageUrl |

---

## Tools Available

| Tool | What it does |
|---|---|
| `tools/reset_db.sh` | Runs DELETE + sample-data.sql against MySQL |
| `tools/hash_password.py` | Prints SHA-256 hex of a plain password (for INSERT into users) |
| `tools/run_server.sh` | Runs `mvn compile exec:java` |
| `tools/fetch_images.py` | Calls `/api/carimage` for each car and prints results |

> Tools that hit MySQL require the caller to pass DB credentials. Check the workflow for the exact invocation.

---

## How to Operate

**1. Look for a workflow before doing anything**
If the user asks to reset the DB, add a user, or start the server — read the relevant workflow in `workflows/` first. Only improvise when no workflow exists.

**2. Fix and improve when things break**
When a tool or workflow fails:
- Read the full error
- Fix the script
- Update the workflow with what you learned (rate limits, path quirks, MySQL version differences)
- Do not silently skip the failure or work around it without documenting it

**3. Keep workflows current**
Workflows evolve as the project does. When a new feature changes how something works (e.g., a new column in `cars`), update the relevant workflow. Do not create or overwrite a workflow without confirming with the user first.

**4. Always update `docs/PROJECT.md` after any code change**
This is a hard rule. After every feature, fix, or structural change, read PROJECT.md and update any stale sections (endpoints, file map, DB schema, planned features).

---

## What Goes Where

```
docs/
  PROJECT.md        — App reference: tech stack, endpoints, DB schema, file map
  CLAUDE.md         — This file: how to operate as the agent

workflows/          — Markdown SOPs for recurring tasks
tools/              — Shell/Python scripts for deterministic execution
.tmp/               — Temporary processing files (gitignored, regeneratable)
logs/               — Runtime server logs (gitignored)
database/
  schema.sql        — Run once to create tables
  sample-data.sql   — Reseed data anytime
```

---

## Bottom Line

Read PROJECT.md to understand the app.
Read the relevant workflow before touching anything.
Run tools instead of doing things manually.
When something breaks, fix it and document the fix.
When code changes, update PROJECT.md.
