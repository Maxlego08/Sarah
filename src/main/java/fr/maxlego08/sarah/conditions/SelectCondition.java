package fr.maxlego08.sarah.conditions;

import java.util.Objects;

public class SelectCondition {
    private final String tablePrefix;
    private final String column;
    private final String aliases;
    private final boolean isCoalesce;
    private final Object defaultValue;

    public SelectCondition(String tablePrefix, String column, String aliases, boolean isCoalesce, Object defaultValue) {
        this.tablePrefix = tablePrefix;
        this.column = column;
        this.aliases = aliases;
        this.isCoalesce = isCoalesce;
        this.defaultValue = defaultValue;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getColumn() {
        return column;
    }

    public boolean isCoalesce() {
        return isCoalesce;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getSelectColumn() {
        String result = this.tablePrefix == null ? this.getColumnAndAliases() : this.tablePrefix + "." + this.getColumnAndAliases();
        if (isCoalesce) {
            String tableName = this.tablePrefix == null ? "`" + this.column + "`" : this.tablePrefix + ".`" + this.column + "`";
            return "COALESCE(" + tableName + ", " + defaultValue + ")" + getAliases();
        }
        return result;
    }

    private String getColumnAndAliases() {
        return "`" + this.column + "`" + getAliases();
    }

    private String getAliases() {
        return this.aliases == null ? "" : " as " + this.aliases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectCondition that = (SelectCondition) o;
        return isCoalesce == that.isCoalesce &&
                Objects.equals(tablePrefix, that.tablePrefix) &&
                Objects.equals(column, that.column) &&
                Objects.equals(aliases, that.aliases) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tablePrefix, column, aliases, isCoalesce, defaultValue);
    }

    @Override
    public String toString() {
        return "SelectCondition{" +
                "tablePrefix='" + tablePrefix + '\'' +
                ", column='" + column + '\'' +
                ", aliases='" + aliases + '\'' +
                ", isCoalesce=" + isCoalesce +
                ", defaultValue=" + defaultValue +
                '}';
    }
}