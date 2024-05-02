# Sarah - Java ORM

A library to easily manage your database

## Graddle

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
        implementation 'com.github.Maxlego08:Sarah:<version>'
}
```

## Maven

```xml

<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml

<dependency>
    <groupId>com.github.Maxlego08</groupId>
    <artifactId>Sarah</artifactId>
    <version><version></version>
</dependency>
```

## How to connect to the database ?

### With MYSQL

````java
public void connect(){
    DatabaseConfiguration configuration = DatabaseConfiguration.create(<user>, <password>, <port>, <host>, <database>);
    DatabaseConnection connection = new MySqlConnection(configuration);
}
````

### With SQLITE

````java
public void connect(){
    // The boolean allows to enable or not debug requests
    DatabaseConfiguration configuration = DatabaseConfiguration.sqlite(<boolean>);
    
    // The folder will be where the database.db file will be located
    DatabaseConnection connection = new SqliteConnection(configuration, <folder>);
}
````

## How to create a migration ?

Sarah will create a migrations table (you can change the name with the
method `` MigrationManager.setMigrationTableName``).
Each migration will be run only once.

Example
from [zEssentials](https://github.com/Maxlego08/zEssentials/blob/develop/Essentials/src/main/java/fr/maxlego08/essentials/database/migrations/CreateUserTableMigration.java):

````java
package fr.maxlego08.essentials.database.migrations;

import fr.maxlego08.sarah.SchemaBuilder;
import fr.maxlego08.sarah.database.Migration;

public class CreateUserTableMigration extends Migration {
    @Override
    public void up() {
        SchemaBuilder.create(this, "%prefix%users", table -> {
            table.uuid("unique_id").primary();
            table.string("name", 16);
            table.text("last_location").nullable();
            table.bigInt("play_time").defaultValue("0");
            table.timestamps();
        });
    }
}
````

You can prefix your tables if you need to.

You must then save your migration with the method ``MigrationManager.registerMigration``

After saving all your migrations, you must run them with the method ``MigrationManager.execute``
This method takes as parameter a SQL Java `Connection`, `DatabaseConfiguration` and a `Logger`
