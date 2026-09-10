# Detect Anomalies Test Plan

This plan covers the approved date-only and timed event behavior. Automated
JUnit cases implement the unit and integration checks; transcript cases in
`test/ui-test-plan.md` verify user-visible command responses.

## Schedule parsing and display

| Case | Input or setup | Expected result |
| --- | --- | --- |
| Date only | `2026-09-10` | Retains date-only precision and displays `Sep 10 2026` |
| Timed | `2026-09-10 1430` | Retains time and displays `Sep 10 2026, 2:30 PM` |
| Invalid format | `2026-09-10 2:30pm` | Rejected with the supported formats |
| Mixed precision | Timed `/from`, date-only `/to` | Rejected before addition |
| Existing storage | Stored `2026-09-10` | Loads as a date-only endpoint |
| New storage | Stored `2026-09-10T14:30:00` | Loads as a timed endpoint |

## Range validation

| Case | Range | Expected result |
| --- | --- | --- |
| Single date | Sep 10 to Sep 10 | Accepted |
| Backwards date | Sep 11 to Sep 10 | Rejected |
| Positive duration | 1200 to 1300 | Accepted |
| Zero duration | 1300 to 1300 | Rejected |
| Backwards time | 1400 to 1300 | Rejected |
| Cross midnight | Sep 10 2300 to Sep 11 0100 | Accepted |

## Anomaly detection

| Case | Existing event and candidate | Expected result |
| --- | --- | --- |
| Timed overlap | 1200-1330 and 1300-1400 | Candidate is added with a clash warning |
| Timed containment | 1200-1500 and 1300-1400 | Candidate is added with a clash warning |
| Boundary touch | 1200-1300 and 1300-1400 | Candidate is added without a warning |
| Separate intervals | 1200-1300 and 1400-1500 | Candidate is added without a warning |
| Two date-only events | Intersecting date ranges | Candidate is added with a time advisory |
| Mixed precision | Timed event within a date-only range | Candidate is added with a time advisory |
| Completed overlap | Completed timed event overlaps candidate | Candidate is added without a warning |
| Non-event task | Deadline or todo shares a date or description | Ignored |
| Multiple conflicts | Several incomplete events overlap | All are shown in task-list order |
| Original numbering | Conflicting events are not adjacent | Original task numbers are displayed |

## Persistence and compatibility

- Save and reload a timed event without losing its date, time, or precision.
- Load an existing date-only event record without migration.
- Skip an existing backwards event record and include it in the malformed-record count.
- Do not persist derived clash or advisory information.

## Interfaces and regression

- Verify anomaly messages through `Harold.respond`, which is shared by the CLI and JavaFX interfaces.
- Run all existing task, storage, date, command, and UI transcript tests.
- Run Checkstyle and Javadoc verification after the implementation.
