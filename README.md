# InvSee++

A bukkit plugin for manipulating player inventories.

![Logo](https://github.com/Jannyboy11/InvSee-plus-plus/blob/master/img/invsee6.png?raw=true)

This plugin will still work when target players are offline, even when they have never been on the server.

Do you like this plugin? Then please leave a rating anda review on [SpigotMC](https://www.spigotmc.org/resources/invsee.82342/)!

### Running the plugin

Just drop the InvSee++.jar file in your server's /plugins folder and make sure your servers runs on [Java 11](https://openjdk.java.net/projects/jdk/) or newer.

### Commands
- `/invsee <userName>|<uniqueId>`
- `/endersee <userName>|<uniequeId>`

### Permissions

- `invseeplusplus.invsee.view` allows access to `/invsee`. By default only for server operators.
- `invseeplusplus.invsee.edit` allows the player to manipulate the target player's inventory. By default only for server operators.
- `invseeplusplus.endersee.view` allows access to `/endersee`. By default only for server operators.
- `invseeplusplus.endersee.edit` allows the player to manipulate the target player's enderchest. By default only for server operators.

###### Aggregate permissions:
- `invseeplusplus.invsee` provides `invseeplusplus.invsee.view` and `invseeplusplus.invsee.edit`
- `invseeplusplus.endersee` provides `invseeplusplus.endersee.view` and `invseeplusplus.endersee.edit`
- `invseeplusplus.view` provides `invseeplusplus.invsee.view` and `invseeplusplus.endersee.view`
- `invseeplusplus.edit` provides `invseeplusplus.invsee.edit` and `invseeplusplus.endersee.edit`
- `invseeplusplus.*` provides all four of the base permissions

### Compiling

###### Prerequesites: [JDK-11](https://jdk.java.net/) or newer, [BuildTools](https://www.spigotmc.org/wiki/buildtools/) and [Maven](https://maven.apache.org).

1. Install CraftBukkit into your local repository first by running BuildTools with
    - `java -jar BuildTools.jar --rev 1.15.2 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.16.1 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.16.3 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.16.5 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.17 --compile craftbukkit --remapped`
2. Install PerWorldInventory into your local repository by downloading [PerWorldInventory's code](https://github.com/Jannyboy11/perworldinventory-kt)
and running `mvn clean install -DskipTests=true` in its root directory.
3. Install a maven-shade-plugin into your local repository by downloading [maven-shade-plugin's code](https://github.com/apache/maven-shade-plugin)
and running `mvn clean install` in its root directory.
4. In the root directory of this project run `mvn clean package`.
You can find the plugin jar at Spigot_Plugin/target/InvSee++.jar.

### License
2-Clause BSD. See the LICENSE.txt file.

### Credits
Special thanks to Icodak ([Discord](https://discordapp.com/users/345308025331908619)) ([SpigotMC](https://www.spigotmc.org/members/icodak.473813/)) for creating the logo!