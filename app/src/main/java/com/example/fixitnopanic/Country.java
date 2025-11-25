package com.example.fixitnopanic;

import java.util.Objects;

public class Country {
    private final String name;
    private final String code;
    private final int flag;
    private final boolean isDefault;

    public Country(String name, String code, int flag) {
        this(name, code, flag, false);
    }

    public Country(String name, String code, int flag, boolean isDefault) {
        this.name = name;
        this.code = code;
        this.flag = flag;
        this.isDefault = isDefault;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public int getFlag() { return flag; }
    public boolean isDefault() { return isDefault; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country)) return false;
        Country country = (Country) o;
        return isDefault == country.isDefault &&
                Objects.equals(name, country.name) &&
                Objects.equals(code, country.code) &&
                flag == country.flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, flag, isDefault);
    }

    @Override
    public String toString() {
        return "Country{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", flag=" + flag +
                ", isDefault=" + isDefault +
                '}';
    }
}