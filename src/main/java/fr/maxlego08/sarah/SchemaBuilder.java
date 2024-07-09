package fr.maxlego08.sarah;

import fr.maxlego08.sarah.conditions.ColumnDefinition;
import fr.maxlego08.sarah.conditions.JoinCondition;
import fr.maxlego08.sarah.conditions.SelectCondition;
import fr.maxlego08.sarah.conditions.WhereCondition;
import fr.maxlego08.sarah.database.Executor;
import fr.maxlego08.sarah.database.Migration;
import fr.maxlego08.sarah.database.Schema;
import fr.maxlego08.sarah.database.SchemaType;
import fr.maxlego08.sarah.requests.AlterRequest;
import fr.maxlego08.sarah.requests.CreateRequest;
import fr.maxlego08.sarah.requests.DeleteRequest;
import fr.maxlego08.sarah.requests.InsertRequest;
import fr.maxlego08.sarah.requests.UpdateRequest;
import fr.maxlego08.sarah.requests.UpsertRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SchemaBuilder implements Schema {

    private final String tableName;
    private final SchemaType schemaType;
    private final List<ColumnDefinition> columns = new ArrayList<>();
    private final List<String> primaryKeys = new ArrayList<>();
    private final List<String> foreignKeys = new ArrayList<>();
    private final List<WhereCondition> whereConditions = new ArrayList<>();
    private final List<JoinCondition> joinConditions = new ArrayList<>();
    private final List<SelectCondition> selectColumns = new ArrayList<>();
    private String orderBy;
    private Migration migration;
    private boolean isDistinct;

    private SchemaBuilder(String tableName, SchemaType schemaType) {
        this.tableName = tableName;
        this.schemaType = schemaType;
    }

    public static Schema create(Migration migration, String tableName, Consumer<Schema> consumer) {
        SchemaBuilder schema = new SchemaBuilder(tableName, SchemaType.CREATE);
        if (migration != null) {
            schema.migration = migration;
            MigrationManager.registerSchema(schema);
        }
        consumer.accept(schema);
        return schema;
    }

    public static Schema upsert(String tableName, Consumer<Schema> consumer) {
        Schema schema = new SchemaBuilder(tableName, SchemaType.UPSERT);
        consumer.accept(schema);
        return schema;
    }

    public static Schema alter(Migration migration, String tableName, Consumer<Schema> consumer) {
        SchemaBuilder schema = new SchemaBuilder(tableName, SchemaType.ALTER);
        if (migration != null) {
            schema.migration = migration;
            MigrationManager.registerSchema(schema);
        }
        consumer.accept(schema);
        return schema;
    }

    public static Schema insert(String tableName, Consumer<Schema> consumer) {
        Schema schema = new SchemaBuilder(tableName, SchemaType.INSERT);
        consumer.accept(schema);
        return schema;
    }

    public static Schema update(String tableName, Consumer<Schema> consumer) {
        Schema schema = new SchemaBuilder(tableName, SchemaType.UPDATE);
        consumer.accept(schema);
        return schema;
    }

    public static Schema select(String tableName) {
        return new SchemaBuilder(tableName, SchemaType.SELECT);
    }

    public static Schema selectCount(String tableName) {
        return new SchemaBuilder(tableName, SchemaType.SELECT);
    }

    public static Schema delete(String tableName) {
        return new SchemaBuilder(tableName, SchemaType.DELETE);
    }

    @Override
    public Schema where(String columnName, Object value) {
        this.whereConditions.add(new WhereCondition(columnName, value));
        return this;
    }

    @Override
    public Schema where(String columnName, UUID value) {
        return this.where(columnName, value.toString());
    }

    @Override
    public Schema where(String columnName, String operator, Object value) {
        this.whereConditions.add(new WhereCondition(columnName, operator, value));
        return this;
    }

    @Override
    public Schema whereNotNull(String columnName) {
        this.whereConditions.add(new WhereCondition(columnName));
        return this;
    }

    @Override
    public Schema uuid(String columnName) {
        this.string(columnName, 36);
        return this;
    }

    @Override
    public Schema uuid(String columnName, UUID value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value.toString()));
    }

    @Override
    public Schema string(String columnName, int length) {
        return addColumn(new ColumnDefinition(columnName, "VARCHAR").setLength(length));
    }

    @Override
    public Schema text(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "TEXT"));
    }

    @Override
    public Schema longText(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "LONGTEXT"));
    }

    @Override
    public Schema decimal(String columnName) {
        return this.decimal(columnName, 65, 30);
    }

    @Override
    public Schema decimal(String columnName, int length, int decimal) {
        return addColumn(new ColumnDefinition(columnName, "DECIMAL").setLength(length).setDecimal(decimal));
    }

    @Override
    public Schema string(String columnName, String value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value));
    }

    @Override
    public Schema decimal(String columnName, Number value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value));
    }

    @Override
    public Schema date(String columnName, Date value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value));
    }

    @Override
    public Schema object(String columnName, Object object) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(object));
    }

    @Override
    public Schema bigInt(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "BIGINT"));
    }

    @Override
    public Schema integer(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "INT"));
    }

    @Override
    public Schema bigInt(String columnName, long value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value));
    }

    @Override
    public Schema bool(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "BOOLEAN"));
    }

    @Override
    public Schema bool(String columnName, boolean value) {
        return this.addColumn(new ColumnDefinition(columnName).setObject(value));
    }

    @Override
    public Schema blob(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "BLOB"));
    }

    @Override
    public Schema blob(String columnName, byte[] value) {
        return this.addColumn(new ColumnDefinition(columnName, "BLOB").setObject(value));
    }

    @Override
    public Schema blob(String columnName, Object object) {
        try {
            byte[] serializedObject = serializeObject(object);
            return this.addColumn(new ColumnDefinition(columnName, "BLOB").setObject(serializedObject));
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred while serializing object for BLOB column: " + columnName, exception);
        }
    }

    @Override
    public Schema foreignKey(String referenceTable) {
        if (this.columns.isEmpty()) throw new IllegalStateException("No column defined to apply foreign key.");
        ColumnDefinition lastColumn = this.columns.get(this.columns.size() - 1);

        String fkDefinition = String.format("FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE", lastColumn.getSafeName(), referenceTable, lastColumn.getSafeName());
        this.foreignKeys.add(fkDefinition);
        return this;
    }

    @Override
    public Schema foreignKey(String referenceTable, String columnName, boolean onCascade) {
        if (this.columns.isEmpty()) throw new IllegalStateException("No column defined to apply foreign key.");
        ColumnDefinition lastColumn = this.columns.get(this.columns.size() - 1);

        String fkDefinition = String.format("FOREIGN KEY (%s) REFERENCES %s(%s)%s", lastColumn.getSafeName(), referenceTable, columnName, onCascade ? " ON DELETE CASCADE" : "");
        this.foreignKeys.add(fkDefinition);
        return this;
    }

    @Override
    public Schema createdAt() {
        ColumnDefinition column = new ColumnDefinition("created_at", "TIMESTAMP");
        column.setDefaultValue("CURRENT_TIMESTAMP");
        this.columns.add(column);
        return this;
    }

    @Override
    public Schema timestamp(String columnName) {
        return this.addColumn(new ColumnDefinition(columnName, "TIMESTAMP"));
    }

    @Override
    public Schema autoIncrement(String columnName) {
        return addColumn(new ColumnDefinition(columnName, "BIGINT").setAutoIncrement(true)).primary();
    }

    @Override
    public Schema updatedAt() {
        ColumnDefinition column = new ColumnDefinition("updated_at", "TIMESTAMP");
        column.setDefaultValue("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        this.columns.add(column);
        return this;
    }

    @Override
    public Schema nullable() {
        getLastColumn().setNullable(true);
        return this;
    }

    @Override
    public Schema defaultValue(String value) {
        getLastColumn().setDefaultValue(value);
        return this;
    }

    @Override
    public Schema primary() {
        ColumnDefinition lastColumn = getLastColumn();
        lastColumn.setPrimaryKey(true);
        primaryKeys.add(lastColumn.getSafeName());
        return this;
    }

    private Schema addColumn(ColumnDefinition column) {
        columns.add(column);
        return this;
    }

    @Override
    public Schema timestamps() {
        this.createdAt();
        this.updatedAt();
        return this;
    }

    private ColumnDefinition getLastColumn() {
        if (columns.isEmpty()) throw new IllegalStateException("No columns defined.");
        return columns.get(columns.size() - 1);
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public void whereConditions(StringBuilder sql) {
        if (!this.whereConditions.isEmpty()) {
            List<String> conditions = new ArrayList<>();
            for (WhereCondition condition : this.whereConditions) {
                conditions.add(condition.getCondition());
            }
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
    }

    @Override
    public long executeSelectCount(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException {
        StringBuilder selectQuery = new StringBuilder("SELECT COUNT(*) FROM " + tableName);
        this.whereConditions(selectQuery);

        String finalQuery = selectQuery.toString();
        if (databaseConfiguration.isDebug()) {
            logger.info("Executing SQL: " + finalQuery);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(finalQuery)) {

            applyWhereConditions(preparedStatement, 1);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getInt(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SQLException("Failed to execute schema select count: " + exception.getMessage(), exception);
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> executeSelect(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        String selectedValues = "*";
        if (!this.selectColumns.isEmpty()) {
            selectedValues = this.selectColumns.stream().map(SelectCondition::getSelectColumn).collect(Collectors.joining(","));
        }

        StringBuilder selectQuery = this.isDistinct ? new StringBuilder("SELECT DISTINCT " + this.tableName + "." + selectedValues + " FROM " + this.tableName) : new StringBuilder("SELECT " + selectedValues + " FROM " + this.tableName);

        if (!this.joinConditions.isEmpty()) {
            for (JoinCondition join : this.joinConditions) {
                selectQuery.append(" ").append(join.getJoinClause());
            }
        }

        this.whereConditions(selectQuery);
        if (this.orderBy != null) {
            selectQuery.append(" ").append(this.orderBy);
        }

        String finalQuery = databaseConfiguration.replacePrefix(selectQuery.toString());
        if (databaseConfiguration.isDebug()) {
            logger.info("Executing SQL: " + finalQuery);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(finalQuery)) {
            applyWhereConditions(preparedStatement, 1);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SQLException("Failed to execute schema select: " + exception.getMessage(), exception);
        }

        return results;
    }

    @Override
    public void applyWhereConditions(PreparedStatement preparedStatement, int index) throws SQLException {
        for (WhereCondition condition : this.whereConditions) {
            if (!condition.isNotNull()) {
                preparedStatement.setObject(index++, condition.getValue());
            }
        }
    }

    @Override
    public <T> List<T> executeSelect(Class<T> clazz, Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws Exception {
        List<Map<String, Object>> results = executeSelect(connection, databaseConfiguration, logger);
        return transformResults(results, clazz);
    }

    private <T> List<T> transformResults(List<Map<String, Object>> results, Class<T> clazz) throws Exception {
        List<T> transformedResults = new ArrayList<>();
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> firstConstructor = constructors[0];
        firstConstructor.setAccessible(true);

        for (Map<String, Object> row : results) {
            Object[] params = new Object[firstConstructor.getParameterCount()];
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    params[i] = convertToRequiredType(row.get(column.value()), field.getType());
                } else {
                    params[i] = convertToRequiredType(row.get(field.getName()), field.getType());
                }
            }
            T instance = (T) firstConstructor.newInstance(params);
            transformedResults.add(instance);
        }
        return transformedResults;
    }

    protected Object convertToRequiredType(Object value, Class<?> type) {
        if (value == null) {
            return null;
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, (String) value);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value.toString());
        } else if (type == UUID.class) {
            return UUID.fromString((String) value);
        } else if (type == Long.class || type == long.class) {
            return ((Number) value).longValue();
        } else if (type == Double.class || type == double.class) {
            return ((Number) value).doubleValue();
        } else if (type == Integer.class || type == int.class) {
            return ((Number) value).intValue();
        } else if(Serializable.class.isAssignableFrom(type) && value instanceof byte[]) {
            return deserializeObject((byte[]) value, type);
        } else {
            return value;
        }
    }

    protected byte[] serializeObject(Object object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return baos.toByteArray();
        }
    }

    protected  <T> T deserializeObject(byte[] data, Class<T> type) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return type.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException exception) {
            throw new Error("An exception occurred during deserialization of a BLOB ", exception);
        }
    }

    @Override
    public Migration getMigration() {
        return migration;
    }

    @Override
    public Schema leftJoin(String primaryTable, String primaryColumnAlias, String primaryColumn, String foreignTable, String foreignColumn) {
        this.joinConditions.add(new JoinCondition(JoinCondition.JoinType.LEFT, primaryTable, primaryColumnAlias, primaryColumn, foreignTable, foreignColumn, null));
        return this;
    }

    @Override
    public Schema leftJoin(String primaryTable, String primaryColumnAlias, String primaryColumn, String foreignTable, String foreignColumn, JoinCondition andCondition) {
        this.joinConditions.add(new JoinCondition(JoinCondition.JoinType.LEFT, primaryTable, primaryColumnAlias, primaryColumn, foreignTable, foreignColumn, andCondition));
        return this;
    }

    @Override
    public Schema rightJoin(String primaryTable, String primaryColumnAlias, String primaryColumn, String foreignTable, String foreignColumn) {
        this.joinConditions.add(new JoinCondition(JoinCondition.JoinType.RIGHT, primaryTable, primaryColumnAlias, primaryColumn, foreignTable, foreignColumn, null));
        return this;
    }

    @Override
    public Schema innerJoin(String primaryTable, String primaryColumnAlias, String primaryColumn, String foreignTable, String foreignColumn) {
        this.joinConditions.add(new JoinCondition(JoinCondition.JoinType.INNER, primaryTable, primaryColumnAlias, primaryColumn, foreignTable, foreignColumn, null));
        return this;
    }

    @Override
    public Schema fullJoin(String primaryTable, String primaryColumnAlias, String primaryColumn, String foreignTable, String foreignColumn) {
        this.joinConditions.add(new JoinCondition(JoinCondition.JoinType.FULL, primaryTable, primaryColumnAlias, primaryColumn, foreignTable, foreignColumn, null));
        return this;
    }

    @Override
    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    @Override
    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    @Override
    public List<String> getForeignKeys() {
        return foreignKeys;
    }

    @Override
    public List<JoinCondition> getJoinConditions() {
        return joinConditions;
    }

    @Override
    public void orderBy(String columnName) {
        this.orderBy = String.format("ORDER BY %s", columnName);
    }

    @Override
    public void orderByDesc(String columnName) {
        this.orderBy = String.format("ORDER BY %s DESC", columnName);
    }

    @Override
    public String getOrderBy() {
        return this.orderBy;
    }

    @Override
    public void distinct() {
        this.isDistinct = true;
    }

    @Override
    public boolean isDistinct() {
        return this.isDistinct;
    }

    @Override
    public int execute(Connection connection, DatabaseConfiguration databaseConfiguration, Logger logger) throws SQLException {
        Executor executor;
        switch (this.schemaType) {
            case CREATE:
                executor = new CreateRequest(this);
                break;
            case ALTER:
                executor = new AlterRequest(this);
                break;
            case UPSERT:
                executor = new UpsertRequest(this);
                break;
            case UPDATE:
                executor = new UpdateRequest(this);
                break;
            case INSERT:
                executor = new InsertRequest(this);
                break;
            case DELETE:
                executor = new DeleteRequest(this);
                break;
            case SELECT:
            case SELECT_COUNT:
                throw new IllegalArgumentException("Wrong method !");
            default:
                throw new Error("Schema type not found !");
        }

        return executor.execute(connection, databaseConfiguration, logger);
    }

    @Override
    public void addSelect(String selectedColumn) {
        this.selectColumns.add(new SelectCondition(null, selectedColumn, null, false, null));
    }

    @Override
    public void addSelect(String prefix, String selectedColumn) {
        this.selectColumns.add(new SelectCondition(prefix, selectedColumn, null, false, null));
    }

    @Override
    public void addSelect(String prefix, String selectedColumn, String aliases) {
        this.selectColumns.add(new SelectCondition(null, selectedColumn, aliases, false, null));
    }

    @Override
    public void addSelect(String prefix, String selectedColumn, String aliases, Object defaultValue) {
        this.selectColumns.add(new SelectCondition(null, selectedColumn, aliases, true, defaultValue));
    }
}
