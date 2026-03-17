# Contributing
1. Any features added should have a config flag added to `ConfigManager.java`
```java
public boolean isCreeperGriefingDisabled() {
    return config.getOrElse("features.mobs.disable_creeper_griefing", true);
}
```

2. Add the feature to `resources/default_config.yml`
```toml
[features.mobs]
disable_creeper_griefing = true
```

3. add a guard-clause to the event handler(s) you're targetting so it can be disabled at runtime.
```java
@EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!configManager.isCreeperGriefingDisabled()) return;
```


# Features
## Mobs
* creeper griefing is disbaled
* creepers ignite when they take fire damage
* ghasts drop more loot when they get hit with their fireball

## Backup
* Player data backups occur when they die or the server restarts

## Commands
* homebase -> /home command that sends you back to where you'd respawn, cancelled when damage is taken
* config, built in with no way to disable it

## Enchantments
* Harvesting -> 12.5% per level up to 50% more crop drops

## Misc
* Bedrock armorstand emulation. Just right click with a stick to add arms, and shift+click to cycle poses.
