package net.tka.antivpn;

import java.util.List;

public record IpResult(String ip, int score, int flagged, int services, boolean vpn, boolean proxy, boolean tor, boolean datacenter, boolean hosting, boolean sharedIp, boolean geoJump, String country, String isp, String org, String reason, List<String> sources) {
   public boolean explicitAnonymizer() {
      return this.vpn || this.proxy || this.tor;
   }
}
