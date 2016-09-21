/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.query;

import com.martiansoftware.util.Check;
import com.martiansoftware.util.Strings;
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
        private QueryTerm(String action, String argument) {
            _action = Strings.lower(Check.arg(action, "action").notNullOrEmpty().value());
            _arg = Check.arg(Strings.safeTrimToNull(argument), "argument").notNullOrEmpty().value();
        }
        static QueryTerm of(String action, String arg) {
            return new QueryTerm(action, arg);
        }
        public String action() { return _action; }
        public String arg() { return _arg; }
        
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
            return String.format("%s%c%s", action(), QueryTermParser.ACTION_ARG_SEPARATOR, maybeQuote(arg()));
        }
    }
