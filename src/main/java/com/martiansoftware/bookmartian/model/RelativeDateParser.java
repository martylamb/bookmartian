package com.martiansoftware.bookmartian.model;

import com.martiansoftware.util.Dates;
import com.martiansoftware.util.Oops;
import com.martiansoftware.util.Strings;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author mlamb
 */
public class RelativeDateParser {
    
    enum UNITS { 
        YEARS("years year yr y", Calendar.YEAR, 1),
        MONTHS("months month mnth mon mo m", Calendar.MONTH, 1),
        WEEKS("weeks week wk w", Calendar.DATE, 7),
        DAYS("days day dy d", Calendar.DATE, 1);
        
        private final List<String> _abbr;
        private final int _calendarField;
        private final int _adjustFactor;
        
        private UNITS(String s, int calendarField, int adjustFactor) {
            _abbr = Strings.splitOnWhitespaceAndCommas(s);
            _calendarField = calendarField;
            _adjustFactor = adjustFactor;
        }
        
        public static UNITS of(String s) {
            String unit = Strings.lower(Strings.safeTrim(s));
            for (UNITS u : UNITS.values()) {
                if (u._abbr.contains(unit)) return u;
            }
            return null;
        }
        
        public String regex() {
            return _abbr.stream().collect(Collectors.joining("|"));
        }
        
        public Calendar adjust(Calendar cal, int qty) {
            cal.add(_calendarField, qty * _adjustFactor * -1);
            return cal;
        }
        
        public static String anyUnitPattern() {
            return Stream.of(UNITS.values()).map(u -> u.regex()).collect(Collectors.joining("|"));
        }        
    }
    
    private static final String ONE_ADJUSTMENT_REGEX = "(0*(?<qty>[1-9][0-9]*)(?<units>" + UNITS.anyUnitPattern() + "))";
    private static final Pattern ONE_ADJUSTMENT = Pattern.compile(ONE_ADJUSTMENT_REGEX);
    private static final Pattern MULTIPLE_ADJUSTMENTS = Pattern.compile("^" + ONE_ADJUSTMENT_REGEX + "+$");
    
    public static Date parse(String s) {
        String d = Strings.lower(s);
        Matcher m = MULTIPLE_ADJUSTMENTS.matcher(d);
        if (!m.matches()) Oops.oops("not a valid relative date: '%s'", s);
        
        Calendar cal = Calendar.getInstance();
        m = ONE_ADJUSTMENT.matcher(d);
        while (m.find()) {
            UNITS.of(m.group("units")).adjust(cal, Integer.valueOf(m.group("qty")));
        }        
        return Dates.stripTime(cal.getTime());
    }
    
}
