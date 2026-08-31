# Documentation Backlog

## Documentation Language Cleanup

### Context

Rule D-7 requires each document to use a single language

During event-flow cleanup, mixed-language headings were fixed only for event-flow documents.
Other ADR, RFC and architecture documents may still contain mixed-language sections

### Deferred

- Review ADR documents for D-7 compliance
- Review RFC documents for D-7 compliance
- Review architecture documents for mixed-language headings
- Align glossary and contract documents with the selected document language

### Reason

The cleanup is intentionally deferred to avoid mixing documentation refactoring with the next
engineering iteration

---

## Phase 1/2 processes

Architecture
------------
✔ launcher-context.md
✔  verification.md
✔ launcher-lifecycle.md
✔ authentication-model.md

RFC
-----------
✔ RFC-0001
✔ RFC-0002
✔ RFC-0003
✔ RFC-0004

ADR
-----------
✔ ADR-0001
✔ ADR-0002
✔ ADR-0003
✔ ADR-0004
✔ ADR-0005
✔ ADR-0006 Authentication Boundaries
✔ ADR-0007 Context ownership
✔ ADR-0008 Operation Resolution Strategy

Diagrams
-----------
✔ verification.puml
✔ session.puml
✔ launcher-startup.puml
✔ launch-sequence.puml
✔ authentication-sequence.puml
✔ authentication-flow.puml
✔ module-dependency.puml

Events
-----------
✔ download-events.md
✔ verification-events.md

Examples
------------
✔ login-sequence.md
✔ logout-sequence.md
✔ verification-flow.md
✔ download-flow.md