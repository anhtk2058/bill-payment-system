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

1. **All-or-nothing payment**: If paying multiple bills and one fails, all previously paid bills are rolled back via Saga compensation.
2. **Due date priority**: Multi-bill payments process bills sorted by due date (earliest first).
3. **Seed data**: 3 default bills pre-loaded on startup (ELECTRIC/WATER/INTERNET).
4. **BigDecimal for amounts**: Avoids floating-point precision issues in financial calculations.
5. **Scheduler**: Background daemon thread checks PENDING payments every second and processes them when their scheduled date arrives.
