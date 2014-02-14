package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.TypeContext;

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

    /**
     * 获得语法块最开始的token
     * @return
     */
    abstract Token getFirstToken();

    /**
     * 获得语法块最末位的token
     * @return
     */
    abstract Token getLastToken();

    /**
     * 语法块字符串
     * @return
     */
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
