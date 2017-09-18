package com.martiansoftware.bookmartian.query;

import com.martiansoftware.bookmartian.model.TagName;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author mlamb
 */
class QueryTermParser {

    private static final String DEFAULT_ACTION = "tagged";
    public static final char QUOTE = '"';
    public static final char ACTION_ARG_SEPARATOR = ':';
    
    private enum STATE { DELIM, ACTION, PRE_ARG, QUOTED_ARG, UNQUOTED_ARG, ONE_QUOTE };
    
    private final StringBuffer _action = new StringBuffer();
    private final StringBuffer _arg = new StringBuffer();
    private boolean _negateAction = false;

    private STATE _state;
    private int _pos;
    private List<QueryTerm> _queryTerms;
    
    public QueryTermParser() { 
        reset();
    }

    public List<QueryTerm> parse(String charSequence) {
        if (charSequence != null) {
            IntStream.concat(charSequence.chars(), IntStream.of(-1)).forEach(this::handleChar);
        }
        List<QueryTerm> result = new java.util.ArrayList<>(_queryTerms);
        reset();
        return result;
    }
    
    private void reset() {
        _state = STATE.DELIM;
        _pos = -1;
        _action.setLength(0);
        _arg.setLength(0);
        _negateAction = false;
        _queryTerms = new java.util.ArrayList<>();
    }
    
    private void handleChar(int c) {
        ++_pos;
        _state = stateMachine(c);
    }

    private STATE handleAs(STATE state, int c) {
        _state = state;
        return stateMachine(c);
    }
    
    private STATE stateMachine(int c) {
        switch(_state) {
        
            case DELIM:     if (isEof(c) || isDelimiter(c)) return STATE.DELIM;
                            if (isActionNegatorChar(c) || isActionChar(c)) return handleAs(STATE.ACTION, c);
                            return unexpected(c);

            case ACTION:    if (isActionNegatorChar(c) && _action.length() == 0) {
                                _negateAction = true;
                                return STATE.ACTION;
                            }
                            if (isActionChar(c)) {
                                _action.append((char) c);
                                return STATE.ACTION;
                            }
                            if (isEof(c) || isDelimiter(c)) {
                                emit(DEFAULT_ACTION, _action.toString(), _negateAction);
                                return STATE.DELIM;
                            }
                            if (isActionArgSeparator(c)) return STATE.PRE_ARG;
                            return unexpected(c);

            case PRE_ARG:   if (isEof(c) || isDelimiter(c)) return unexpected(c);
                            if (isQuote(c)) return STATE.QUOTED_ARG;
                            return handleAs(STATE.UNQUOTED_ARG, c);
                            
            case UNQUOTED_ARG:  if (isEof(c) || isDelimiter(c)) return emit();
                                _arg.append((char) c);
                                return STATE.UNQUOTED_ARG;
                                
            case QUOTED_ARG:    if (isEof(c)) return unexpected(c);
                                if (isQuote(c)) return STATE.ONE_QUOTE;
                                _arg.append((char) c);
                                return STATE.QUOTED_ARG;
                                
            case ONE_QUOTE:     if (isEof(c) || isDelimiter(c)) return emit();
                                if (isQuote(c)) {
                                    _arg.append((char) c);
                                    return STATE.QUOTED_ARG;
                                }
                                return unexpected(c);
        }
        return oops("fell off the end of the state machine!");
    }
    
    private boolean isEof(int c) { return c == -1; }
    static boolean isDelimiter(int c) { return Character.isWhitespace(c) || c == ','; }
    private boolean isActionChar(int c) { return TagName.isTagNameCharacter(c); }
    private boolean isActionArgSeparator(int c) { return c == ACTION_ARG_SEPARATOR; }
    private boolean isActionNegatorChar(int c) { return c == '!'; }
    static boolean isQuote(int c) { return c == QUOTE; }

    private void emit(String action, String arg, boolean negateAction) {
        _queryTerms.add(QueryTerm.of(action, arg, negateAction));
        _action.setLength(0);
        _arg.setLength(0);
        _negateAction = false;
    }

    private STATE emit() {
        emit(_action.toString(), _arg.toString(), _negateAction);
        return STATE.DELIM;
    }
        
    private <T> T oops(String fmt, Object... args) {
        throw new QueryTermParseException(_pos, String.format(fmt, args));
    }
    
    private STATE unexpected(int c) {
        if (isEof(c)) {
            return oops("unexpected end of query at position %d", _pos);                    
        } else {
            return oops("unexpected char '%c' at position %d", c, _pos);        
        }
    }
    
    class QueryTermParseException extends RuntimeException {
        private final int _pos;
        private QueryTermParseException(int pos, String s) {
            super(s);
            _pos = pos;
        }
        public int position() { return _pos; }
    }
    
    public static void main(String[] args) {
        QueryTermParser qtp = new QueryTermParser();
        //         012345678901234567890123456789
//        qtp.parse(" something:else a  b     cde ");
        qtp.parse("a b c  d:efthi");
        //         0123456789012345678901234567890123456789
//        qtp.parse("as:\"my awesome thing!\" by:url  \"bok");
        qtp.parse("   \"");
        
//        qtp.parse(":abc :");
        
    }
}
