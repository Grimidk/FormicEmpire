package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class CityTitle extends Constant {

    public enum Affix {
        PREFIX,
        SUFFIX
    }

    public enum ColonyRole {
        CAPITAL,
        SATELLITE
    }

    private final Affix affix;
    private final ColonyRole colonyRole;

    public CityTitle(int id, String nameKey, Affix affix, ColonyRole colonyRole) {
        super(id, nameKey, null);
        this.affix = affix != null ? affix : Affix.SUFFIX;
        this.colonyRole = colonyRole != null ? colonyRole : ColonyRole.SATELLITE;
    }

    public Affix getAffix() {
        return affix;
    }

    public ColonyRole getColonyRole() {
        return colonyRole;
    }

    public boolean isCapital() {
        return colonyRole == ColonyRole.CAPITAL;
    }

    public boolean isSatellite() {
        return colonyRole == ColonyRole.SATELLITE;
    }

    public boolean isPrefix() {
        return affix == Affix.PREFIX;
    }

    public boolean isSuffix() {
        return affix == Affix.SUFFIX;
    }

    public String formatName(String baseName) {
        String base = baseName != null ? baseName.trim() : "";
        String title = getName();
        if (title == null || title.isEmpty()) {
            return base;
        }
        if (base.isEmpty()) {
            return title;
        }
        if (affix == Affix.PREFIX) {
            return title + " " + base;
        }
        return base + " " + title;
    }

    public String stripLocalizedTitle(String fullName, String localizedTitle) {
        if (fullName == null || localizedTitle == null || localizedTitle.isEmpty()) {
            return null;
        }
        String trimmed = fullName.trim();
        String title = localizedTitle.trim();
        if (affix == Affix.PREFIX) {
            String prefix = title + " ";
            if (trimmed.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return trimmed.substring(prefix.length()).trim();
            }
            return null;
        }
        String suffix = " " + title;
        if (trimmed.length() > suffix.length()
                && trimmed.regionMatches(true, trimmed.length() - suffix.length(), suffix, 0, suffix.length())) {
            return trimmed.substring(0, trimmed.length() - suffix.length()).trim();
        }
        return null;
    }

    @Override
    public String toString() {
        return LanguageStrings.get(getNameKey());
    }
}
