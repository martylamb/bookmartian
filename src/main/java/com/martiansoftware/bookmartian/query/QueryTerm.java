/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.martiansoftware.bookmartian.query;

import com.martiansoftware.util.Check;
import com.martiansoftware.util.Strings;

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
        public String toString() {
            return String.format("%s:%s", action(), arg());
        }
    }
