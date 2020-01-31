package fi.maanmittauslaitos.pta.search.metadata.model;

import java.util.Objects;

public class MetadataDate {

    private String date;
    private CodeListValue type;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CodeListValue getType() {
        return type;
    }

    public void setType(CodeListValue type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataDate that = (MetadataDate) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, type);
    }

    @Override
    public String toString() {
        return "MetadataDate{" +
                "date='" + date + '\'' +
                ", type=" + type +
                '}';
    }
}
