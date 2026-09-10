# Harold User Guide

Harold is a French bulldog-themed task chatbot. Commands can be entered in the
JavaFX interface or the command-line interface.

## Adding events

Add a date-only event using ISO dates:

```text
event conference /from 2026-09-10 /to 2026-09-11
```

Harold displays the dates in a readable format:

```text
Got it. I've added this task:
  [E][ ] conference (from: Sep 10 2026 to: Sep 11 2026)
Now you have 1 task in the list.
```

Add a timed event by appending a 24-hour time in `HHmm` format to both endpoints:

```text
event lunch /from 2026-09-10 1200 /to 2026-09-10 1300
```

Both endpoints must include times, or both must be date-only. Times use the
user's local context; Harold does not perform timezone conversion.

## Detecting schedule anomalies

Harold checks new, incomplete timed events against existing incomplete timed
events. Overlapping events are still added, followed by a warning containing
the original task numbers:

```text
Heads up! This event clashes with:
1.[E][ ] lunch (from: Sep 10 2026, 12:00 PM to: Sep 10 2026, 1:30 PM)
```

Endpoints are treated as boundaries. Events from `1200` to `1300` and from
`1300` to `1400` touch but do not clash. Cross-midnight events are supported
when the end date and time is later than the start.

Date-only events can share dates. When at least one overlapping event has no
time, Harold adds the new event and explains that precise detection requires
times:

```text
Heads up! These events share a date, but at least one has no time:
1.[E][ ] conference (from: Sep 10 2026 to: Sep 11 2026)
Add times using yyyy-MM-dd HHmm if you want Harold to check for a precise clash.
```

Completed events, deadlines, and todos do not participate in clash detection.
Existing date-only event records remain compatible with the new timed format.

## Event validation

- Date-only ranges may start and end on the same date.
- A date-only end date cannot be before its start date.
- A timed event must end strictly after it starts.
- `/from` and `/to` must use matching date-only or timed formats.
- Date input uses `yyyy-MM-dd`; timed input uses `yyyy-MM-dd HHmm`.
