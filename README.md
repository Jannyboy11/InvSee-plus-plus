# InvSee++

A bukkit plugin for manipulating player inventories.

![Logo](https://github.com/Jannyboy11/InvSee-plus-plus/blob/master/img/invsee6.png?raw=true)

This plugin will still work when target players are offline, even when they have never been on the server.

Do you like this plugin? Then please leave a rating anda review on [SpigotMC](https://www.spigotmc.org/resources/invsee.82342/)!

### Running the plugin

Just drop the InvSee++.jar file in your server's /plugins folder and make sure your servers runs on [Java 11](https://openjdk.java.net/projects/jdk/) or newer.

See also: [What Server Software does InvSee++ support?](#supported-server-software)

### Commands
- `/invsee <userName>|<uniqueId> [PWI{...}]`
- `/endersee <userName>|<uniequeId> [PWI{...}]`
Note that for integration with PerWorldInventory, `load-data-on-join` needs to be set to `true` in its config.

### Permissions

###### Base permissions:
- `invseeplusplus.invsee.view` allows access to `/invsee`. By default only for server operators.
- `invseeplusplus.invsee.edit` allows the player to manipulate the target player's inventory. By default only for server operators.
- `invseeplusplus.endersee.view` allows access to `/endersee`. By default only for server operators.
- `invseeplusplus.endersee.edit` allows the player to manipulate the target player's enderchest. By default only for server operators.
- `invseeplusplus.exempt.invsee` makes it impossible to spectate the inventory of the owner of this permission.
- `invseeplusplus.exempt.endersee` makes it impossible to spectate the enderchest of the owner of this permission.
- `invseeplusplus.bypass-exempt.invsee` ignore whether target players are exempted from having their inventory spectated
- `invseeplusplus.bypass-exempt.endersee` ignore whether target players are exempted from having their enderchest spectated

###### Aggregate permissions:
- `invseeplusplus.view` provides `invseeplusplus.invsee.view` and `invseeplusplus.endersee.view`.
- `invseeplusplus.edit` provides `invseeplusplus.invsee.edit` and `invseeplusplus.endersee.edit`.
- `invseeplusplus.exempt` provides `invseeplusplus.exempt.invsee` and `invseeplusplus.exempt.endersee`.
- `invseeplusplus.bypass-exempt` provides `invseeplusplus.bypass-exempt.invsee` and `invseeplusplus.bypass-exempt.endersee`.
- `invseeplusplus.*` provides all eight of the base permissions as well as all of the addon permissions.

## Addons

#### InvSee++_Give
##### Commands:
- `/invgive <player> <item type> [amount] [nbt tag]`
- `/endergive <player> <item type> [amount] [nbt tag]`
###### Examples:
- `/invgive Notch diamond 1 {"foo":"bar"}`
- `/endergive Jannyboy11 wool:14`
##### Permissions:
- `invseeplusplus.give.*` provides `invseeplusplus.give.inventory` and `invseeplusplus.give.enderchest`.
- `invseeplusplus.give.inventory` allows access to `/invgive`.
- `invseeplusplus.give.enderchest` allows access to `/endergive`.


#### InvSee++_Clear
##### Commands:
- `/invclear <player> [item type] [amount]`
- `/enderclear <player> [item type] [amount]`
###### Examples:
- `/invclear Notch diamond 1`
- `/enderclear Jannyboy11 wool:14`
##### Permissions:
- `invseeplusplus.clear.*` provides `invseeplusplus.clear.inventory` and `invseeplusplus.clear.enderchest`.
- `invseeplusplus.clear.inventory` allows access to `/invclear`.
- `invseeplusplus.clear.enderchest` allows access to `/enderclear`.

### Contact

Bugs & Feature requests: [GitHub issues](https://github.com/Jannyboy11/InvSee-plus-plus/issues)
Anything else can be discussed via the [discussion thread on SpigotMC](https://www.spigotmc.org/threads/invsee.456148/) or via
[Discord](https://discord.gg/Z8WCDHHcdJ).

### Compiling

###### Prerequisites: [JDK-17](https://jdk.java.net/) or newer, [BuildTools](https://www.spigotmc.org/wiki/buildtools/) and [Maven](https://maven.apache.org).

1. Install CraftBukkit into your local repository first by running BuildTools with
    - `java -jar BuildTools.jar --rev 1.8.8 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.12.2 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.15.2 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.16.5 --compile craftbukkit`
    - `java -jar BuildTools.jar --rev 1.17.1 --compile craftbukkit --remapped`
    - `java -jar BuildTools.jar --rev 1.18.2 --compile craftbukkit --remapped`
    - `java -jar BuildTools.jar --rev 1.19.2 --compile craftbukkit --remapped`
    - `java -jar BuildTools.jar --rev 1.19.3 --compile craftbukkit --remapped`
    - `java -jar BuildTools.jar --rev 1.19.4 --compile craftbukkit --remapped`
2. Install PerWorldInventory into your local repository by downloading [PerWorldInventory's code](https://github.com/Jannyboy11/perworldinventory-kt)
and running `mvn clean install -DskipTests=true` in its root directory.
3. In the root directory of this project run `mvn clean package`.
You can find the plugin jar at Spigot_Plugin/target/InvSee++.jar.

### Developers API
Documentation available on the [wiki](https://github.com/Jannyboy11/InvSee-plus-plus/wiki)!

### License
LGPLv2.1. See the LICENSE.txt file.

### Credits
Special thanks to Icodak ([Discord](https://discordapp.com/users/345308025331908619)) ([SpigotMC](https://www.spigotmc.org/members/icodak.473813/)) for creating the logo!

### Supported server software

InvSee++ supports servers implementing the [Bukkit](https://dev.bukkit.org) api which is currently maintained by [SpigotMC](https://spigotmc.org).
There are two types of support, Tier 1 support and Tier 2 support.
- Tier 1 support: I regularly test new versions of InvSee++ on this server software to make sure that it runs smooth.
- Tier 2 support: I don't test InvSee++ regularly on this server software, but will make an effort to fix bugs encountered when running on this server software when [an issue](https://github.com/Jannyboy11/InvSee-plus-plus/issues) is reported.

In general I support the latest patch release of popularly used Minecraft version, as well as multiple recent versions of the latest major release.

Server support matrix:
| Server Software            | 1.8.8         | 1.12.2        | 1.15.2  | 1.16.5  | 1.17.1 | 1.18.2 | 1.19.3 | 1.19.4 |
|----------------------------|---------------|---------------|---------|---------|--------|--------|--------|--------|
| CraftBukkit                | Tier 1        | Tier 1        | Tier 2  | Tier 2  | Tier 2 | Tier 2 | Tier 2 | Tier 1 |
| Paper                      | Tier 2        | Tier 2        | Tier 2  | Tier 2  | Tier 2 | Tier 2 | Tier 2 | Tier 2 |
| Folia                      | n/a           | n/a           | n/a     | n/a     | n/a    | n/a    | Tier 2 | Tier 2 |
| Other forks of CraftBukkit | Tier 2        | Tier 2        | Tier 2  | Tier 2  | Tier 2 | Tier 2 | Tier 2 | Tier 2 |
| Forge/Bukkit hybrids       | Tier 2*       | Tier 2*       | Tier 2* | Tier 2* | Tier 2 | Tier 2 | Tier 2 | Tier 2 |
| Fabric/Bukkit hybrids      | Tier 2*       | Tier 2*       | Tier 2* | Tier 2* | Tier 2 | Tier 2 | Tier 2 | Tier 2 |
| Glowstone                  | Not supported | [Planned](37) | n/a     | n/a     | n/a    | n/a    | n/a    | n/a    |

*The modding frameworks that these servers are based on were released at a time when Minecraft's minimum supported version was Java 8 (or lower),
and there is a good chance they won't be able to load mods and plugins compiled for newer Java versions.
Since I am not going to downgrade InvSee++ to Java 8, I won't fix any bugs related to the plugin loader not being able to load plugins compiled for Java 11.
The only hybrid server that I'm currently aware of that is taking effort to let their 1.12.2 server run on Java 11 (or newer) is [Magma](https://magmafoundation.org/), see [!571](https://git.magmafoundation.org/magmafoundation/Magma/-/merge_requests/571).

### Supported Java versions
| Minecraft version: | 1.8.x       | 1.12.x      | 1.15.x      | 1.16.x      | 1.17.x      | 1.18.x      | 1.19.x      |
|--------------------|-------------|-------------|-------------|-------------|-------------|-------------|-------------|
| Java version:      | 11 or newer | 11 or newer | 11 or newer | 11 or newer | 16 or newer | 17 or newer | 17 or newer |

[37]: https://github.com/Jannyboy11/InvSee-plus-plus/issues/37 "37"
