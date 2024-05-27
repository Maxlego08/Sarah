package fr.maxlego08.sarah.database;

public record SelectDefinition(String column, String tablePrefix) {

    public String getSelectColumn() {
        return this.tablePrefix == null ? "`" + column + "`" : this.tablePrefix + ".`" + column + "`";
    }
}
