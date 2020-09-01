# setupdb-maven-plugin

Deploy databases setups/updates with [Liquibase](https://www.liquibase.org/) and manage configuration dependencies.

You needs a Maven, Java 11+ project with at least a `database 
changelog.xml`.

`setupdb` calls will do recursively with all maven dependencies (configured with this setupdb plugin) from the caller project.

## Setup

Add to `pom.xml`

```xml
<plugin>
  <groupId>tv.hd3g.mvnplugin</groupId>
  <artifactId>setupdb</artifactId>
  <version>(last maven version)</version>
</plugin>
```

## Usage

```maven
mvn setupdb:deploy
```

For push current setup to database (eq. to Liquibase update).

```maven
mvn setupdb:dropall
```

For reverse all setups.

```maven
mvn setupdb:archive
```

For create an XML file with all setups (this current project and maven dependencies), usefull for automated setup scripts or CI/CD.

## Configuration

Default plugin configuration. Please edit (on add entry to the `<plugin>` node) or inject values as needed.

```xml
<plugin>
    <groupId>tv.hd3g.mvnplugin</groupId>
    <artifactId>setupdb</artifactId>
    <version>(last maven version)</version>
    <configuration>
        <!-- Global configuration, this should not be change -->
        <cacheRepository>${user.home}/.mvn-setupdb-cache</cacheRepository>
        <resolvedChangelog>${project.build.directory}/database-changelog-resolved.xml</resolvedChangelog>
        <archiveChangelog>${project.build.directory}/database-full-archive-changelog.xml</archiveChangelog>
        <liquibasePluginVersion>3.8.9</liquibasePluginVersion>
        <liquibaseVerbose>false</liquibaseVerbose>

        <!-- Specific configuration, you should be set this for your project -->
        <changeLog>scripts/db/database-changelog.xml</changeLog>
        <applicationconfig><!-- where to found application.yml|yaml|properties --></applicationconfig>
    </configuration>
<plugin>
```

- `<cacheRepository>` will store the git cache for resolved dependencies.
- `<resolvedChangelog>` where to create/read a full resolved changelog with all targeted changelogs with resolved dependencies.
- `<archiveChangelog>` used with `setupdb:archive`.
- `<liquibasePluginVersion>` the maven Liquibase plugin version to use.

`<changeLog>` where to found the main `database-changelog.xml` for this project.

`<applicationconfig>` is where to found SpringBoot application file. _It don't manage conf. profiles._

By default, it will search on many paths, modeled on SpringBoot behavior (like `/config`, `-Dspring.config.location`, `/src/test/resources/`...). **It will extract mandatories variables:**

- `spring.datasource.url` like `jdbc:mysql://localhost:3306/mydb`
- `spring.datasource.driver-class-name` like `com.mysql.cj.jdbc.Driver`
- `spring.datasource.username`
- `spring.datasource.password`

In another hand, you can inject values for this variables and/or mix with file configuration. You can add it in system properties and/or environment properties (usefull for inject passwords ouside of git files).

---

![Java CI with Maven](https://github.com/hdsdi3g/setupdb-maven-plugin/workflows/Java%20CI%20with%20Maven/badge.svg)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hdsdi3g_setupdb-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=hdsdi3g_setupdb-maven-plugin)

![CodeQL](https://github.com/hdsdi3g/setupdb-maven-plugin/workflows/CodeQL/badge.svg)

