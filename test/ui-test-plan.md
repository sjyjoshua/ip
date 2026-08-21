# UI Test Plan

This file is the source of truth for command-line UI regression tests run by the project-specific `test-ui` skill. Inputs are sent in order to a fresh program process for each test case. Expected outputs exclude the startup banner and separator lines.

## TC01: Add, mark, and list all task types

**Aim:** Verify that todos, deadlines, and events retain their type-specific data and completion status.

### Inputs

```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
mark 1
list
bye
```

### Expected outputs

#### Output 1

```text
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 task in the list.
```

#### Output 2

```text
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks in the list.
```

#### Output 3

```text
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 3 tasks in the list.
```

#### Output 4

```text
Nice! I've marked this task as done:
  [T][X] borrow book
```

#### Output 5

```text
Here are the tasks in your list:
1.[T][X] borrow book
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
```

#### Output 6

```text
Goodbye! Please take me down soon hehe!
```

## TC02: Accept arbitrary deadline text and unmark a task

**Aim:** Verify that deadline timing is stored as text and a completed task can be marked incomplete again.

### Inputs

```text
deadline do homework /by no idea :-p
mark 1
unmark 1
list
bye
```

### Expected outputs

#### Output 1

```text
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 1 task in the list.
```

#### Output 2

```text
Nice! I've marked this task as done:
  [D][X] do homework (by: no idea :-p)
```

#### Output 3

```text
OK, I've marked this task as not done yet:
  [D][ ] do homework (by: no idea :-p)
```

#### Output 4

```text
Here are the tasks in your list:
1.[D][ ] do homework (by: no idea :-p)
```

#### Output 5

```text
Goodbye! Please take me down soon hehe!
```

## TC03: Explain invalid commands and arguments

**Aim:** Verify that every current command reports malformed input through clear chatbot-specific exception messages and continues running.

### Inputs

```text
todo
deadline
deadline /by Sunday
deadline submit /by
event
event meeting /from Monday
event /from Monday /to Friday
event meeting /from /to Friday
event meeting /from Monday /to
mark
unmark 1
todo valid task
mark abc
mark 2
unmark 0
blah
bye
```

### Expected outputs

#### Output 1

```text
OOPS!!! The description of a todo cannot be empty. Try: todo <description>
```

#### Output 2

```text
OOPS!!! A deadline needs '/by <date or time>'. Try: deadline <description> /by <date or time>
```

#### Output 3

```text
OOPS!!! The description of a deadline cannot be empty. Try: deadline <description>
```

#### Output 4

```text
OOPS!!! Please enter a date or time after /by.
```

#### Output 5

```text
OOPS!!! An event needs '/from <start>'. Try: event <description> /from <start> /to <end>
```

#### Output 6

```text
OOPS!!! An event needs '/to <end>'. Try: event <description> /from <start> /to <end>
```

#### Output 7

```text
OOPS!!! The description of an event cannot be empty. Try: event <description>
```

#### Output 8

```text
OOPS!!! Please enter a start date or time after /from.
```

#### Output 9

```text
OOPS!!! Please enter an end date or time after /to.
```

#### Output 10

```text
OOPS!!! Please enter a task number after mark.
```

#### Output 11

```text
OOPS!!! Your task list is empty, so there is nothing to unmark.
```

#### Output 12

```text
Got it. I've added this task:
  [T][ ] valid task
Now you have 1 task in the list.
```

#### Output 13

```text
OOPS!!! 'abc' is not a valid task number. Try: mark <task number>
```

#### Output 14

```text
OOPS!!! Task 2 does not exist. Choose a number from 1 to 1.
```

#### Output 15

```text
OOPS!!! Task 0 does not exist. Choose a number from 1 to 1.
```

#### Output 16

```text
OOPS!!! I don't know what 'blah' means. Try todo, deadline, event, list, mark, unmark, or bye.
```

#### Output 17

```text
Goodbye! Please take me down soon hehe!
```
