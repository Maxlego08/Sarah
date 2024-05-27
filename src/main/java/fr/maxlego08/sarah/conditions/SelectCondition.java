package fr.maxlego08.sarah.conditions;

public record SelectCondition(String tablePrefix, String column, String aliases, boolean isCoalesce,
                              Object defaultValue) {

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
}
