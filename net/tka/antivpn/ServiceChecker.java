package net.tka.antivpn;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.tka.antivpn.lib.json.JSONObject;
import net.tka.antivpn.lib.json.parser.JSONParser;
import org.bukkit.configuration.file.FileConfiguration;

public final class ServiceChecker {
   private final TkAntiVPN plugin;
   private final HttpClient http;
   private final ExecutorService pool;

   public ServiceChecker(TkAntiVPN plugin) {
      this.plugin = plugin;
      this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).build();
      this.pool = Executors.newFixedThreadPool(8, (r) -> {
         Thread t = new Thread(r, "TkAntiVPN-HTTP");
         t.setDaemon(true);
         return t;
      });
   }

   public CompletableFuture<IpResult> check(String ip) {
      return CompletableFuture.supplyAsync(() -> this.run(ip), this.pool);
   }

   private IpResult run(String ip) {
      List<Signal> signals = new ArrayList();
      this.addIpApi(ip, signals);
      this.addProxyCheck(ip, signals);
      this.addVpnApi(ip, signals);
      this.addIpHub(ip, signals);
      this.addGetIpIntel(ip, signals);
      this.addIpHunter(ip, signals);
      this.addIpQualityScore(ip, signals);
      int flagged = 0;
      int score = 0;
      boolean vpn = false;
      boolean proxy = false;
      boolean tor = false;
      boolean dc = false;
      boolean hosting = false;
      String country = "Unknown";
      String isp = "Unknown";
      String org = "Unknown";
      List<String> sources = new ArrayList();

      for(Signal s : signals) {
         if (s.flagged) {
            ++flagged;
         }

         score += s.score;
         vpn |= s.vpn;
         proxy |= s.proxy;
         tor |= s.tor;
         dc |= s.datacenter;
         hosting |= s.hosting;
         if (!s.country.equals("Unknown")) {
            country = s.country;
         }

         if (!s.isp.equals("Unknown")) {
            isp = s.isp;
         }

         if (!s.org.equals("Unknown")) {
            org = s.org;
         }

         if (s.flagged) {
            sources.add(s.name);
         }
      }

      score = Math.min(100, score);
      return new IpResult(ip, score, flagged, signals.size(), vpn, proxy, tor, dc, hosting, false, false, country, isp, org, sources.isEmpty() ? "No service flagged" : String.join(", ", sources), sources);
   }

   private void addIpApi(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.ip-api.enabled", true)) {
         try {
            JSONObject j = this.get(c.getString("services.ip-api.url", "").replace("%ip%", ip));
            if (!"success".equalsIgnoreCase(str(j, "status", "fail"))) {
               return;
            }

            boolean proxy = bool(j, "proxy");
            boolean hosting = bool(j, "hosting");
            String isp = str(j, "isp", "Unknown");
            String org = str(j, "org", str(j, "as", "Unknown"));
            String combo = (isp + " " + org).toLowerCase(Locale.ROOT);
            boolean dc = hosting || containsAny(combo, "amazon", "aws", "digitalocean", "linode", "ovh", "hetzner", "m247", "vultr", "contabo", "leaseweb", "choopa");
            int sc = proxy ? this.getInt("detection.weights.proxy", 85) : (dc ? this.getInt("detection.weights.datacenter", 25) : 0);
            out.add(new Signal("IP-API", proxy, proxy, false, false, dc, hosting, sc, str(j, "countryCode", "Unknown"), isp, org));
         } catch (Exception e) {
            this.plugin.debug("IP-API", e);
         }

      }
   }

   private void addProxyCheck(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.proxycheck.enabled", true)) {
         try {
            String url = c.getString("services.proxycheck.url", "").replace("%ip%", ip).replace("%key%", c.getString("services.proxycheck.key", ""));
            JSONObject root = this.get(url);
            JSONObject j = obj(root, ip);
            String type = str(j, "type", "");
            String proxyS = str(j, "proxy", "no");
            boolean proxy = "yes".equalsIgnoreCase(proxyS) || "yes".equalsIgnoreCase(str(j, "vpn", "no"));
            boolean vpn = type.equalsIgnoreCase("vpn") || "vps".equalsIgnoreCase(type) || "proxy".equalsIgnoreCase(type) && proxy;
            boolean tor = type.toLowerCase(Locale.ROOT).contains("tor") || "yes".equalsIgnoreCase(str(j, "tor", "no"));
            int risk = number(j, "risk", 0);
            boolean dc = "hosting".equalsIgnoreCase(type) || "vps".equalsIgnoreCase(type);
            int sc = (vpn ? this.getInt("detection.weights.vpn", 90) : 0) + (tor ? this.getInt("detection.weights.tor", 100) : 0) + (proxy && !vpn ? this.getInt("detection.weights.proxy", 85) : 0);
            if (risk >= 70) {
               sc += this.getInt("detection.weights.risk", 20);
            }

            if (dc && !vpn && !proxy) {
               sc += this.getInt("detection.weights.datacenter", 25);
            }

            out.add(new Signal("ProxyCheck", vpn || proxy || tor, vpn, proxy, tor, dc, dc, Math.min(100, sc), str(j, "country", "Unknown"), str(j, "provider", "Unknown"), str(j, "operator", "Unknown")));
         } catch (Exception e) {
            this.plugin.debug("ProxyCheck", e);
         }

      }
   }

   private void addVpnApi(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.vpnapi.enabled", true)) {
         try {
            String url = c.getString("services.vpnapi.url", "").replace("%ip%", ip).replace("%key%", c.getString("services.vpnapi.key", ""));
            JSONObject j = this.get(url);
            JSONObject sec = obj(j, "security");
            JSONObject net = obj(j, "network");
            JSONObject loc = obj(j, "location");
            boolean vpn = bool(sec, "vpn");
            boolean proxy = bool(sec, "proxy");
            boolean tor = bool(sec, "tor");
            boolean dc = bool(sec, "hosting") || bool(sec, "cloud_provider");
            int sc = (vpn ? this.getInt("detection.weights.vpn", 90) : 0) + (proxy ? this.getInt("detection.weights.proxy", 85) : 0) + (tor ? this.getInt("detection.weights.tor", 100) : 0);
            if (dc && !vpn && !proxy) {
               sc += this.getInt("detection.weights.datacenter", 25);
            }

            out.add(new Signal("VPNAPI", vpn || proxy || tor, vpn, proxy, tor, dc, dc, Math.min(100, sc), str(loc, "country_code", "Unknown"), str(net, "network", "Unknown"), str(net, "autonomous_system_organization", "Unknown")));
         } catch (Exception e) {
            this.plugin.debug("VPNAPI", e);
         }

      }
   }

   private void addIpHub(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.iphub.enabled", false)) {
         try {
            String key = c.getString("services.iphub.key", "");
            String url = c.getString("services.iphub.url", "").replace("%ip%", ip);
            HttpRequest q = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(3L)).header("X-Key", key).GET().build();
            JSONObject j = json((String)this.http.send(q, BodyHandlers.ofString()).body());
            int block = number(j, "block", 0);
            boolean flagged = block > 0;
            boolean dc = block >= 1;
            int sc = flagged ? this.getInt("detection.weights.proxy", 85) : 0;
            out.add(new Signal("IPHub", flagged, false, flagged, false, dc, dc, sc, str(j, "countryCode", "Unknown"), str(j, "isp", "Unknown"), str(j, "asn", "Unknown")));
         } catch (Exception e) {
            this.plugin.debug("IPHub", e);
         }

      }
   }

   private void addGetIpIntel(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.getipintel.enabled", false)) {
         try {
            String url = c.getString("services.getipintel.url", "").replace("%ip%", ip).replace("%email%", c.getString("services.getipintel.email", ""));
            JSONObject j = this.get(url);
            double score = Double.parseDouble(str(j, "result", "0"));
            boolean flagged = score >= 0.85;
            out.add(new Signal("GetIPIntel", flagged, flagged, false, false, false, false, flagged ? this.getInt("detection.weights.proxy", 85) : 0, "Unknown", "Unknown", "Unknown"));
         } catch (Exception e) {
            this.plugin.debug("GetIPIntel", e);
         }

      }
   }

   private void addIpHunter(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.iphunter.enabled", false)) {
         try {
            String url = c.getString("services.iphunter.url", "").replace("%ip%", ip).replace("%key%", c.getString("services.iphunter.key", ""));
            JSONObject j = this.get(url);
            int block = number(j, "block", 0);
            boolean flagged = block > 0;
            out.add(new Signal("IPHunter", flagged, flagged, false, false, block >= 1, block >= 1, flagged ? this.getInt("detection.weights.proxy", 85) : 0, str(j, "countryCode", "Unknown"), str(j, "isp", "Unknown"), str(j, "org", "Unknown")));
         } catch (Exception e) {
            this.plugin.debug("IPHunter", e);
         }

      }
   }

   private void addIpQualityScore(String ip, List<Signal> out) {
      FileConfiguration c = this.plugin.getConfig();
      if (c.getBoolean("services.ipqualityscore.enabled", false)) {
         try {
            String url = c.getString("services.ipqualityscore.url", "").replace("%ip%", ip).replace("%key%", c.getString("services.ipqualityscore.key", ""));
            JSONObject j = this.get(url);
            boolean vpn = bool(j, "vpn");
            boolean proxy = bool(j, "proxy");
            boolean tor = bool(j, "tor");
            boolean bot = bool(j, "bot_status");
            int fraud = number(j, "fraud_score", 0);
            boolean flagged = vpn || proxy || tor || fraud >= c.getInt("services.ipqualityscore.fraud-score", 85);
            int sc = (vpn ? this.getInt("detection.weights.vpn", 90) : 0) + (proxy ? this.getInt("detection.weights.proxy", 85) : 0) + (tor ? this.getInt("detection.weights.tor", 100) : 0) + (fraud >= 85 ? this.getInt("detection.weights.risk", 20) : 0);
            out.add(new Signal("IPQualityScore", flagged, vpn, proxy, tor, false, false, Math.min(100, sc), str(j, "country_code", "Unknown"), str(j, "ISP", "Unknown"), str(j, "organization", "Unknown")));
         } catch (Exception e) {
            this.plugin.debug("IPQualityScore", e);
         }

      }
   }

   private JSONObject get(String url) throws Exception {
      if (url != null && !url.isBlank()) {
         HttpRequest q = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(3L)).header("User-Agent", "TkAntiVPNV5/5.0").GET().build();
         HttpResponse<String> r = this.http.send(q, BodyHandlers.ofString());
         if (r.statusCode() / 100 != 2) {
            throw new IllegalStateException("HTTP " + r.statusCode());
         } else {
            return json((String)r.body());
         }
      } else {
         throw new IllegalArgumentException("empty URL");
      }
   }

   private static JSONObject json(String s) throws Exception {
      Object x = (new JSONParser()).parse(s);
      return x instanceof JSONObject ? (JSONObject)x : new JSONObject();
   }

   private static JSONObject obj(JSONObject j, String k) {
      Object x = j.get(k);
      return x instanceof JSONObject ? (JSONObject)x : new JSONObject();
   }

   private static String str(JSONObject j, String k, String d) {
      Object x = j.get(k);
      return x == null ? d : String.valueOf(x);
   }

   private static boolean bool(JSONObject j, String k) {
      Object x = j.get(k);
      return Boolean.TRUE.equals(x) || "true".equalsIgnoreCase(String.valueOf(x)) || "yes".equalsIgnoreCase(String.valueOf(x));
   }

   private static int number(JSONObject j, String k, int d) {
      try {
         return Integer.parseInt(str(j, k, String.valueOf(d)));
      } catch (Exception var6) {
         try {
            return (int)Double.parseDouble(str(j, k, String.valueOf(d)));
         } catch (Exception var5) {
            return d;
         }
      }
   }

   private static boolean containsAny(String s, String... a) {
      for(String x : a) {
         if (s.contains(x)) {
            return true;
         }
      }

      return false;
   }

   private int getInt(String path, int d) {
      return this.plugin.getConfig().getInt(path, d);
   }

   private void debug(String n, Exception e) {
      this.plugin.debug(n, e);
   }

   public void shutdown() {
      this.pool.shutdownNow();
   }

   private static record Signal(String name, boolean flagged, boolean vpn, boolean proxy, boolean tor, boolean datacenter, boolean hosting, int score, String country, String isp, String org) {
   }
}
