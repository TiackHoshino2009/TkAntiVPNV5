package net.tka.antivpn;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class DiscordNotifier {
   private DiscordNotifier() {
   }

   public static void send(TkAntiVPN p, String player, IpResult r) {
      if (p.getConfig().getBoolean("actions.discord.enabled", true)) {
         String url = p.getConfig().getString("actions.discord.webhook", "");
         if (url != null && !url.isBlank()) {
            String msg = p.getConfig().getString("actions.discord.alert-message", "");
            msg = VerificationListener.fill(msg, player, r);
            String user = p.getConfig().getString("actions.discord.username", "TkAntiVPN");
            String var10000 = q(user);
            String body = "{\"username\":" + var10000 + ",\"content\":" + q(msg) + "}";
            CompletableFuture.runAsync(() -> {
               try {
                  HttpRequest req = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json").timeout(Duration.ofSeconds(5L)).POST(BodyPublishers.ofString(body)).build();
                  HttpResponse<String> resp = HttpClient.newHttpClient().send(req, BodyHandlers.ofString());
                  if (resp.statusCode() / 100 != 2) {
                     p.getLogger().warning("Discord webhook HTTP " + resp.statusCode());
                  }
               } catch (Exception e) {
                  p.debug("Discord webhook", e);
               }

            });
         }
      }
   }

   private static String q(String s) {
      String var10000 = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
      return "\"" + var10000.replace("\r", "\\r") + "\"";
   }
}
