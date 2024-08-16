package fr.maxlego08.sarah;

import fr.maxlego08.sarah.database.Schema;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

public class ConsumerConstructor {


    public static Consumer<Schema> createConsumerFromTemplate(Class<?> template, Object data) {
        Constructor<?>[] constructors = template.getDeclaredConstructors();
        Constructor<?> firstConstructor = constructors[0];
        firstConstructor.setAccessible(true);

        Field[] fields = template.getDeclaredFields();
        if (fields.length != firstConstructor.getParameterCount()) {
            throw new IllegalArgumentException("Fields count does not match constructor parameters count");
        }


        return schema -> {
            boolean primaryAlready = false;
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Type type = field.getType();
                String name = field.getName();
                String typeName = type.getTypeName().substring(type.getTypeName().lastIndexOf('.') + 1);
                Column column = null;

                if (field.isAnnotationPresent(Column.class)) {
                    column = field.getAnnotation(Column.class);
                }

                if (column != null && !column.type().isEmpty()) {
                    typeName = column.type();
                }

                if (column != null && !column.value().isEmpty()) {
                    name = column.value();
                }

                try {
                    schemaFromType(schema, typeName, name, field.get(data));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                if (column != null) {
                    if (column.primary() && !primaryAlready) {
                        primaryAlready = true;
                        schema.primary();
                    }
                    if (column.autoIncrement()) {
                        if (!type.getTypeName().equals("long")) {
                            throw new IllegalArgumentException("Auto increment is only available for long type");
                        }
                        schema.autoIncrement(column.value());
                    }
                    if (column.foreignKey()) {
                        if (column.foreignKeyReference().isEmpty()) {
                            throw new IllegalArgumentException("Foreign key reference is empty");
                        }
                        schema.foreignKey(column.foreignKeyReference());
                    }
                    if (column.nullable()) {
                        schema.nullable();
                    }
                }

                if (i == 0 && !primaryAlready) {
                    primaryAlready = true;
                    schema.primary();
                }
            }
        };
    }

    private static void schemaFromType(Schema schema, String type, String name, Object object) {
        switch (type.toLowerCase()) {
            case "string":
                if (object == null) {
                    schema.string(name, 255);
                    break;
                }
                schema.string(name, object.toString());
                break;
            case "longtext":
                schema.longText(name);
                break;
            case "integer":
            case "int":
            case "long":
            case "bigint":
                if (object == null) {
                    schema.bigInt(name);
                    break;
                }
                schema.bigInt(name, Long.parseLong(object.toString()));
                break;
            case "boolean":
                if (object == null) {
                    schema.bool(name);
                    break;
                }
                schema.bool(name, (Boolean) object);
                break;
            case "double":
            case "float":
            case "bigdecimal":
                if (object == null) {
                    schema.decimal(name);
                    break;
                }
                schema.decimal(name, (Double) object);
                break;
            case "uuid":
                if (object == null) {
                    schema.uuid(name);
                    break;
                }
                schema.uuid(name, (UUID) object);
                break;
            case "date":
                schema.date(name, (Date) object).nullable();
                break;
            case "timestamp":
                schema.timestamp(name).nullable();
                break;
            default:
                throw new IllegalArgumentException("Type " + type + " is not supported");
        }
    }

}
