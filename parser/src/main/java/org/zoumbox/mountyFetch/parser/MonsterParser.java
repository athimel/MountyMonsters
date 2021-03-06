package org.zoumbox.mountyFetch.parser;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lit et échappe les informations fournies par l'utilisateur (ou MH) et délègue le calcul à {@link MonsterBuilder}
 */
public class MonsterParser {

    private static final Pattern SP_VUE2_MONSTER_PATTERN = Pattern.compile("([0-9]*);(.*);([-]?[0-9]*);([-]?[0-9]*);([-]?[0-9]*)");

    protected static String normalizePart(String part) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(part));
        if (part.equalsIgnoreCase("parasitus")) {
            return part;
        }
        if (part.startsWith("[") && part.endsWith("]")) {
            String subPart = part.substring(1, part.length() - 1);
            String result = String.format("[%s]", normalizePart(subPart));
            return result;
        }
        String result = StringUtils.capitalize(part);
        return result;
    }

    protected static Optional<String> tryNormalizeName(String rawName) {
        if (!Strings.isNullOrEmpty(rawName)) {
            List<String> parts = Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(rawName);
            String result = parts.stream()
                    .map(MonsterParser::normalizePart)
                    .collect(Collectors.joining(" "));
            // Si et seulement si le résultat est différent alors on renvoie celui-ci
            if (!result.equals(rawName)) {
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    /**
     * Essaye de calculer un maximum d'informations sur un monstre à partir de son nom. Exemple :
     * <pre>Maîtresse Ame-en-peine [Naissante]</pre>
     *
     * @param name le nom brut
     * @return Les informations déduites à propos du monstre
     */
    public static ImmutableMonster fromRawName(String name) {
        ImmutableMonster result = MonsterBuilder.fromName(name);
        result = MonsterBuilder.extractTemplate(result);
        result = MonsterBuilder.extractFamilyAndNival(result);
        result = MonsterBuilder.finalizeExtraction(result);
        if (!result.nival().isPresent()) {
            System.err.println("Impossible de trouver le nival : " + result);
            Optional<String> normalized = tryNormalizeName(name);
            if (normalized.isPresent()) {
                String alternativeName = normalized.get();
                Preconditions.checkState(!alternativeName.equals(name),
                        "On va créer une nouble infinie si on rappelle avec le même nom");
                ImmutableMonster alternative = fromRawName(alternativeName);
                if (alternative.nival().isPresent()) {
                    System.err.println("On a trouvé une alternative : " + alternative);
                    result = alternative;
                }
            }
        }
        return result;
    }

    /**
     * Essaye de calculer un maximum d'informations sur un monstre à partir de la ligne extraite de Sp_Vue2.php).
     * Exemple :
     * <pre>5864923;Maîtresse Ame-en-peine [Naissante];-74;-40;-78</pre>
     *
     * @param row La ligne brute provenant de Sp_Vue2.php
     * @return Les informations déduites à propos du monstre
     * @see #fromRawName(String)
     */
    public static ImmutableMonster fromSpVue2Row(String row) {
        Matcher matcher = SP_VUE2_MONSTER_PATTERN.matcher(row);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Format invalide: %s", row));
        }

        String name = matcher.group(2);
        ImmutableMonster result = fromRawName(name);
        Position position = Position.of(
                Integer.valueOf(matcher.group(3)),
                Integer.valueOf(matcher.group(4)),
                Integer.valueOf(matcher.group(5)));
        result = ImmutableMonster.builder()
                .from(result)
                .id(Integer.valueOf(matcher.group(1)))
                .position(position)
                .build();
        return result;
    }

}
