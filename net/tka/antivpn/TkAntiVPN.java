package net.tka.antivpn;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TkAntiVPN extends JavaPlugin {
   private Database database;
   private ServiceChecker checker;
   private final Map<String, Long> ipConnections = new ConcurrentHashMap();

   public void onEnable() {
      this.saveDefaultConfig();
      this.database = new Database(this);
      this.checker = new ServiceChecker(this);
      this.getServer().getPluginManager().registerEvents(new VerificationListener(this, this.database, this.checker, this.ipConnections), this);
      AntiVPNCommand cmd = new AntiVPNCommand(this, this.database, this.checker);
      ((PluginCommand)Objects.requireNonNull(this.getCommand("antivpn"))).setExecutor(cmd);
      ((PluginCommand)Objects.requireNonNull(this.getCommand("antivpn"))).setTabCompleter(cmd);
      this.getLogger().info("TkAntiVPN 4.0.0 enabled. Default action: KICK.");
   }

   public void onDisable() {
      if (this.checker != null) {
         this.checker.shutdown();
      }

      if (this.database != null) {
         this.database.close();
      }

   }

   void debug(String tag, Exception e) {
      if (this.getConfig().getBoolean("debug", false)) {
         this.getLogger().warning("[DEBUG] " + tag + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }

   }

   public Database db() {
      return this.database;
   }
}
