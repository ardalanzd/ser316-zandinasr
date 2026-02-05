# Black Box Testing Report - Assignment 2

**Student Name:** [Your Name]  
**ASU ID:** [Your ASU ID]  
**Date:** [Date]

---

## Part 1: Equivalence Partitioning (EP)

Identify equivalence partitions for the `checkoutBook(Book book, Patron patron)` method based on the specification (JavaDoc).

Create **multiple tables**, one per partition category (e.g., book state, patron state, renewal, limits, etc.).

Do **not** put everything into one table.

**Column Explanations:**
- **Partition ID**: Unique identifier (e.g., EP 1.1, EP 2.1)
- **State**: The specific state/value for this partition (e.g., "Unavailable", "Available")
- **Valid/Invalid**: Whether this partition represents valid or invalid input
- **Input Condition**: Precise condition that defines this partition
- **Expected Return**: What return code you expect
- **Expected Behavior**: What should happen

### Example EP Table: Book Availability

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|--------------|-------|---------------|----------------|-----------------|------------------|
| EP 1.1 | Unavailable (0 copies) | Invalid | availableCopies == 0 AND other conditions allow checkout | 2.0 | No copies to checkout |
| EP 1.2 | Available (1+ copies) | Valid | availableCopies > 0 AND other conditions allow checkout | Success | Book can be checked out |

**Example test cases:** `testBookAvailable()`, `testUnavailableBook()`

---

### Your EP Tables (add as many as needed)

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|--------------|-------|---------------|----------------|-----------------|------------------|
| EP ___ | | | | | |

---

## Part 2: Boundary Value Analysis (BVA)

Important BVA cases may overlap with EP. That is OK. You can reference all relevant EP/BVA coverage in Part 3.

### Example BVA Table: Overdue Count (Threshold: 3)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 1.1 | Below | overdueCount = 0 | Success (depends on other setup) | Below warning threshold |
| BVA 1.2 | Warning High | overdueCount = 2 | 1.0 | Just below reject threshold |
| BVA 1.3 | At | overdueCount = 3 | 4.0 | At rejection boundary |
| BVA 1.4 | Above | overdueCount = 4 | 4.0 | Above rejection boundary |

---

### Your BVA Tables (add more as needed)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA ___ | | | | |

---

## Part 3: Test Cases Designed

List at least **20** test cases you designed based on your EP/BVA analysis.

Each test case should include:
- EP/BVA coverage
- specific inputs / setup
- expected return code
- expected **observable state changes** (if any)

> Do not test console output.

### Test Case Table
At least some of your tests should verify observable state changes, not just return values.

**Checkout0-3 Columns:** Mark each implementation as Pass (✓) or Fail (✗) for this test case. This helps you track which implementations have bugs and will be useful for Part 4 analysis.

| Test ID Name | EP/BVA | Input Description | Expected Return | Expected State Changes | Checkout0 | Checkout1 | Checkout2 | Checkout3 |
|--------------|--------|-------------------|-----------------|------------------------|-----------|-----------|-----------|-----------|
| T1 testUnavailableBook | EP 1.1 | Book unavailable (0 copies), eligible patron | 2.0 | No state change | ✓ | ✓ | ✗ | ✓ |
| T2 testBookAvailable | EP 1.2 | Book available (1+ copies), eligible patron, no warnings normal checkout | 0.0 | Patron map updated; copies of book change | ✗ | ✗ | ✓ | ✓ |

(Add rows until you have at least 20.)

---

## Part 4: Bug Analysis

### Easter Eggs Found
List any easter egg messages you observed:
- 
- 

### Implementation Results

| Implementation | Bugs Found (count) |
|----------------|---------------------|
| Checkout0      | |
| Checkout1      | |
| Checkout2      | |
| Checkout3      | |

### Bugs Discovered
List distinct bugs you identified for each implementation. Each bug must cite at least one test case that revealed it.

**Checkout0:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout1:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout2:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

**Checkout3:**
- Bug 1: [Brief description] — Revealed by: [Test ID]

### Comparative Analysis
Compare the four implementations:
- Which bugs are most critical (cause the worst failures)?
- Which implementation would you use if you had to choose?
- Why? Justify your choice considering bug severity and frequency.

---

## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**

**What was the most challenging aspect of this assignment?**

**How did you decide on your EP and BVA?**

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**


## Step 1: Equivalence Partitioning (EP) & Boundary Value Analysis (BVA)

### EP Table 1 — Patron eligibility (validatePatronEligibility)
| Partition ID | Patron condition | Valid/Invalid | Expected behavior / return |
|---|---|---|---|
| P-EP1 | patron is null | Invalid | return **3.1** |
| P-EP2 | account suspended | Invalid | return **3.0** |
| P-EP3 | overdueCount ≥ 3 | Invalid | return **4.0** |
| P-EP4 | fineBalance ≥ 10.00 | Invalid | return **4.1** |
| P-EP5 | eligible (not null, not suspended, overdueCount < 3, fineBalance < 10.00) | Valid | continue to book checks |

### EP Table 2 — Book null / reference-only
| Partition ID | Book condition | Valid/Invalid | Expected behavior / return |
|---|---|---|---|
| B-EP1 | book is null | Invalid | return **2.1** |
| B-EP2 | book is reference-only | Invalid | return **5.0** |
| B-EP3 | book is non-reference | Valid | continue |

### EP Table 3 — Renewal vs non-renewal
| Partition ID | Condition | Expected behavior / return |
|---|---|---|
| R-EP1 | patron already has this ISBN checked out | return **0.1**; dueDate becomes today + loanPeriodDays; **available copies do not change** |
| R-EP2 | patron does not have ISBN checked out | continue to availability + limit checks |

### EP Table 4 — Availability (non-renewal only)
| Partition ID | Condition | Expected behavior / return |
|---|---|---|
| A-EP1 | availableCopies <= 0 | return **2.0** |
| A-EP2 | availableCopies > 0 | continue |

### EP Table 5 — Max checkout limit (non-renewal only)
Limits: FACULTY=20, STAFF=15, STUDENT=10, PUBLIC=5, CHILD=3

| Partition ID | Condition | Expected behavior / return |
|---|---|---|
| L-EP1 | checkoutCount ≥ maxLimit | return **3.2** |
| L-EP2 | checkoutCount < maxLimit | checkout proceeds |

### EP Table 6 — Success codes after checkout (non-renewal)
Priority: overdue warning (1.0) beats near-limit warning (1.1)

| Partition ID | Condition after checkout | Expected return |
|---|---|---|
| S-EP1 | overdueCount is 1–2 | **1.0** |
| S-EP2 | within 2 of max limit afte


cd /Users/apple/Desktop/Library-students
cat <<'EOF' >> blackbox-testing.md

## Step 1: Equivalence Partitioning (EP) & Boundary Value Analysis (BVA)


## Step 2: Test Case Design (EP/BVA → Test Cases)

| TC ID | Goal | Setup (inputs) | Expected code | Expected state change |
|---|---|---|---:|---|
| TC01 | book null | book=null, eligible patron | 2.1 | no change to patron/books |
| TC02 | patron null priority | book valid, patron=null | 3.1 | no change to book |
| TC03 | suspended priority even if book null | book=null, patron suspended | 3.0 | no change |
| TC04 | overdue>=3 | book valid, patron overdue=3 | 4.0 | no change |
| TC05 | fine>=10 | book valid, patron fine=10.00 | 4.1 | no change |
| TC06 | reference-only | book reference-only, eligible patron | 5.0 | no change |
| TC07 | unavailable | availableCopies=0, eligible patron | 2.0 | no change (copies unchanged, patron list unchanged) |
| TC08 | renewal path | patron already has ISBN; book copies 0 | 0.1 | due date updated; copies unchanged; checkout count unchanged |
| TC09 | normal success | eligible; copies=1; under limit; overdue=0 | 0.0 | copies -1; ISBN added with due date today+loan |
| TC10 | success overdue warning | overdue=1; copies=1; under limit | 1.0 | copies -1; ISBN added |
| TC11 | success overdue warning | overdue=2; copies=1; under limit | 1.0 | copies -1; ISBN added |
| TC12 | near-limit warn (STUDENT) | student count before=7; copies=1; overdue=0 | 1.1 | copies -1; ISBN added |
| TC13 | at max limit (STUDENT) | student count before=10; copies=1 | 3.2 | no change |
| TC14 | fine boundary low | fine=9.99; copies=1; under limit | 0.0 | normal success changes |
| TC15 | overdue boundary low | overdue=2; copies=1; under limit | 1.0 | success + warning |
| TC16 | availability boundary high | copies=1; under limit | 0.0 | normal success |
| TC17 | availability boundary low | copies=0; under limit | 2.0 | no change |
| TC18 | eligibility checked before book null | fine=10.00; book=null | 4.1 | no change |
| TC19 | renewal skips availability | already has ISBN; copies=0 | 0.1 | due updated; copies unchanged |
| TC20 | priority 1.0 over 1.1 | overdue=1 AND near-limit after checkout | 1.0 | checkout happens; warning should be 1.0 |


| TC03 suspended priority | EP | Suspended patron | 3.0 | No state change | ✓ | ✗ | ✓ | ✓ |
| TC04 overdue>=3 | EP | Patron overdue >=3 | 4.0 | No state change | ✓ | ✓ | ✓ | ✓ |
| TC05 fines>=10 | EP | Patron fine >=10 | 4.1 | No state change | ✓ | ✓ | ✓ | ✓ |
| TC06 reference-only | EP | Reference-only book | 5.0 | No checkout occurs | ✗ | ✓ | ✓ | ✓ |
| TC07 unavailable | EP | Book unavailable | 2.0 | No state change | ✓ | ✓ | ✗ | ✓ |
| TC08 renewal | EP | Patron renews same book | 0.1 | Due date updated only | ✗ | ✗ | ✗ | ✗ |
| TC09 normal success | EP | Normal checkout | 0.0 | Copies decrement & patron updated | ✗ | ✗ | ✓ | ✓ |
| TC10 overdue warning | EP | Patron has overdue books | 1.0 | Checkout still succeeds | ✗ | ✗ | ✗ | ✗ |
| TC12 near-limit warning | EP | Patron near checkout limit | 1.1 | Checkout succeeds | ✓ | ✗ | ✗ | ✓ |
| TC13 at max limit | EP | Patron at max limit | 3.2 | No checkout occurs | ✓ | ✗ | ✗ | ✓ |
| TC14 fine 9.99 eligible | BVA | Fine just below limit | 0.0 | Checkout succeeds | ✓ | ✓ | ✓ | ✓ |
| TC15 overdue=2 eligible | BVA | Overdue count below limit | 0.0 | Checkout succeeds | ✓ | ✓ | ✓ | ✓ |
| TC16 copies=1 available | BVA | Last copy available | 0.0 | Checkout succeeds | ✓ | ✓ | ✓ | ✓ |
| TC17 copies=0 unavailable | EP | No copies available | 2.0 | No state change | ✓ | ✓ | ✗ | ✓ |
| TC18 fine priority | EP | Fine>=10 & book null | 4.1 | No state change | ✓ | ✗ | ✓ | ✓ |
| TC19 renewal count | EP | Renewal count check | 0.1 | Count unchanged | ✗ | ✗ | ✗ | ✗ |
| TC20 overdue priority | EP | Overdue overrides near-limit | 1.0 | Checkout succeeds | ✗ | ✗ | ✗ | ✗ |


### Implementation Results

| Implementation | Bugs Found (count) |
|----------------|---------------------|
| Checkout0      | 6 |
| Checkout1      | 9 |
| Checkout2      | 7 |
| Checkout3      | 4 |


### Bugs Discovered

**Checkout0:**
- Bug 1: Reference-only books not handled correctly (should return 5.0). — Revealed by: TC06
- Bug 2: Renewal behavior incorrect (should return 0.1 and not decrement copies). — Revealed by: TC08
- Bug 3: Normal successful checkout/state updates incorrect (should return 0.0 and update copies + patron map). — Revealed by: TC09
- Bug 4: Overdue warning not returned correctly (should return 1.0). — Revealed by: TC10
- Bug 5: Renewal incorrectly affects checkout count (should not increase count). — Revealed by: TC19
- Bug 6: Priority error: overdue warning should override near-limit warning (should return 1.0). — Revealed by: TC20

**Checkout1:**
- Bug 1: Suspended priority incorrect (should return 3.0). — Revealed by: TC03
- Bug 2: Renewal behavior incorrect (should return 0.1 and not decrement copies). — Revealed by: TC08
- Bug 3: Normal successful checkout/state updates incorrect (should return 0.0 and update state). — Revealed by: TC09
- Bug 4: Overdue warning not returned correctly (should return 1.0). — Revealed by: TC10
- Bug 5: Near-limit warning incorrect (should return 1.1). — Revealed by: TC12
- Bug 6: Max checkout limit incorrect (should return 3.2 at limit). — Revealed by: TC13
- Bug 7: Priority error: fine>=10 should return 4.1 even when book is null. — Revealed by: TC18
- Bug 8: Renewal incorrectly affects checkout count (should not increase count). — Revealed by: TC19
- Bug 9: Priority error: overdue warning should override near-limit warning (should return 1.0). — Revealed by: TC20

**Checkout2:**
- Bug 1: Unavailable book handling incorrect (should return 2.0). — Revealed by: TC07 / TC17
- Bug 2: Renewal behavior incorrect (should return 0.1 and not decrement copies). — Revealed by: TC08
- Bug 3: Overdue warning not returned correctly (should return 1.0). — Revealed by: TC10
- Bug 4: Near-limit warning incorrect (should return 1.1). — Revealed by: TC12
- Bug 5: Max checkout limit incorrect (should return 3.2 at limit). — Revealed by: TC13
- Bug 6: Renewal incorrectly affects checkout count (should not increase count). — Revealed by: TC19
- Bug 7: Priority error: overdue warning should override near-limit warning (should return 1.0). — Revealed by: TC20

**Checkout3:**
- Bug 1: Renewal behavior incorrect (should return 0.1 and not decrement copies). — Revealed by: TC08
- Bug 2: Overdue warning not returned correctly (should return 1.0). — Revealed by: TC10
- Bug 3: Renewal incorrectly affects checkout count (should not increase count). — Revealed by: TC19
- Bug 4: Priority error: overdue warning should override near-limit warning (should return 1.0). — Revealed by: TC20


### Comparative Analysis
- Most critical bugs are the ones that block correct business rules or return the wrong priority code (e.g., suspended/fine priority, max-limit handling). These can prevent valid checkouts or allow invalid ones.
- Checkout3 is the best overall choice because it has the fewest failures (4) and passes core eligibility cases (TC03–TC07, TC09, TC12–TC18).
- Checkout1 is the worst overall (9 failures) due to multiple priority/order issues (TC03, TC18, TC20) plus incorrect warning/limit behavior (TC12, TC13) and renewal/count bugs (TC08, TC19).
- If forced to pick one implementation, I would choose Checkout3 because it is closest to the expected behavior and has fewer severe failures compared to the others.


## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**  
Black-box testing was most effective early because it quickly exposed incorrect return codes and priority ordering across implementations without needing to inspect code (e.g., TC03, TC18, TC20). White-box testing was most helpful for improving coverage and making sure internal branches (like null checks and availability filtering) were exercised.

**What was the most challenging aspect of this assignment?**  
Designing test cases that verify observable state changes (copies, patron map, renewal behavior) rather than only checking return values, and making sure tests cover edge cases like priority ordering.

**How did you decide on your EP and BVA?**  
I used EP to cover distinct behavior categories (unavailable vs available, suspended vs eligible, renewal vs normal checkout), and BVA around numeric thresholds like fine amount (9.99 vs 10.00), overdue count (2 vs 3), and inventory copies (1 vs 0).

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**  
TC09 (normal success) requires checking state changes: the book’s available copies must decrement and the patron’s checkedOutBooks map must be updated. A method could return 0.0 but fail to update copies or the patron record, which would be missed if only the return value was asserted.

