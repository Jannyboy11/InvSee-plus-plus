# InvSee++

A bukkit plugin for manipulating player inventories.
This plugin will still work when target players are offline, even when they have never been on the server.

Do you like this plugin? Then please leave a rating on [SpigotMC](https://www.spigotmc.org/resources/invsee.82342/)!

### Commands
- `/invsee <userName>|<uniqueId>`
- `/endersee <userName>|<uniequeId>`

### Permissions

- `invseeplusplus.invsee` allows access to `/invsee`. By default only for server operators.
- `invseeplusplus.endersee` allows access to `/endersee`. By default only for server operators.

### Compiling

###### Prerequesites: [JDK-11](https://jdk.java.net/) or newer, [BuildTools](https://www.spigotmc.org/wiki/buildtools/) and [Maven](https://maven.apache.org).

1. Install Spigot into your local repository first by running BuildTools with
`java -jar BuildTools.jar --rev 1.16.1`.
2. In the root directory of the project run `mvn clean package`.
You can find the plugin jar at Spigot_Plugin/target/InvSee++.jar.

### License
2-Clause BSD. See the LICENSE.txt file.