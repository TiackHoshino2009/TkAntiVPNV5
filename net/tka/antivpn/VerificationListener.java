package net.tka.antivpn;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public final class VerificationListener implements Listener {
   private final TkAntiVPN plugin;
   private final Database db;
   private final ServiceChecker checker;
   private final Map<String, Long> connectionWindow;
   private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

   public VerificationListener(TkAntiVPN p, Database db, ServiceChecker c, Map<String, Long> window) {
      this.plugin = p;
      this.db = db;
      this.checker = c;
      this.connectionWindow = window;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPreLogin(AsyncPlayerPreLoginEvent e) {
      if (e.getLoginResult() == Result.ALLOWED) {
         String ip = e.getAddress().getHostAddress();
         String uuid = e.getUniqueId().toString();
         if (!this.isWhitelisted(ip, uuid)) {
            if (this.isBlacklisted(ip, uuid)) {
               this.kick(e, "&cBạn đang nằm trong blacklist của TkAntiVPN.");
            } else {
               int max = this.plugin.getConfig().getInt("connection.maximum-per-ip", 3);
               long now = System.currentTimeMillis();
               long window = this.plugin.getConfig().getLong("connection.window-seconds", 60L) * 1000L;
               this.connectionWindow.entrySet().removeIf((x) -> now - (Long)x.getValue() > window);
               long count = this.connectionWindow.keySet().stream().filter((x) -> x.startsWith(ip + ":")).count();
               this.connectionWindow.put(ip + ":" + String.valueOf(e.getUniqueId()), now);
               if (count >= (long)max) {
                  this.kick(e, "&cCó quá nhiều kết nối từ IP này.");
               } else {
                  IpResult cached = this.plugin.getConfig().getBoolean("cache.enabled", true) ? this.db.get(ip, this.plugin.getConfig().getLong("cache.hours", 24L) * 3600000L) : null;

                  IpResult result;
                  try {
                     result = cached != null ? cached : (IpResult)this.checker.check(ip).get(7L, TimeUnit.SECONDS);
                  } catch (Exception ex) {
                     this.plugin.debug("check", ex);
                     this.plugin.getLogger().warning("IP check failed for " + ip + "; allowing player instead of false-kicking.");
                     return;
                  }

                  String previous = this.db.lastCountry(e.getUniqueId());
                  boolean geoJump = previous != null && !previous.equalsIgnoreCase("Unknown") && !result.country().equalsIgnoreCase("Unknown") && !previous.equalsIgnoreCase(result.country());
                  boolean sharedIp = count >= 2L;
                  int extra = (geoJump ? this.plugin.getConfig().getInt("detection.weights.geo-jump", 10) : 0) + (sharedIp ? this.plugin.getConfig().getInt("detection.weights.shared-ip", 15) : 0);
                  String extraReason = (geoJump ? ", geo-jump" : "") + (sharedIp ? ", shared-ip" : "");
                  result = new IpResult(result.ip(), Math.min(100, result.score() + extra), result.flagged(), result.services(), result.vpn(), result.proxy(), result.tor(), result.datacenter(), result.hosting(), sharedIp, geoJump, result.country(), result.isp(), result.org(), result.reason() + extraReason, result.sources());
                  this.db.saveHistory(e.getUniqueId(), ip, result.country());
                  if (cached == null) {
                     this.db.put(result);
                  }

                  int threshold = this.plugin.getConfig().getInt("detection.kick-threshold", 75);
                  boolean explicit = result.explicitAnonymizer();
                  boolean kick = result.score() >= threshold;
                  if (this.plugin.getConfig().getBoolean("detection.safe-mode", true) && this.plugin.getConfig().getBoolean("detection.require-explicit-anonymizer", true)) {
                     kick = kick && explicit;
                  }

                  if (!kick && result.flagged() >= 2 && explicit) {
                     kick = true;
                  }

                  if (kick && this.plugin.getConfig().getBoolean("detection.second-check-before-kick", true)) {
                     try {
                        IpResult second = (IpResult)this.checker.check(ip).get(7L, TimeUnit.SECONDS);
                        boolean secondExplicit = second.explicitAnonymizer();
                        kick = second.score() >= threshold && secondExplicit;
                        if (this.plugin.getConfig().getBoolean("detection.safe-mode", true) && this.plugin.getConfig().getBoolean("detection.require-explicit-anonymizer", true)) {
                           kick = kick && secondExplicit;
                        }

                        result = second;
                     } catch (Exception ex) {
                        this.plugin.debug("second-check", ex);
                        kick = false;
                     }
                  }

                  if (kick) {
                     this.kick(e, this.plugin.getConfig().getString("actions.kick.message", "&cVPN/Proxy/Tor không được phép."));
                     this.notifyStaff(e.getName(), result);
                     DiscordNotifier.send(this.plugin, e.getName(), result);
                  }

               }
            }
         }
      }
   }

   private void kick(AsyncPlayerPreLoginEvent e, String msg) {
      e.setLoginResult(Result.KICK_OTHER);
      e.setKickMessage(color(msg));
   }

   private void notifyStaff(String name, IpResult r) {
      if (this.plugin.getConfig().getBoolean("actions.notify.enabled", true)) {
         String m = this.plugin.getConfig().getString("actions.notify.message", "");
         m = fill(m, name, r);
         Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.getOnlinePlayers().stream().filter((p) -> p.hasPermission("antivpn.notify")).forEach((p) -> p.sendMessage(color(m))));
      }
   }

   private boolean isWhitelisted(String ip, String uuid) {
      return this.plugin.getConfig().getStringList("whitelist.ips").stream().anyMatch((x) -> x.equals(ip) || wild(x, ip)) || this.plugin.getConfig().getStringList("whitelist.players").contains(uuid);
   }

   private boolean isBlacklisted(String ip, String uuid) {
      return this.plugin.getConfig().getStringList("blacklist.ips").stream().anyMatch((x) -> x.equals(ip) || wild(x, ip)) || this.plugin.getConfig().getStringList("blacklist.players").contains(uuid);
   }

   private static boolean wild(String p, String s) {
      return p.contains("*") && s.matches(Pattern.quote(p).replace("\\*", ".*"));
   }

   static String fill(String s, String name, IpResult r) {
      return s.replace("%player%", name).replace("%ip%", r.ip()).replace("%country%", r.country()).replace("%isp%", r.isp()).replace("%org%", r.org()).replace("%score%", String.valueOf(r.score())).replace("%flagged%", String.valueOf(r.flagged())).replace("%services%", String.valueOf(r.services())).replace("%reason%", r.reason());
   }

   static String color(String s) {
      return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
   }
}
