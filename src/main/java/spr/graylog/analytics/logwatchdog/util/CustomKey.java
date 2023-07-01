package spr.graylog.analytics.logwatchdog.util;

import java.util.Map;
import java.util.Objects;

public class CustomKey {
    private final Map<String, String> map;

    public CustomKey(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            System.out.println("equal = ture");
            return true;
        }
        System.out.println("equal = false");

        if (o == null || getClass() != o.getClass()) return false;
        CustomKey customKey = (CustomKey) o;
        return Objects.equals(map, customKey.map);
    }

    @Override
    public int hashCode() {
        System.out.println("inside hash");

        return Objects.hash(map);
    }
}