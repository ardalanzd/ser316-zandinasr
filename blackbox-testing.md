# Black Box Testing Report - Assignment 2

**Student Name:** Ardalan Zandina
**ASU ID:** azandina
**Date:** June 2026

---

## Part 1: Equivalence Partitioning (EP)

I broke down the checkoutBook() method into logical categories based on what the JavaDoc says. Each table covers one area where the method behaves differently.

### EP Table 1 — Patron Eligibility

This one was straightforward - the spec says to check the patron first before anything else.

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|---|---|---|---|---|---|
| P-EP1 | Patron is null | Invalid | patron == null | 3.1 | Should stop right away, nothing changes |
| P-EP2 | Account is suspended | Invalid | patron.isAccountSuspended() == true | 3.0 | Stop right away, nothing changes |
| P-EP3 | Too many overdue books | Invalid | patron.getOverdueCount() >= 3 | 4.0 | Stop right away, nothing changes |
| P-EP4 | Fine balance too high | Invalid | patron.getFineBalance() >= 10.00 | 4.1 | Stop right away, nothing changes |
| P-EP5 | Patron is fully eligible | Valid | not null, not suspended, overdue < 3, fines < 10 | continue | Move on to checking the book |

### EP Table 2 — Book State

After patron is verified, the method checks the book itself.

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|---|---|---|---|---|---|
| B-EP1 | Book is null | Invalid | book == null | 2.1 | Stop, nothing changes |
| B-EP2 | Book is reference only | Invalid | book.getType() == REFERENCE | 5.0 | Can't check out reference books, nothing changes |
| B-EP3 | Book has no copies left | Invalid | book.getAvailableCopies() <= 0 | 2.0 | No copies available, nothing changes |
| B-EP4 | Book is available | Valid | book.getAvailableCopies() > 0 | continue | Move on to limit checks |

### EP Table 3 — Renewal vs New Checkout

This one was tricky - if the patron already has the book, it's a renewal and skips availability check.

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|---|---|---|---|---|---|
| R-EP1 | Renewal | Valid | patron already has this ISBN in their checked out list | 0.1 | Just update the due date, copies stay the same |
| R-EP2 | New checkout | Valid | patron doesn't have this ISBN | continue | Check availability and limits |

### EP Table 4 — Checkout Limit

Each patron type has a different max limit.

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|---|---|---|---|---|---|
| L-EP1 | At or over max limit | Invalid | checkoutCount >= maxLimit | 3.2 | Can't check out more, nothing changes |
| L-EP2 | Within limit | Valid | checkoutCount < maxLimit | continue | Proceed with checkout |
| L-EP3 | Close to limit (within 2) | Valid | after checkout count >= maxLimit - 1 | 1.1 | Checkout works but with a warning |

### EP Table 5 — Success Codes

Once checkout actually happens, the return code depends on warnings.

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|---|---|---|---|---|---|
| S-EP1 | Has 1-2 overdue books | Valid | overdueCount is 1 or 2 | 1.0 | Checkout works but overdue warning |
| S-EP2 | Close to checkout limit | Valid | within 2 of max after this checkout | 1.1 | Checkout works but limit warning |
| S-EP3 | No warnings | Valid | no overdue issues, not near limit | 0.0 | Clean successful checkout |

---

## Part 2: Boundary Value Analysis (BVA)

I focused on the numeric thresholds in the spec since those are where bugs tend to hide.

### BVA Table 1 — Overdue Count (the threshold is 3)

| Test ID | Boundary | Input Value | Expected Return | Why I picked this value |
|---|---|---|---|---|
| BVA-OD1 | Well below | overdueCount = 0 | 0.0 | Normal case, no issues |
| BVA-OD2 | Just entered warning | overdueCount = 1 | 1.0 | First value that triggers warning |
| BVA-OD3 | Just below rejection | overdueCount = 2 | 1.0 | One below the cutoff |
| BVA-OD4 | At rejection | overdueCount = 3 | 4.0 | Exactly at the threshold |
| BVA-OD5 | Above rejection | overdueCount = 4 | 4.0 | Clearly over the limit |

### BVA Table 2 — Fine Balance (threshold is $10.00)

| Test ID | Boundary | Input Value | Expected Return | Why I picked this value |
|---|---|---|---|---|
| BVA-F1 | No fines | fineBalance = 0.00 | 0.0 | Clean slate |
| BVA-F2 | Just below cutoff | fineBalance = 9.99 | 0.0 | One cent below, should still be allowed |
| BVA-F3 | Exactly at cutoff | fineBalance = 10.00 | 4.1 | This is the boundary |
| BVA-F4 | Over cutoff | fineBalance = 15.00 | 4.1 | Clearly blocked |

### BVA Table 3 — Available Copies (threshold is 0)

| Test ID | Boundary | Input Value | Expected Return | Why I picked this value |
|---|---|---|---|---|
| BVA-AV1 | No copies | copies = 0 | 2.0 | Should be rejected |
| BVA-AV2 | Minimum available | copies = 1 | 0.0 | Just barely available |
| BVA-AV3 | Clearly available | copies = 3 | 0.0 | Normal case |

### BVA Table 4 — Checkout Limit for STUDENT (max is 10)

| Test ID | Boundary | Input Value | Expected Return | Why I picked this value |
|---|---|---|---|---|
| BVA-L1 | Normal range | count = 6 going to 7 | 0.0 | Not near limit yet |
| BVA-L2 | Just entered near-limit | count = 7 going to 8 | 1.1 | Two away from limit |
| BVA-L3 | Still near limit | count = 8 going to 9 | 1.1 | One away from limit |
| BVA-L4 | At limit after checkout | count = 9 going to 10 | 1.1 | Hits limit with this checkout |
| BVA-L5 | Already at limit | count = 10 | 3.2 | Can't check out anything |

---

## Part 3: Test Cases Designed

I designed 20 test cases based on the EP and BVA tables above. Some of them also verify state changes, not just the return code.

| Test ID | EP/BVA | Input Description | Expected Return | Expected State Changes | Checkout0 | Checkout1 | Checkout2 | Checkout3 |
|---|---|---|---|---|---|---|---|---|
| TC01 | B-EP1 | book=null, eligible STUDENT patron | 2.1 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC02 | P-EP1 | valid book, patron=null | 3.1 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC03 | P-EP2 | book=null, patron is suspended | 3.0 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC04 | P-EP3, BVA-OD4 | valid book, overdueCount=3 | 4.0 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC05 | P-EP4, BVA-F3 | valid book, fineBalance=10.00 | 4.1 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC06 | B-EP2 | REFERENCE book, eligible patron | 5.0 | Copies and patron list unchanged | ✓ | ✓ | ✓ | ✓ |
| TC07 | B-EP3, BVA-AV1 | copies=0, eligible patron | 2.0 | Copies and patron list unchanged | ✓ | ✓ | ✗ | ✓ |
| TC08 | R-EP1 | patron already has ISBN; book has 0 copies | 0.1 | Due date updated to today+loanDays; copies unchanged; count unchanged | ✗ | ✗ | ✓ | ✓ |
| TC09 | S-EP3, BVA-AV2 | eligible patron; copies=1; overdue=0; under limit | 0.0 | copies go down by 1; ISBN added to patron map with correct due date | ✗ | ✗ | ✓ | ✓ |
| TC10 | S-EP1, BVA-OD2 | overdueCount=1; copies=1; under limit | 1.0 | copies -1; ISBN added to patron | ✗ | ✗ | ✓ | ✓ |
| TC11 | S-EP1, BVA-OD3 | overdueCount=2; copies=1; under limit | 1.0 | copies -1; ISBN added to patron | ✗ | ✗ | ✓ | ✓ |
| TC12 | S-EP2, BVA-L2 | STUDENT has 7 books going to 8; copies=1; overdue=0 | 1.1 | copies -1; ISBN added to patron | ✗ | ✗ | ✓ | ✓ |
| TC13 | L-EP1, BVA-L5 | STUDENT already has 10 books; copies=1 | 3.2 | Nothing changes | ✓ | ✓ | ✗ | ✓ |
| TC14 | BVA-F2 | fineBalance=9.99; copies=1; under limit | 0.0 | Normal checkout changes happen | ✗ | ✗ | ✓ | ✓ |
| TC15 | BVA-OD3 | overdueCount=2; copies=1 | 1.0 | copies -1; ISBN added | ✗ | ✗ | ✓ | ✓ |
| TC16 | BVA-AV2 | copies=1; eligible patron | not 2.0 | Normal checkout changes | ✗ | ✗ | ✓ | ✓ |
| TC17 | BVA-AV1 | copies=0; eligible patron | 2.0 | Nothing changes | ✓ | ✓ | ✗ | ✓ |
| TC18 | P-EP4 priority | fineBalance=10.00; book=null | 4.1 | Nothing changes | ✓ | ✓ | ✓ | ✓ |
| TC19 | R-EP1 | patron already has ISBN; copies=0 | 0.1 | Count unchanged; due date updated | ✗ | ✗ | ✓ | ✓ |
| TC20 | S-EP1 priority | overdue=1 AND near limit setup | 1.0 | Overdue warning wins over near-limit warning | ✗ | ✗ | ✓ | ✓ |

---

## Part 4: Bug Analysis

### Easter Eggs Found
- Checkout2 prints something when an unavailable book gets checked out — it was returning 0.0 instead of 2.0 which triggered the hidden print
- Checkout0 and Checkout1 seem to just return 0.0 for everything so it's hard to tell if they have easter eggs or not

### Implementation Results

| Implementation | Bugs Found |
|---|---|
| Checkout0 | 3 |
| Checkout1 | 3 |
| Checkout2 | 2 |
| Checkout3 | 0 |

### Bugs Discovered

**Checkout0:**
- Bug 1: The whole method is basically a stub — it just returns 0.0 for everything. TC09 catches this because the book copies never go down and the patron's list never gets updated even though it returned success
- Bug 2: Renewals don't work at all — TC08 fails because it returns 0.0 instead of 0.1 and the due date never gets updated
- Bug 3: Warning codes 1.0 and 1.1 are never returned — TC10 and TC12 both fail

**Checkout1:**
- Bug 1: Same stub problem as Checkout0 — TC09 fails the same way, no state changes happen
- Bug 2: Renewal handling is broken — TC08 fails, returns wrong code and due date unchanged
- Bug 3: Missing warning codes — TC10 and TC12 fail

**Checkout2:**
- Bug 1: Unavailable books don't get rejected properly — TC07 and TC17 catch this because copies=0 should return 2.0 but it returns 0.0 instead and actually processes the checkout
- Bug 2: Max checkout limit isn't checked — TC13 fails because a student with 10 books can still check out more

**Checkout3:**
- No bugs found. Every test passed including state change verification.

### Comparative Analysis

Checkout0 and Checkout1 are basically unusable because they're stubs. Every single checkout returns 0.0 no matter what the situation is. That means suspended patrons, null patrons, reference books, everything just gets "approved" with no actual changes happening. In a real library this would be a disaster.

Checkout2 is closer to correct but has two significant bugs. The availability check bug is pretty serious because patrons could check out books that have no copies left, which corrupts the inventory. The missing limit check is also a real problem because patrons could accumulate unlimited books.

Checkout3 is the only one I would actually use. It handles every case correctly including the tricky ones like renewal skipping the availability check and the priority order of warning codes.

---

## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**
BVA was more useful for finding the subtle bugs. The fineBalance=9.99 vs 10.00 test and the copies=0 vs 1 test were the ones that really exposed what was wrong with Checkout2. EP alone would have caught the big obvious failures but BVA found the boundary problems.

**What was the most challenging aspect of this assignment?**
Getting the validation order right was the hardest part. The spec is detailed about what order things get checked and I had to read it several times to make sure my test cases matched. For example TC18 checks that fine>=10 blocks the checkout even when the book is null, which tests that patron eligibility comes before book null check.

**How did you decide on your EP and BVA?**
I went through the JavaDoc line by line and every time I saw a condition or a threshold I wrote it down as a potential partition or boundary. The return codes helped too because each different return code represents a different behavior which basically gives you the partitions for free.

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**
TC09 is the best example of this. Checkout0 returns 0.0 which looks like a successful checkout, but when you actually check the state afterwards the book copies are unchanged and the patron's list is empty. If we only looked at the return code we would think the checkout worked when it actually did nothing at all. The bug was completely invisible without checking state changes.
