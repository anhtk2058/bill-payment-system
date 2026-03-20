# Bill Payment System

A Java CLI application for managing and paying bills, built with clean OOP design using **Command Pattern** and **Saga Pattern** for transactional multi-bill payments.

## Requirements

- Java 11+
- Maven 3.6+

## Build

```bash
mvn clean package
```

## Run Tests

```bash
mvn clean test
```

## Usage

```bash
# Using the shell script (auto-builds if needed)
chmod +x run.sh
./run.sh <COMMAND> [args...]

# Or directly with java
java -jar target/bill-payment.jar <COMMAND> [args...]
```

## Commands

| Command | Description |
|---------|-------------|
| `CASH_IN <amount>` | Add funds to your account |
| `LIST_BILL` | List all bills |
| `ADD_BILL <TYPE> <amount> <dd/MM/yyyy> <provider>` | Create a new bill |
| `UPDATE_BILL <id> <TYPE> <amount> <dd/MM/yyyy> <provider>` | Update a bill |
| `DELETE_BILL <id>` | Delete a bill |
| `PAY <id> [id2] ...` | Pay one or more bills (Saga pattern: all-or-nothing) |
| `DUE_DATE` | List unpaid bills sorted by due date |
| `SCHEDULE <id> <dd/MM/yyyy>` | Schedule a bill payment for a future date |
| `LIST_PAYMENT` | View all payment history |
| `SEARCH_BILL_BY_PROVIDER <name>` | Search bills by provider name |
| `EXIT` | Exit the application |

## Example Session

```bash
$ ./run.sh CASH_IN 1000000
Your available balance: 1000000

$ ./run.sh LIST_BILL
Bill No.  Type       Amount   Due Date    State     PROVIDER
1.        ELECTRIC   200000   25/10/2020  NOT_PAID  EVN HCMC
2.        WATER      175000   30/10/2020  NOT_PAID  SAVACO HCMC
3.        INTERNET   800000   30/11/2020  NOT_PAID  VNPT

$ ./run.sh PAY 1
Payment has been completed for Bill with id 1.
Your current balance is: 800000

$ ./run.sh PAY 2 3
Sorry! Not enough fund to proceed with payment.

$ ./run.sh EXIT
Good bye!
```

## Architecture

- **Command Pattern** — each CLI command is an isolated class, registered in `CommandRegistry`
- **Saga Pattern (Orchestration-based)** — multi-bill payment uses `SagaOrchestrator` with compensating transactions for all-or-nothing atomicity
- **Repository Pattern** — in-memory `ConcurrentHashMap` stores, swappable for DB later
- **Immutable Value Objects** — `Bill` and `Payment` are immutable (Builder pattern)
- **No external libraries** — pure Java 11, only JUnit 5 for testing

## Design Assumptions

1. **All-or-nothing payment (Saga Pattern)**
   Rolling back with a simple try/catch would leave inconsistent state (e.g. bill deducted but bill state not updated). The Saga pattern with explicit `compensate()` steps guarantees that if bill N fails, all previously paid bills are refunded and reverted to `NOT_PAID` — regardless of failure cause.

2. **Due date priority for multi-bill payments**
   When paying multiple bills, they are sorted by `dueDate ASC` before execution. This ensures the most urgent bills are paid first — if funds run out mid-payment, the ones closest to overdue are already settled.

3. **Immutable domain model (`Bill`, `Payment`)**
   Both classes are `final` with all-`final` fields and no setters. State changes produce new copies via `withState()` / `withDetails()`. This prevents accidental mutation across threads (Saga + Scheduler share the same objects), and makes rollback reasoning simple — you always have the original snapshot.

4. **`BigDecimal` for all monetary amounts**
   `double` arithmetic introduces floating-point errors (e.g. `0.1 + 0.2 ≠ 0.3`). Financial applications must use `BigDecimal` for exact decimal arithmetic.

5. **`DUE_DATE` lists all `NOT_PAID` bills sorted by due date**
   Based on the assignment's example output, `DUE_DATE` shows all unpaid bills (not just overdue ones). The sort allows customers to see which bills need attention soonest.

6. **`SCHEDULE` accepts past dates**
   A scheduled payment with `date <= today` will be picked up and processed automatically on the next scheduler tick (within 1 second). This is intentional — it allows retroactive scheduling and immediate processing without a separate command.

7. **Seed data on startup**
   3 pre-loaded bills (ELECTRIC, WATER, INTERNET) match the assignment's `LIST_BILL` example output exactly, ensuring the demo commands work out of the box.

8. **In-memory storage with `ConcurrentHashMap`**
   No database is required. `ConcurrentHashMap` provides thread-safe reads/writes between the main CLI thread and the background `SchedulerService` daemon thread.

