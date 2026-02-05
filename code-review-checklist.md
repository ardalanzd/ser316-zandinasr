# Code Review Checklist

**Reviewer Name:** [Your Name]
**Date:** [Date]
**Branch:** Review

## Instructions
Review ALL source files (in main not test) in the project and identify defects using the categories below. Log at least 5 defects total:
- At least 1 from CS (Coding Standards)
- At least 1 from CG (Code Quality/General)
- At least 1 from FD (Functional Defects)
- Remaining can be from any category

## Review Categories

- **CS**: Coding Standards (naming conventions, formatting, style violations)
- **CG**: Code Quality/General (design issues, code smells, maintainability)
- **FD**: Functional Defects (logic errors, incorrect behavior, bugs)
- **MD**: Miscellaneous (documentation, comments, other issues)

## Defect Log

| 1 | Checkout.java | 76-77 | CS | Hard-coded fine threshold 10.0 should use a named constant instead of a magic number | Medium |
| 2 | Checkout.java | 94-115 | CG | checkoutBook JavaDoc is excessively long, mixes specification with algorithm steps reducing readability | Low |
| 3 | Checkout.java | 258-263 | FD | Validation order may cause return behavior compared to specification, handling null values | Medium |
| 4 | Patron.java | 205 | CS | Single-line if statement without braces reduces readability and can cause maintenance errors | Low |
| 5 | Book.java | 124-138 | CG | equals/hashCode implementation verbose and could be simplified using Objects helper methods | Low |



**Severity Levels:**
- **Critical**: Causes system failure, data corruption, or security issues
- **High**: Major functional defect or significant quality issue
- **Medium**: Moderate issue affecting maintainability or minor functional problem
- **Low**: Minor style issue or cosmetic problem

## Example Entry

| Defect ID | File          | Line(s) | Category | Description                                | Severity |
|-----------|---------------|---------|----------|--------------------------------------------|----------|
| 1 | Checkout.java | 17      | CS       | Variable bookList misleading - Map not List | Medium |
| 2 | Book.java     | 107     | FD       | Magic number 100 should be totalCopies      | High |

## Notes
- Be specific with line numbers
- Provide clear, actionable descriptions
- Consider: readability, maintainability, correctness, performance, security
- Focus on issues that impact code quality or functionality


## Assignment 3 – Task 1: Structured Code Review (Defect Log)
| Defect ID | File | Line(s) | Category | Severity | Description |
|---|---|---:|---|---|---|
| D01 | src/main/java/Checkout.java | 13 | CS | Medium | Constant should be `private static final double MAX_FINE_AMOUNT = 25.0;` (currently mutable public static). |
| D02 | src/main/java/Checkout.java | 15 | CG | Low | Fields `bookList`, `patrons`, `history` can be `final` since they are assigned only in constructor. |
| D03 | src/main/java/Checkout.java | 17 | CS | Low | Incomplete/unclear comment (`history; //`). Should be descriptive or removed. |
| D04 | src/main/java/Checkout.java | N/A | FD | High | `isPatronType()` uses `==` for String comparison (reference compare). Should use `.equals(...)`. |
| D05 | src/main/java/Checkout.java | 299 | CS | Low | Variable name `looped` is unclear. Prefer `count` or `matchedCount` for readability. |
| D06 | src/main/java/Patron.java | 130 | CG | Low | Redundant API: `chkSuspended()` duplicates `isAccountSuspended()`; keep one for consistency. |
| D07 | src/test/java/BlackBox.java | 110 | CS | Low | variable naming issue for checkoutBookResult: tests use generic variable name `code`; prefer `checkoutBookResult`/`resultCode`. |
| D08 | src/main/java/Main.java | 7 | MD | Low | Demo `Main` mixes many scenarios; consider structuring output/tests separately or adding clearer scenario headings. |
| D09 | src/main/java/Checkout.java | 66 | CG | Medium | Magic number return codes (3.1, 4.1, etc.) should be named constants/enums for maintainability. |
