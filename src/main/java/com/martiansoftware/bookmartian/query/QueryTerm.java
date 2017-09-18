package com.martiansoftware.bookmartian.query;

import com.martiansoftware.util.Strings;
import com.martiansoftware.validation.Hope;
import java.util.regex.Pattern;

/**
 * an individual "query term", consisting of an "action" and an "argument"
 * 
 * @author mlamb
 */
    // an individual "query term", consisting of an optional "action:" and an "argument"
    // for example, "is:untagged" has an action of "is" and an argument of "untagged"
    // if no action is specified, a default of "tagged" is used.
    class QueryTerm {
        private final String _action, _arg;
        private final boolean _negateAction; // if true, result should be "negated" (logical NOT) however appropriate, or else this is an error.
        
        private QueryTerm(String action, String argument, boolean negateAction) {
            _action = Hope.that(action).named("action").isNotNullOrEmpty().map(s -> Strings.lower(s)).value();
            _arg = Hope.that(Strings.safeTrimToNull(argument)).named("argument").isNotNullOrEmpty().value();
            _negateAction = negateAction;
        }
        
        static QueryTerm of(String action, String arg, boolean negateAction) {
            return new QueryTerm(action, arg, negateAction);
        }
        public String action() { return _action; }
        public String arg() { return _arg; }
        public boolean isNegated() { return _negateAction; }
        
        private String maybeQuote(String s) {
            boolean needsQuotes = false;
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (QueryTermParser.isQuote(c)) {
                    sb.append(QueryTermParser.QUOTE);
                    sb.append(QueryTermParser.QUOTE);
                    needsQuotes = true;
                } else {
                    sb.append(c);
                    if (QueryTermParser.isDelimiter(c)) needsQuotes = true;
                }
            }
            return needsQuotes ? String.format("\"%s\"", sb.toString()) : sb.toString();
        }
        
        @Override
        public String toString() {
            return String.format("%s%s%c%s", isNegated() ? "!" : "", action(), QueryTermParser.ACTION_ARG_SEPARATOR, maybeQuote(arg()));
        }
    }
