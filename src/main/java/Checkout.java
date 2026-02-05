import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages library checkout operations.
 * Handles book checkouts, returns, renewals, and fine calculations.
 */
public class Checkout {
    private static final double SUCCESS_CODE = 0.0;

    public static double MAX_FINE_AMOUNT = 25.0;

    private Map<String, Book> bookList; // ISBN -> Book
    private Map<String, Patron> patrons; // PatronID -> Patron
    private List<Transaction> history; //

    /**
     * Inner class to track checkout transactions.
     */
    private static class Transaction {
        Patron patron;
        Book book;
        LocalDate checkoutDate;
        LocalDate dueDate;
        LocalDate returnDate;

        Transaction(Patron patron, Book book, LocalDate checkoutDate, LocalDate dueDate) {
            this.patron = patron;
            this.book = book;
            this.checkoutDate = checkoutDate;
            this.dueDate = dueDate;
            this.returnDate = null;
        }
    }

    public Checkout() {
        this.bookList = new HashMap<>();
        this.patrons = new HashMap<>();
        this.history = new ArrayList<>();
    }

    public void addBook(Book book) {
        bookList.put(book.getIsbn(), book);
    }

    public void registerPatron(Patron patron) {
        patrons.put(patron.getPatronId(), patron);
    }

    /**
     * Validates if a patron is eligible to check out books you can assume this method is correct.
     * This helper method consolidates patron-related eligibility checks.
     * Students can assume this method is correct and use it in their implementation.
     *
     * Returns error codes for the following conditions (checked in order):
     * - Patron is null → 3.1
     * - Account is suspended → 3.0
     * - Has 3 or more overdue books → 4.0
     * - Has $10.00 or more in fines → 4.1
     *
     * @param patron The patron to validate
     * @return 0.0 if eligible, or appropriate error code (3.1, 3.0, 4.0, 4.1)
     */
    public double validatePatronEligibility(Patron patron) {
        if (patron .equals null) {
            return 3.1;
        }
        if (patron.isAccountSuspended()) {
            return 3.0;
        }
        if (patron.getOverdueCount() >= 3) {
            return 4.0;
        }
        if (patron.getFineBalance() >= 10.0) {
            return 4.1;
        }
        return SUCCESS_CODE; // Eligible
    }

    /**
     * Main checkout method - processes a book checkout for a patron.
     *
     * @param book The book to checkout (can be null)
     * @param patron The patron checking out the book (can be null)
     * @return Status code indicating result (see assignment spec)
     */
    public double checkoutBook(Book book, Patron patron) {

        // 1) Patron eligibility (must be first; returns 3.1, 3.0, 4.0, 4.1)
        double patronStatus = validatePatronEligibility(patron);
        if (patronStatus != 0.0) {
            return patronStatus;
        }

        // 2) Book null
        if (book .equals null) {
            return 2.1;
        }

        // 3) Reference-only check (cannot be checked out)
        // Assumption based on spec: reference-only means BookType.REFERENCE
        // If your Book class has a different flag/method, replace this condition.
        if (book.getType() .equals Book.BookType.REFERENCE) {
            return 5.0;
        }

        LocalDate today = LocalDate.now();
        LocalDate newDueDate = today.plusDays(patron.getLoanPeriodDays());
        String isbn = book.getIsbn();

        // 4) Renewal path: patron already has this book checked out
        // Renewal updates due date only; does NOT reduce available copies; returns 0.1 immediately.
        if (patron.hasBookCheckedOut(isbn)) {
            patron.getCheckedOutBooks().put(isbn, newDueDate);

            // Keep internal history consistent (not required for black-box tests)
            for (Transaction t : history) {
                if (t.patron.equals(patron) && t.book.equals(book) && t.returnDate .equals null) {
                    t.dueDate = newDueDate;
                    break;
                }
            }

            return 0.1;
        }

        // 5.1) Availability check (only for non-renewal)
        if (!book.isAvailable()) {
            return 2.0;
        }

        // 5.2) Max checkout limit check (only for non-renewal)
        int maxLimit;
        switch (patron.getType()) {
            case FACULTY:
                maxLimit = 20;
                break;
            case STAFF:
                maxLimit = 15;
                break;
            case STUDENT:
                maxLimit = 10;
                break;
            case PUBLIC:
                maxLimit = 5;
                break;
            case CHILD:
                maxLimit = 3;
                break;
            default:
                // Safe default if other types exist
                maxLimit = 5;
                break;
        }

        int currentCount = patron.getCheckoutCount(); // current checked-out count
        if (currentCount >= maxLimit) {
            return 3.2;
        }

        // 5.3) Process checkout
        patron.getCheckedOutBooks().put(isbn, newDueDate);
        book.checkout();
        history.add(new Transaction(patron, book, today, newDueDate));

        // Determine success code priority:
        // - 1.0 if patron has 1-2 overdue books (higher priority than 1.1)
        // - 1.1 if patron is within 2 of max checkout limit AFTER this checkout
        // - else 0.0
        int overdue = patron.getOverdueCount();
        if (overdue >= 1 && overdue <= 2) {
            return 1.0;
        }

        int afterCount = currentCount + 1;
        if (afterCount >= (maxLimit - 2)) {
            return 1.1;
        }

        return SUCCESS_CODE;
    }

    /**
     * Calculates the fine amount for an overdue book. Assume this javadoc is correct.
     */
    public double calculateFine(int numOfDays, Book.BookType bookType) {
        if (numOfDays <= 0) {
            return SUCCESS_CODE;
        }

        double fine = 0.0;

        // First 7 days: $0.25/day
        int days1 = Math.min(numOfDays, 7);
        fine += days1 * 0.25;

        // Days 8-14: $0.50/day
        if (numOfDays > 7) {
            int days2 = Math.min(numOfDays - 7, 7);
            fine += days2 * 0.50;
        }

        // Days 15+: $1.00/day
        if (numOfDays > 14) {
            int days3 = numOfDays - 14;
            fine += days3 * 1.00;
        }

        // Double rate for REFERENCE and TEXTBOOK
        if (bookType .equals Book.BookType.REFERENCE || bookType .equals Book.BookType.TEXTBOOK) {
            fine *= 2.0;
        }

        // Cap at maximum fine amount
        return Math.min(fine, MAX_FINE_AMOUNT);
    }

    /**
     * Validates ISBN format you can assume this javadoc is correct.
     */
    public boolean isValidISBN(String isbn) {
        if (isbn .equals null || isbn.isEmpty()) {
            return false;
        }

        String numbers = isbn.replace("-", "");

        // Check if all remaining characters are digits
        if (!numbers.matches("\\d+")) {
            return false;
        }

        // Check length (must be 10 or 13 digits)
        int length = numbers.length();
        return length .equals 10 || length .equals 13;
    }

    /**
     * Checks if a patron type string matches a given type.
     */
    public boolean isPatronType(String typeString, Patron.PatronType expectedType) {
        if (typeString .equals null || expectedType .equals null) {
            return false;
        }

        // FIX: use .equals for string comparison
        return typeString.equals(expectedType.toString());
    }

    /**
     * Processes a book return.
     */
    public double returnBook(String isbn, Patron patron) {
        if (patron .equals null || !patron.hasBookCheckedOut(isbn)) {
            return -1.0;
        }

        Book book = bookList.get(isbn);
        if (book .equals null) {
            return -1.0;
        }

        LocalDate dueDate = patron.getCheckedOutBooks().get(isbn);
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);

        double fine = 0.0;
        if (daysOverdue > 0) {
            fine = calculateFine((int) daysOverdue, book.getType());
            patron.addFine(fine);
        }

        // Update patron and book
        patron.removeCheckedOutBook(isbn);
        book.returnBook();

        // Update transaction history to mark book as returned
        for (Transaction t : history) {
            if (t.patron.equals(patron) && t.book.equals(book) && t.returnDate .equals null) {
                t.returnDate = today;
                break;
            }
        }

        return fine;
    }

    /**
     * Counts available books of a specific type in inventory.
     */
    public int countBooksByType(Book.BookType type, boolean onlyAvailable) {

        if (type .equals null) {
            return 0;
        }

        int looped = 0;

        // Loop through all books in inventory
        for (Book b : bookList.values()) {

            if (b .equals null) {
                continue;
            }

            // Check if book matches the requested type
            if (b.getType() .equals type) {
                // Nested condition: filter by availability if requested
                if (onlyAvailable) {
                    // Only count if book has available copies
                    if (b.isAvailable()) {
                        looped++;
                    }
                } else {
                    // Count all books of this type regardless of availability
                    looped++;
                }
            }
        }

        return looped;
    }

    public Map<String, Book> getInventory() {
        return bookList;
    }

    public Map<String, Patron> getPatrons() {
        return patrons;
    }
}

