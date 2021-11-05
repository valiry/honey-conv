package net.valiry.honeyconv;

import java.util.Map;
import java.util.Objects;

public class CachedState {

    private String type;
    private Map<String, String> properties;

    public CachedState(final String type, final Map<String, String> properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CachedState that = (CachedState) o;
        return this.type.equals(that.type) && this.properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.properties);
    }

}
