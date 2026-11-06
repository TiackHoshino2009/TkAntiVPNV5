package net.tka.antivpn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

public final class AntiVPNCommand implements CommandExecutor, TabCompleter {
   private final TkAntiVPN plugin;
   private final Database db;
   private final ServiceChecker checker;

   public AntiVPNCommand(TkAntiVPN p, Database d, ServiceChecker c) {
      this.plugin = p;
      this.db = d;
      this.checker = c;
   }

   public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
      if (a.length != 0 && !a[0].equalsIgnoreCase("help")) {
         if (a[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            s.sendMessage("§aTkAntiVPN config reloaded.");
            return true;
         } else if (a[0].equalsIgnoreCase("debug")) {
            this.plugin.getConfig().set("debug", !this.plugin.getConfig().getBoolean("debug"));
            this.plugin.saveConfig();
            FileConfiguration var10001 = this.plugin.getConfig();
            s.sendMessage("§eDebug: " + var10001.getBoolean("debug"));
            return true;
         } else if (a[0].equalsIgnoreCase("test") && a.length >= 2) {
            s.sendMessage("§7Checking " + a[1] + "...");
            this.checker.check(a[1]).orTimeout(8L, TimeUnit.SECONDS).whenComplete((r, e) -> {
               if (e != null) {
                  s.sendMessage("§cCheck failed: " + e.getMessage());
               } else {
                  int var10001 = r.score();
                  s.sendMessage("§aScore §f" + var10001 + " §7| flagged §f" + r.flagged() + "/" + r.services());
                  var10001 = r.vpn();
                  s.sendMessage("§7VPN=" + var10001 + " Proxy=" + r.proxy() + " Tor=" + r.tor() + " DC=" + r.datacenter() + " Hosting=" + r.hosting());
                  String var4 = r.country();
                  s.sendMessage("§7Country=" + var4 + " ISP=" + r.isp() + " Org=" + r.org());
                  s.sendMessage("§7Sources: " + String.join(", ", r.sources()));
               }
            });
            return true;
         } else if ((a[0].equalsIgnoreCase("whitelist") || a[0].equalsIgnoreCase("blacklist")) && a.length >= 3) {
            String path = a[0].toLowerCase(Locale.ROOT) + ".ips";
            List<String> list = new ArrayList(this.plugin.getConfig().getStringList(path));
            if (a[1].equalsIgnoreCase("add")) {
               if (!list.contains(a[2])) {
                  list.add(a[2]);
               }
            } else if (a[1].equalsIgnoreCase("remove")) {
               list.remove(a[2]);
            }

            this.plugin.getConfig().set(path, list);
            this.plugin.saveConfig();
            s.sendMessage("§aUpdated " + a[0] + " for " + a[2]);
            return true;
         } else {
            return true;
         }
      } else {
         s.sendMessage("/antivpn test <ip> | reload | debug | whitelist <add/remove> <ip> | blacklist <add/remove> <ip>");
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] args) {
      if (args.length == 1) {
         String q = args[0].toLowerCase(Locale.ROOT);
         return List.of("help", "test", "reload", "debug", "whitelist", "blacklist").stream().filter((x) -> x.startsWith(q)).toList();
      } else if (args.length != 2 || !args[0].equalsIgnoreCase("whitelist") && !args[0].equalsIgnoreCase("blacklist")) {
         return Collections.emptyList();
      } else {
         String q = args[1].toLowerCase(Locale.ROOT);
         return List.of("add", "remove").stream().filter((x) -> x.startsWith(q)).toList();
      }
   }
}
