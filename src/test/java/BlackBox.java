import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assignment 2 - Task 3: Black-box tests for checkoutBook().
 * Parameterized across Checkout0-Checkout3 (buggy implementations).
 * Some failures are expected and used for bug analysis.
 */
public class BlackBox {

    static Stream<Class<? extends Checkout>> checkoutClassProvider() {
        return Stream.of(
                Checkout0.class,
                Checkout1.class,
                Checkout2.class,
                Checkout3.class
        );
    }

    private Checkout newCheckoutInstance(Class<? extends Checkout> clazz) throws Exception {
        Constructor<? extends Checkout> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
    }

    private Patron makePatron(Patron.PatronType type) {
        return new Patron("P1", "Test Patron", "p1@test.com", type);
    }

    private Book makeBook(String isbn, Book.BookType type, int copies) {
        return new Book(isbn, "Title", "Author", type, copies);
    }

    // -------------------- Core return codes --------------------

    // TC01: book null -> 2.1
    @ParameterizedTest(name = "TC01 book null -> 2.1 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC01: book=null returns 2.1")
    void tc01_bookNull_returns21(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        assertEquals(2.1, c.checkoutBook(null, patron), 0.0001);
    }

    // TC02: patron null -> 3.1
    @ParameterizedTest(name = "TC02 patron null -> 3.1 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC02: patron=null returns 3.1")
    void tc02_patronNull_returns31(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Book book = makeBook("9780123456789", Book.BookType.FICTION, 1);
        assertEquals(3.1, c.checkoutBook(book, null), 0.0001);
    }

    // TC03: suspended -> 3.0 (priority even if book null)
    @ParameterizedTest(name = "TC03 suspended -> 3.0 (priority) ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC03: suspended patron returns 3.0 even if book is null")
    void tc03_suspendedPriority_returns30(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.setAccountSuspended(true);
        assertEquals(3.0, c.checkoutBook(null, patron), 0.0001);
    }

    // TC04: overdue>=3 -> 4.0
    @ParameterizedTest(name = "TC04 overdue>=3 -> 4.0 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC04: overdueCount >= 3 returns 4.0")
    void tc04_overdueAtLeast3_returns40(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.setOverdueCount(3);
        Book book = makeBook("9780000000500", Book.BookType.FICTION, 1);
        assertEquals(4.0, c.checkoutBook(book, patron), 0.0001);
    }

    // TC05: fines>=10 -> 4.1
    @ParameterizedTest(name = "TC05 fines>=10 -> 4.1 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC05: fineBalance >= 10.00 returns 4.1")
    void tc05_finesAtLeast10_returns41(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.addFine(10.00);
        Book book = makeBook("9780000000600", Book.BookType.FICTION, 1);
        assertEquals(4.1, c.checkoutBook(book, patron), 0.0001);
    }

    // TC06: reference-only -> 5.0 (no state change)
    @ParameterizedTest(name = "TC06 reference-only -> 5.0 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC06: reference-only book returns 5.0 and does not modify state")
    void tc06_referenceOnly_returns50_noStateChange(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000010", Book.BookType.REFERENCE, 1);

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);
        assertEquals(5.0, code, 0.0001);

        assertEquals(beforeCopies, book.getAvailableCopies());
        assertEquals(beforeMap, patron.getCheckedOutBooks().size());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // TC07: unavailable -> 2.0 (no state change)
    @ParameterizedTest(name = "TC07 unavailable -> 2.0 and no state change ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC07: unavailable book returns 2.0 and does not modify state")
    void tc07_unavailable_returns20_noStateChange(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000001", Book.BookType.FICTION, 0);

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);

        assertEquals(2.0, code, 0.0001);
        assertEquals(beforeCopies, book.getAvailableCopies());
        assertEquals(beforeMap, patron.getCheckedOutBooks().size());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // TC08: renewal -> 0.1 (due date updated, copies unchanged)
    @ParameterizedTest(name = "TC08 renewal -> 0.1 and no copy change ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC08: renewal updates due date only (skips availability)")
    void tc08_renewal_updatesDueDate_only(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000003", Book.BookType.FICTION, 0);

        patron.getCheckedOutBooks().put(book.getIsbn(), LocalDate.now().minusDays(1));

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);

        assertEquals(0.1, code, 0.0001);
        assertEquals(beforeCopies, book.getAvailableCopies());
        assertEquals(beforeMap, patron.getCheckedOutBooks().size());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()));
        assertEquals(LocalDate.now().plusDays(patron.getLoanPeriodDays()),
                patron.getCheckedOutBooks().get(book.getIsbn()));
    }

    // TC09: normal success -> 0.0 and state updates
    @ParameterizedTest(name = "TC09 normal success -> 0.0 with state updates ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC09: successful checkout decrements copies and adds to patron")
    void tc09_success_updatesState(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000002", Book.BookType.FICTION, 1);

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);

        assertEquals(0.0, code, 0.0001);
        assertEquals(beforeCopies - 1, book.getAvailableCopies());
        assertEquals(beforeMap + 1, patron.getCheckedOutBooks().size());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()));
        assertEquals(LocalDate.now().plusDays(patron.getLoanPeriodDays()),
                patron.getCheckedOutBooks().get(book.getIsbn()));
    }

    // TC10: overdue warning -> 1.0
    @ParameterizedTest(name = "TC10 overdue warning -> 1.0 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC10: overdueCount=1 returns 1.0 warning")
    void tc10_overdueWarning_returns10(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);

        // create one overdue item
        patron.getCheckedOutBooks().put("OVERDUE-SEED", LocalDate.now().minusDays(3));

        Book book = makeBook("9780000000100", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertEquals(1.0, code, 0.0001);
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // TC12: near-limit warning -> 1.1 (student: 7->8)
    @ParameterizedTest(name = "TC12 near-limit warning -> 1.1 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC12: student count 7->8 returns 1.1 near-limit warning")
    void tc12_nearLimitWarning_returns11(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);

        for (int i = 0; i < 7; i++) {
            patron.getCheckedOutBooks().put("ISBN-SEED-" + i, LocalDate.now().plusDays(10));
        }

        Book book = makeBook("9780000000200", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertEquals(1.1, code, 0.0001);
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // TC13: at max limit -> 3.2 (student: 10)
    @ParameterizedTest(name = "TC13 at max limit -> 3.2 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC13: student at max limit returns 3.2 and no state change")
    void tc13_atMaxLimit_returns32(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);

        for (int i = 0; i < 10; i++) {
            patron.getCheckedOutBooks().put("ISBN-LIMIT-" + i, LocalDate.now().plusDays(10));
        }

        Book book = makeBook("9780000000300", Book.BookType.FICTION, 1);

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);
        assertEquals(3.2, code, 0.0001);

        assertEquals(beforeCopies, book.getAvailableCopies());
        assertEquals(beforeMap, patron.getCheckedOutBooks().size());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // TC14: fine boundary low (9.99) does NOT return 4.1
    @ParameterizedTest(name = "TC14 fine 9.99 eligible (not 4.1) ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC14: fineBalance 9.99 does not block checkout (not 4.1)")
    void tc14_fineBoundaryLow_not41(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.addFine(9.99);
        Book book = makeBook("9780000000700", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertNotEquals(4.1, code, 0.0001);
    }

    // TC15: overdue boundary low (2) does NOT return 4.0
    @ParameterizedTest(name = "TC15 overdue=2 eligible (not 4.0) ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC15: overdueCount=2 does not block checkout (not 4.0)")
    void tc15_overdueBoundaryLow_not40(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.setOverdueCount(2);
        Book book = makeBook("9780000000800", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertNotEquals(4.0, code, 0.0001);
    }

    // TC16: availability boundary high (1) does NOT return 2.0
    @ParameterizedTest(name = "TC16 copies=1 not unavailable (not 2.0) ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC16: availableCopies=1 allows checkout (not 2.0)")
    void tc16_availabilityBoundaryHigh_not20(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000900", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertNotEquals(2.0, code, 0.0001);
    }

    // TC17: availability boundary low (0) returns 2.0
    @ParameterizedTest(name = "TC17 copies=0 -> 2.0 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC17: availableCopies=0 returns 2.0")
    void tc17_availabilityBoundaryLow_returns20(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000000910", Book.BookType.FICTION, 0);

        assertEquals(2.0, c.checkoutBook(book, patron), 0.0001);
    }

    // TC18: priority - fine>=10 checked before book null -> 4.1
    @ParameterizedTest(name = "TC18 fine>=10 before book null -> 4.1 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC18: fine>=10 returns 4.1 even when book is null (priority)")
    void tc18_priority_finesBeforeBookNull_returns41(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.addFine(10.00);

        assertEquals(4.1, c.checkoutBook(null, patron), 0.0001);
    }

    // TC19: renewal does not change count
    @ParameterizedTest(name = "TC19 renewal does not increase checkout count ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC19: renewal does not increase patron checkout count")
    void tc19_renewal_doesNotIncreaseCount(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        Book book = makeBook("9780000001200", Book.BookType.FICTION, 0);

        patron.getCheckedOutBooks().put(book.getIsbn(), LocalDate.now().plusDays(1));
        int beforeCount = patron.getCheckoutCount();

        double code = c.checkoutBook(book, patron);
        assertEquals(0.1, code, 0.0001);
        assertEquals(beforeCount, patron.getCheckoutCount());
    }

    // TC20: priority - overdue warning beats near-limit warning -> 1.0
    @ParameterizedTest(name = "TC20 overdue priority over near-limit -> 1.0 ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("TC20: overdue warning (1.0) beats near-limit warning (1.1)")
    void tc20_priority_overdueBeatsNearLimit_returns10(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);

        // overdue
        patron.getCheckedOutBooks().put("OVERDUE-1", LocalDate.now().minusDays(3));
        // near-limit setup: add 7 more non-overdue
        for (int i = 0; i < 7; i++) {
            patron.getCheckedOutBooks().put("ISBN-NL-" + i, LocalDate.now().plusDays(10));
        }

        Book book = makeBook("9780000000400", Book.BookType.FICTION, 1);

        double code = c.checkoutBook(book, patron);
        assertEquals(1.0, code, 0.0001);
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()));
    }

    // Extra: eligibility failure should not change state (example: suspended)
    @ParameterizedTest(name = "Extra: eligibility failure no state change ({0})")
    @MethodSource("checkoutClassProvider")
    @DisplayName("Extra: eligibility failures do not modify patron list or book copies")
    void extra_eligibilityFailure_noStateChange(Class<? extends Checkout> clazz) throws Exception {
        Checkout c = newCheckoutInstance(clazz);
        Patron patron = makePatron(Patron.PatronType.STUDENT);
        patron.setAccountSuspended(true);

        Book book = makeBook("9780000000999", Book.BookType.FICTION, 2);

        int beforeCopies = book.getAvailableCopies();
        int beforeMap = patron.getCheckedOutBooks().size();

        double code = c.checkoutBook(book, patron);
        assertEquals(3.0, code, 0.0001);

        assertEquals(beforeCopies, book.getAvailableCopies());
        assertEquals(beforeMap, patron.getCheckedOutBooks().size());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()));
    }
}
