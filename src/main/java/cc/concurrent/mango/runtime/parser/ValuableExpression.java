package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public abstract class ValuableExpression extends ValuableNode {

    public ValuableExpression(int i) {
        super(i);
    }

    public ValuableExpression(Parser p, int i) {
        super(p, i);
    }

    abstract Token getFirstToken();

    abstract Token getLastToken();

    protected String literal() {
        Token first = getFirstToken();
        Token last = getLastToken();

        if (first == last) {
            return first.image;
        }

        Token t = first;
        StringBuffer sb = new StringBuffer(t.image);
        while (t != last) {
            t = t.next;
            sb.append(t.image);
        }
        return sb.toString();
    }

}
