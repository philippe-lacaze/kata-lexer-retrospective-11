package fr.umlv.lexer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FunctionalInterface d'un Lexer retournant un token Optional (si trouvé) de type <T> donné.
 *
 * @param <T> type du token optionnel retourné.
 */
@FunctionalInterface
interface Lexer<T> {

    /**
     * Méthode unique de la FunctionalInterface, parsant un un text et retournant un Optional sur
     * l'éventuel token recherchée par l'instance (donnée lors de la création de l'instance via une Methode
     * factoiy).
     *
     * @param text à parser.
     * @return Valeur optionelle du token de type T.
     */
    Optional<T> tryParse(String text);

    /**
     * Methode factory créant une nouvelle instance de Lexer vide pour un token de type T donné.
     *
     * @param <T> donné.
     * @return une nouvelle instance de Lexer vide pour un token de type T donné.
     */
    static <T> Lexer<T> create() {
        return (String s) -> {
            Objects.requireNonNull(s);
            return Optional.empty();
        };
    }

    /**
     * Methode Factory retournant une nouvelle instance de Lexer<String></String> pour le Pattern donné et in token de String.
     *
     * @param pattern donné
     * @return une nouvelle instance de Lexer<String></String> pour le Pattern donné et in token de String.
     */
    static Lexer<String> from(Pattern pattern) {
        requireOneGroup(pattern);

        // Retourne une nouvelle instance de la fonction Lexer (lambda possible car FunctionalInterface).
        return text -> Optional.of(pattern.matcher(text))
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1));
    }

    /**
     * Methode Factory retournant une nouvelle instance de Lexer<String> pour la regexp String donné et in token de String.
     *
     * @param regex donné
     * @return une nouvelle instance de Lexer<String></String> pour le Pattern donné et in token de String.
     */
    static Lexer<String> from(String regex) {
        return from(Pattern.compile(regex));
    }

    /**
     * Methode Factory retournant une nouvelle instance d'une chaîne de  Lexer<T> pour les liste de pattern et la
     * liste de mappers donnés.
     *
     * @param patterns donnés
     * @param mappers  donnés
     * @param <T>
     * @return une nouvelle instance d'une chaîne de  Lexer<T> pour les liste de pattern et la
     * liste de mappers donnés.
     */
    static <T> Lexer<T> from(List<Pattern> patterns, List<Function<? super T, ? extends T>> mappers) {
        return null;
    }

    /**
     * Méthode utilitaire privée permettant de vérifier que le Pattern donné comporte bien un groupe capturant.
     *
     * @param pattern donné
     * @throws IllegalArgumentException si le Pattern donné ne comporte pas un et un seul groupe capturant.
     */
    private static void requireOneGroup(Pattern pattern) {
        if (pattern.matcher("").groupCount() != 1) {
            throw new IllegalArgumentException(pattern + " n'a pas 1 et 1 seul groupe");
        }
    }

    /**
     * Méthode retournant une nouvelle instance de Lexer<U> composé à partir du Lexer this avec
     * là laquelle on applique par composition le mapper donné.
     *
     * @param mapper donné.
     * @param <U>
     * @return une nouvelle instance de Lexer<U> composé à partir du Lexer this avec
     * là laquelle on applique par composition le mapper donné.
     */
    default <U> Lexer<U> map(
            Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);

        // Retourne une nouvelle instance de la fonction Lexer
        return text -> tryParse(text).map(mapper);
    }

    /**
     * Méthode retournant une nouvelle instance de Lexer<U> composé à partir d'un OU sur this et l'instance
     * de lexer donné.
     *
     * @param lexer donné.
     * @return une nouvelle instance de Lexer<U> composé à partir d'un OU sur this et l'instance
     * de lexer donné.
     */
    default Lexer<T> or(Lexer<? extends T> lexer) {
        Objects.requireNonNull(lexer);

        // Retourne une nouvelle instance de la fonction Lexer
        return text -> tryParse(text)
                .or(() -> lexer.tryParse(text));
    }

    /**
     * Méthode retournant une nouvelle instance de Lexer<T> obtenue par composition fonctionnelle
     * de la liste de regexp X liste de mapper.
     *
     * @param regexp donné
     * @param mapper donné
     * @return une nouvelle instance de Lexer<T> obtenue par composition fonctionnelle
     * de la liste de regexp X liste de mapper.
     */
    default Lexer<T> with(String regexp, Function<? super String, ? extends T> mapper) {
        return or((Lexer<T>) Lexer.from(regexp).map(mapper));
    }

}
