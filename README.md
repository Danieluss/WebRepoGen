# WebRepoGen

Are you done with writing all that Controller and Repository nonsense in spring when all u really want to do is to do CRUD queries via REST to your DB?

I made this small module since spring roo doesn't seem to receive a lot of support and is getting deprecated.

Use this module to generate web controllers and jpa repositories from existing entities with a few simple annotations.


## Installation

Some simple package distribution is on its way, but for now:

```
git clone https://github.com/Danieluss/WebRepoGen.git
cd WebRepoGen
mvn clean install
```

Include artifact in pom.xml in your project, like this:

```
<dependency>
    <groupId>org.webrepogen</groupId>
    <artifactId>webrepogen</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage
```
package this-package;

@WebPackage //store generated web code in this-package.web
@RepoPackage //store generated repo code in this-package.repository
@AllEntities //generate repositories and controllers for all entities
public interface WebRepoGenConfig {
}

```

```
@GenerateWebRepository //used if there is no @AllEntities in your project
@Entity
public class A {
  ...
}
```
