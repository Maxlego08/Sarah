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

dependencies {
        implementation 'com.github.Maxlego08:Sarah:1.0'
}
```

## Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependency>
    <dependencies>
        <groupId>com.github.Maxlego08</groupId>
        <artifactId>Sarah</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```

## How to create a migration ?

Sarah will create a migrations table (you can change the name with the method `` MigrationManager.setMigrationTableName``).
Each migration will be run only once.

Example from [zEssentials](https://github.com/Maxlego08/zEssentials/blob/develop/Essentials/src/main/java/fr/maxlego08/essentials/database/migrations/CreateUserTableMigration.java):
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
