/*
Copyright (c) 2021, MapTiler.com & OpenMapTiles contributors.
All rights reserved.

Code license: BSD 3-Clause License

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Design license: CC-BY 4.0

See https://github.com/openmaptiles/openmaptiles/blob/master/LICENSE.md for details on usage
*/
package org.openmaptiles.util;

import static com.onthegomap.planetiler.util.LanguageUtils.*;
import static org.openmaptiles.util.Utils.coalesce;
import static java.util.Map.entry;

import com.onthegomap.planetiler.util.LanguageUtils;
import com.onthegomap.planetiler.util.Translations;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utilities to extract common name fields (name, name_en, name_de, name:latin, name:nonlatin, name_int) that the
 * OpenMapTiles schema uses across any map element with a name.
 * <p>
 * Ported from
 * <a href="https://github.com/openmaptiles/openmaptiles-tools/blob/master/sql/zzz_language.sql">openmaptiles-tools</a>.
 */
public class OmtLanguageUtils {
  /**
   * Returns a map with default name attributes (name, name_en, name_de, name:latin, name:nonlatin, name_int) that every
   * element should have, derived from name, int_name, name:en, and name:de tags on the input element.
   *
   * <ul>
   * <li>name is the original name value from the element</li>
   * <li>name_en is the original name:en value from the element, or name if missing</li>
   * <li>name_de is the original name:de value from the element, or name/ name_en if missing</li>
   * <li>name:latin is the first of name, int_name, or any name: attribute that contains only latin characters</li>
   * <li>name:nonlatin is any nonlatin part of name if present</li>
   * <li>name_int is the first of int_name name:en name:latin name</li>
   * </ul>
   */
  public static Map<String, Object> getNamesWithoutTranslations(Map<String, Object> tags) {
    return getNames(tags, null);
  }

  /**
   * Returns a map with default name attributes that {@link #getNamesWithoutTranslations(Map)} adds, but also
   * translations for every language that {@code translations} is configured to handle.
   */
  public static Map<String, Object> getNames(Map<String, Object> tags, Translations translations) {
    Map<String, Object> result = new HashMap<>();

    String name = string(tags.get("name"));
    String intName = string(tags.get("int_name"));
    String nameEn = string(tags.get("name:en"));
    String nameDe = string(tags.get("name:de"));

    boolean isLatin = containsOnlyLatinCharacters(name);
    String latin = isLatin ? name :
      Stream
        .concat(Stream.of(nameEn, intName, nameDe), getAllNameTranslationsBesidesEnglishAndGerman(tags))
        .filter(LanguageUtils::containsOnlyLatinCharacters)
        .findFirst().orElse(null);
    if (latin == null && translations != null && translations.getShouldTransliterate()) {
      latin = transliteratedName(tags);
    }
    String nonLatin = isLatin ? null : removeLatinCharacters(name);
    if (coalesce(nonLatin, "").equals(latin)) {
      nonLatin = null;
    }

    putIfNotEmpty(result, "name", name);
    putIfNotEmpty(result, "name_en", coalesce(nameEn, name));
    putIfNotEmpty(result, "name_de", coalesce(nameDe, name, nameEn));
    putIfNotEmpty(result, "name:latin", latin);
    putIfNotEmpty(result, "name:nonlatin", nonLatin);
    putIfNotEmpty(result, "name_int", coalesce(
      intName,
      nameEn,
      latin,
      name
    ));

    if (translations != null) {
      translations.addTranslations(result, tags);
    }

    return result;
  }

  private static Stream<String> getAllNameTranslationsBesidesEnglishAndGerman(Map<String, Object> tags) {
    return tags.entrySet().stream()
      .filter(e -> !EN_DE_NAME_KEYS.contains(e.getKey()) && VALID_NAME_TAGS.test(e.getKey()))
      .map(Map.Entry::getValue)
      .map(LanguageUtils::string);
  }


  static Map<String, String> stateNameToPost = Map.ofEntries(
      entry("Alabama", "Ala."),
      entry("Alaska", "Alaska"),
      entry("Arizona", "Ariz."),
      entry("Arkansas", "Ark."),
      entry("American Samoa", "A.S."),
      entry("California", "Calif."),
      entry("Colorado", "Colo."),
      entry("Connecticut", "Conn."),
      entry("District of Columbia", "D.C."),
      entry("Delaware", "Del."),
      entry("Florida", "Fla."),
      entry("Georgia", "Ga."),
      entry("Guam", "Guam"),
      entry("Hawaii", "Hawaii"),
      entry("Idaho", "Idaho"),
      entry("Illinois", "Ill."),
      entry("Indiana", "Ind."),
      entry("Iowa", "Iowa"),
      entry("Kansas", "Kan."),
      entry("Kentucky", "Ky."),
      entry("Louisiana", "La."),
      entry("Maine", "Maine"),
      entry("Maryland", "Md."),
      entry("Marshall Islands", "M.H."),
      entry("Massachusetts", "Mass."),
      entry("Michigan", "Mich."),
      entry("Minnesota", "Minn."),
      entry("Mississippi", "Miss."),
      entry("Missouri", "Mo."),
      entry("Montana", "Mont."),
      entry("Nebraska", "Neb."),
      entry("Nevada", "Nev."),
      entry("New Hampshire", "N.H."),
      entry("New Jersey", "N.J."),
      entry("New Mexico", "N.M."),
      entry("New York", "N.Y."),
      entry("North Carolina", "N.C."),
      entry("North Dakota", "N.D."),
      entry("Northern Mariana Islands", "M.P."),
      entry("Ohio", "Ohio"),
      entry("Oklahoma", "Okla."),
      entry("Oregon", "Ore."),
      entry("Pennsylvania", "Pa."),
      entry("Puerto Rico", "P.R."),
      entry("Rhode Island", "R.I."),
      entry("South Carolina", "S.C."),
      entry("South Dakota", "S.D."),
      entry("Tennessee", "Tenn."),
      entry("Texas", "Tex."),
      entry("Utah", "Utah"),
      entry("Vermont", "Vt."),
      entry("Virginia", "Va."),
      entry("Virgin Islands", "V.I."),
      entry("Washington", "Wash."),
      entry("West Virginia", "W.Va."),
      entry("Wisconsin", "Wis."),
      entry("Wyoming", "Wyo.")
  );
  /**
   * Converts a state name to Washington Post style abbreviation
   * e.g. "California" => "Calif."
   */
  public static String getPostStyle(String name) {
    return stateNameToPost.get(name);
  }
}
