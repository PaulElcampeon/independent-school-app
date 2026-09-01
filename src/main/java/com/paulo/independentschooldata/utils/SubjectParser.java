package com.paulo.independentschooldata.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubjectParser {

    private static final Pattern SCHOOL_PATTERN =
            Pattern.compile("^(.*?)\\s+school information$", Pattern.CASE_INSENSITIVE);

    public static String extractSchoolName(String subject) {
        if (subject == null) return null;

        Matcher matcher = SCHOOL_PATTERN.matcher(subject.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return subject; // fallback if no match
    }
}
