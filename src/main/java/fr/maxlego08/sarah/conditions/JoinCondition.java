package fr.maxlego08.sarah.conditions;

public class JoinCondition {
    private final String primaryTable;
    private final String primaryTableAlias;
    private final String primaryColumn;
    private final String foreignTable;
    private final String foreignColumn;
    private final JoinType joinType;
    private final JoinCondition additionalCondition;

    public JoinCondition(JoinType joinType, String primaryTable, String primaryTableAlias, String primaryColumn, String foreignTable, String foreignColumn, JoinCondition additionalCondition) {
        this.primaryTable = primaryTable;
        this.primaryTableAlias = primaryTableAlias;
        this.primaryColumn = primaryColumn;
        this.foreignTable = foreignTable;
        this.foreignColumn = foreignColumn;
        this.joinType = joinType;
        this.additionalCondition = additionalCondition;
    }

    public static JoinCondition and(String primaryTable, String primaryTableAlias, String primaryColumn, String foreignTable, String foreignColumn) {
        return new JoinCondition(null, primaryTable, primaryTableAlias, primaryColumn, foreignTable, foreignColumn, null);
    }

    public String getJoinClause() {
        StringBuilder joinClause = new StringBuilder();
        joinClause.append(this.joinType.getSql()).append(" ")
                .append(this.primaryTable).append(" AS ").append(this.primaryTableAlias)
                .append(" ON ").append(this.primaryTableAlias).append(".").append(this.primaryColumn)
                .append(" = ").append(this.foreignTable).append(".").append(this.foreignColumn);

        if (this.additionalCondition != null) {
            joinClause.append(" AND ").append(this.additionalCondition.getCondition());
        }
        return joinClause.toString();
    }

    private String getCondition() {
        return this.primaryTableAlias + "." + this.primaryColumn + " = " + this.foreignTable + "." + this.foreignColumn;
    }

    public enum JoinType {
        INNER("INNER JOIN"),
        LEFT("LEFT JOIN"),
        RIGHT("RIGHT JOIN"),
        FULL("FULL OUTER JOIN");

        private final String sql;

        JoinType(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return this.sql;
        }
    }
}
