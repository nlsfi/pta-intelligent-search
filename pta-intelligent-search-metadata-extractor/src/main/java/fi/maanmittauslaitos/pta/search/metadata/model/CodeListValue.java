package fi.maanmittauslaitos.pta.search.metadata.model;

import java.util.Objects;

/*  POJO for CSW CodeListValues. Some CodeListValues aren't parsed as this, but some could be changed to use this.
    If not directly, maybe as a superclass for a more specific impl. These include:
    - Metadata language
    - Resource Language
    - Metadata type (service, series, dataset)
    - Responsible parties' roles
    - Dates (used to define the date type, e.g. publication, creation)
    - Keyword types
    - Restrictions
*/
// TODO Implement broader CodeListValue usage where applicable, see above comment for details.
public class CodeListValue {

    public static final String VALUE_DATE_TYPE_CODE = "gmd:CI_DateTypeCode";
    public static final String VALUE_MAINTENANCE_FREQUENCY_CODE = "gmd:MD_MaintenanceFrequencyCode";

    private String value;
    private String list;
    private String type;

    public CodeListValue(){}

    public CodeListValue(String value, String list, String type) {
        this.value = value;
        this.list = list;
        this.type = type;
    }

    public CodeListValue(CodeListValue that) {
        this.value = that.getValue();
        this.list = that.getList();
        this.type = that.getType();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeListValue that = (CodeListValue) o;
        return Objects.equals(value, that.value) &&
                Objects.equals(list, that.list) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, list, type);
    }

    @Override
    public String toString() {
        return "CodeListValue{" +
                "value='" + value + '\'' +
                ", list='" + list + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
