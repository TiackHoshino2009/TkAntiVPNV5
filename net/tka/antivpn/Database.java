package net.tka.antivpn;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class Database {
   private final TkAntiVPN plugin;
   private Connection c;

   public Database(TkAntiVPN plugin) {
      this.plugin = plugin;
   }

   public synchronized void open() throws SQLException {
      if (this.c != null) {
         try {
            if (!this.c.isClosed()) {
               return;
            }
         } catch (SQLException var6) {
         }
      }

      File f = new File(this.plugin.getDataFolder(), "antivpn.db");
      f.getParentFile().mkdirs();
      this.c = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
      Statement s = this.c.createStatement();

      try {
         s.executeUpdate("CREATE TABLE IF NOT EXISTS cache (ip TEXT PRIMARY KEY, checked INTEGER, score INTEGER, flagged INTEGER, services INTEGER, vpn INTEGER, proxy INTEGER, tor INTEGER, datacenter INTEGER, hosting INTEGER, shared_ip INTEGER, geo_jump INTEGER, country TEXT, isp TEXT, org TEXT, reason TEXT, sources TEXT)");
         s.executeUpdate("CREATE TABLE IF NOT EXISTS history (uuid TEXT, ip TEXT, country TEXT, checked INTEGER, PRIMARY KEY(uuid))");
      } catch (Throwable var7) {
         if (s != null) {
            try {
               s.close();
            } catch (Throwable var5) {
               var7.addSuppressed(var5);
            }
         }

         throw var7;
      }

      if (s != null) {
         s.close();
      }

   }

   public synchronized IpResult get(String ip, long maxAgeMs) {
      try {
         this.open();
         PreparedStatement p = this.c.prepareStatement("SELECT * FROM cache WHERE ip=?");

         IpResult var6;
         label80: {
            try {
               p.setString(1, ip);
               ResultSet r = p.executeQuery();

               label82: {
                  try {
                     if (!r.next() || System.currentTimeMillis() - r.getLong("checked") > maxAgeMs) {
                        var6 = null;
                        break label82;
                     }

                     var6 = this.row(r, ip);
                  } catch (Throwable var10) {
                     if (r != null) {
                        try {
                           r.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (r != null) {
                     r.close();
                  }
                  break label80;
               }

               if (r != null) {
                  r.close();
               }
            } catch (Throwable var11) {
               if (p != null) {
                  try {
                     p.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (p != null) {
               p.close();
            }

            return var6;
         }

         if (p != null) {
            p.close();
         }

         return var6;
      } catch (Exception e) {
         this.plugin.debug("DB get", e);
         return null;
      }
   }

   public synchronized void put(IpResult x) {
      try {
         this.open();
         PreparedStatement p = this.c.prepareStatement("INSERT OR REPLACE INTO cache VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

         try {
            int i = 1;
            p.setString(i++, x.ip());
            p.setLong(i++, System.currentTimeMillis());
            p.setInt(i++, x.score());
            p.setInt(i++, x.flagged());
            p.setInt(i++, x.services());
            p.setBoolean(i++, x.vpn());
            p.setBoolean(i++, x.proxy());
            p.setBoolean(i++, x.tor());
            p.setBoolean(i++, x.datacenter());
            p.setBoolean(i++, x.hosting());
            p.setBoolean(i++, x.sharedIp());
            p.setBoolean(i++, x.geoJump());
            p.setString(i++, x.country());
            p.setString(i++, x.isp());
            p.setString(i++, x.org());
            p.setString(i++, x.reason());
            p.setString(i++, String.join(",", x.sources()));
            p.executeUpdate();
         } catch (Throwable var6) {
            if (p != null) {
               try {
                  p.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (p != null) {
            p.close();
         }
      } catch (Exception e) {
         this.plugin.debug("DB put", e);
      }

   }

   public synchronized String lastCountry(UUID uuid) {
      try {
         this.open();
         PreparedStatement p = this.c.prepareStatement("SELECT country FROM history WHERE uuid=?");

         String var4;
         try {
            p.setString(1, uuid.toString());
            ResultSet r = p.executeQuery();

            try {
               var4 = r.next() ? r.getString(1) : null;
            } catch (Throwable var8) {
               if (r != null) {
                  try {
                     r.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (r != null) {
               r.close();
            }
         } catch (Throwable var9) {
            if (p != null) {
               try {
                  p.close();
               } catch (Throwable var6) {
                  var9.addSuppressed(var6);
               }
            }

            throw var9;
         }

         if (p != null) {
            p.close();
         }

         return var4;
      } catch (Exception var10) {
         return null;
      }
   }

   public synchronized void saveHistory(UUID uuid, String ip, String country) {
      try {
         this.open();
         PreparedStatement p = this.c.prepareStatement("INSERT OR REPLACE INTO history VALUES(?,?,?,?)");

         try {
            p.setString(1, uuid.toString());
            p.setString(2, ip);
            p.setString(3, country);
            p.setLong(4, System.currentTimeMillis());
            p.executeUpdate();
         } catch (Throwable var8) {
            if (p != null) {
               try {
                  p.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (p != null) {
            p.close();
         }
      } catch (Exception e) {
         this.plugin.debug("DB history", e);
      }

   }

   private IpResult row(ResultSet r, String ip) throws SQLException {
      String src = r.getString("sources");
      return new IpResult(ip, r.getInt("score"), r.getInt("flagged"), r.getInt("services"), r.getBoolean("vpn"), r.getBoolean("proxy"), r.getBoolean("tor"), r.getBoolean("datacenter"), r.getBoolean("hosting"), r.getBoolean("shared_ip"), r.getBoolean("geo_jump"), r.getString("country"), r.getString("isp"), r.getString("org"), r.getString("reason"), src != null && !src.isBlank() ? Arrays.asList(src.split(",")) : List.of());
   }

   public synchronized void close() {
      try {
         if (this.c != null) {
            this.c.close();
         }
      } catch (Exception var2) {
      }

   }
}
