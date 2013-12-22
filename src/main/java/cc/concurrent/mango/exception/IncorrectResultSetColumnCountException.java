package cc.concurrent.mango.exception;

/**
 * @author ash
 */
public class IncorrectResultSetColumnCountException extends DataAccessException {

    private int expectedCount;

    private int actualCount;


    public IncorrectResultSetColumnCountException(int expectedCount, int actualCount) {
        super("Incorrect column count: expected " + expectedCount + ", actual " + actualCount);
        this.expectedCount = expectedCount;
        this.actualCount = actualCount;
    }

    public int getExpectedCount() {
        return this.expectedCount;
    }

    public int getActualCount() {
        return this.actualCount;
    }


}
