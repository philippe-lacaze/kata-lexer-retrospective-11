package fr.umlv.lexer;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
interface Lexer<T> {

    Optional<T> tryParse(String text);

    static <T> Lexer<T> create() {
        return (String s) -> {
            Objects.requireNonNull(s);
            return Optional.empty();
        };
    }

    static Lexer<String> from(Pattern pattern) {
        requireOneCaptureGroup(pattern);
        return text -> Optional.of(pattern.matcher(text))
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1));
    }

    static Lexer<String> from(String regex) {
        return from(Pattern.compile(regex));
    }

    private static void requireOneCaptureGroup(Pattern pattern) {
        if (pattern.matcher("").groupCount() != 1) {
            throw new IllegalArgumentException(pattern + " has not one captured group");
        }
    }

    default <U> Lexer<U> map(
            Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return text -> tryParse(text).map(mapper);
    }

    default Lexer<T> or(Lexer<? extends T> lexer) {
        Objects.requireNonNull(lexer);
        return text -> tryParse(text)
                .or(()-> lexer.tryParse(text));
    }

}
