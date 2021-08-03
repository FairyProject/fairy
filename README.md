<p align="center">
  <img width="20%" height="20%" src="https://i.imgur.com/CIxPcyV.png">
  <br>
  <a><img alt="Issues" src="https://img.shields.io/github/issues/FairyProject/fairy"></a>
  <a><img alt="Forks" src="https://img.shields.io/github/forks/FairyProject/fairy"></a>
  <a><img alt="Stars" src="https://img.shields.io/github/stars/FairyProject/fairy"></a>
  <a><img alt="License" src="https://img.shields.io/github/license/FairyProject/fairy"></a>
  <a><img alt="Authors" src="https://img.shields.io/badge/Authors-LeeGod-blue"></a>  
</p>

# Fairy Framework
Fairy is a compat &amp; open-sourced Framework made with love <3. Fairy is heavily inspired and uses components from Spring Boot. 
It's objective is to bring the beauty of dependency injection and CRUD based design patterns to various games, including Minecraft
via the Bukkit api. This project is a universal framework. 

Some notable features include the compatibility with most spring boot components, kindly ported over by the FairProject team. If you
would like to share a Fairy framework component, contact us via the issues tab!

Main Maintainer: Imanity Software

## Maven distribution
Get started by importing Fairy via the immanity repository!

```xml
<repository>
  <id>imanity-libraries</id>
  <url>https://maven.imanity.dev/repository/imanity-libraries/</url>
</repository>
```

```xml
<dependency>
  <groupId>org.fairy</groupId>
  <artifactId>bukkit-all</artifactId>
  <version>0.4b2</version>
  <scope>provided</scope>
</dependency>
```


## Quick initialization tutorial

Ever wanted to increase your productivity whilst writing [Bukkit](https://bukkit.org) plugins? Here's an example! 

```java
@Plugin(
        name = "test",
        version = "1.0.0",
        description = "test",
        load = PluginLoadOrder.POSTWORLD,
        authors = {"Author"},
        type = PluginType.BUKKIT
)
@ClasspathScan("me.test.testplugin") // Replace it with your package name
public class Test extends BukkitPlugin {

    @Override
    public void onPreEnable() {
        // Before Fairy initalize this plugin
    }

    @Override
    public void onPluginEnable() {
        // After Fairy initalize to this plugin
    }

    @Override
    public void onPluginDisable() {
        // Plugin shutdown, and Before Fairy shut down
    }

    @Override
    public void onFrameworkFullyDisable() {
        // After Fairy fully shut down
    }

}
```

### Simple command example

Here's an example of what a simply command looks like!

```java
@Component
public class TestCommand implements CommandHolder {

    @Command(names = {"test"}, permissionNode = "test.test")
    public void handle(final BukkitCommandEvent event) {
        event.getSender().sendMessage("Test command");
    }
}
```

Simple as that! 
