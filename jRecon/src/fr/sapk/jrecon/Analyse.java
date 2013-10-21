/*
 This file is part of jRecon.

 jRecon is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jRecon is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with jRecon.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.sapk.jrecon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antoine
 */
public class Analyse extends Thread {

    private static String name;
    protected static String state = "Not initialized";
    protected static long request_done = 0;
    protected static long request_total = 0;
    private static long timestamp;
    private static String target;
    private static String port;
    private static String limit;
    private static String checkdns;
    private static int id;

    public Analyse(String[] params) {
        System.out.println("Init analyse");

        for (String s : params) {
            System.out.print("#" + s);
        }
        System.out.println("");

        name = params[0] + " # " + (new Date()).toString();
        target = params[1];
        port = params[2];
        limit = params[3];
        checkdns = params[4];
        timestamp = System.currentTimeMillis();

        request_done = 0;
        request_total = 0;
        if (Tool.is_network(target)) {
            request_total += Math.pow(2, 32 - Integer.parseInt(target.split("/")[1]));
        } else {
            request_total += 1;
        }

        request_total = (request_total <= 0) ? 1 : request_total;

        double multi = 0;
        if (port != null && port.split("-").length == 2) {
            multi += (Integer.parseInt(port.split("-")[1]) - Integer.parseInt(port.split("-")[0]) + 1);
        }
        if (checkdns == "true") {
            multi += 1;
        }

        //System.out.println(multi);
        //request étati multiplié par 10 pour les traceroute (moyenne)
        request_total = (long) (10 * request_total + (request_total) * multi);
        /*
         if (port != null && port.split("-").length == 2) {
         request_total += request_total * 10 + request_total * (Integer.parseInt(port.split("-")[1]) - Integer.parseInt(port.split("-")[0]) + 1);
         } else {
         request_total += request_total * 10;
         }
         */
        try {
            DB.exec("INSERT INTO analyse ('state', 'name', 'target', 'port' , 'limit', 'checkdns', 'timestamp') VALUES ('Running', '" + name + "', '" + target + "', '" + port + "', '" + limit + "', '" + checkdns + "', '" + timestamp + "') ");
            ResultSet ret = DB.query("SELECT * FROM analyse WHERE name='" + name + "' ");
            id = ret.getInt("id_analyse");
            System.out.println("id analyse : " + id);
            //System.out.println(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        state = "Initialized";
    }

    @Override
    public void run() {
        System.out.println("Starting analyse ...");
        state = "Populating ...";
        populate_db();
        state = "Running ...";
        recon();
        state = "Finished !";
    }

    private void populate_db() {
        if (Tool.is_network(target)) {
            //TODO populate db with all ip possible.
            String ip_host = target.split("/")[0];
            String masque = target.split("/")[1];
            System.out.println(ip_host + " " + masque);
            long num_network_size = (long) (Math.pow(2, 32 - Integer.parseInt(masque)));
            long num_ip_host = Tool.IPtoLong(ip_host);
            long num_ip_reseau = num_ip_host - (num_ip_host % (num_network_size));
            long num_ip_broadcast = num_ip_reseau + num_network_size - 1;

            for (long i = num_ip_reseau; i <= num_ip_broadcast; i++) {
                String ip = Tool.LongtoIPv4(i);
                //   System.out.println(ip);
                String hostname = ip;
                if (checkdns == "true") {
                    try {
                        hostname = Tool.reverseDns(ip);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Analyse.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
                try {
                    DB.exec("INSERT INTO host ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES (" + id + ", '" + ip + "', '" + hostname + "', '[]', '[]', '" + timestamp + "') ");
                } catch (SQLException ex) {
                    //Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            }

        } else if (Tool.is_hostname(target)) {
            try {
                //TODO support multiple ip DNS.
                String ip = InetAddress.getByName(target).getHostAddress();
                System.out.println("hostname : " + target + " @ " + ip);
                try {
                    DB.exec("INSERT INTO host ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES (" + id + ", '" + ip + "', '" + target + "', '[]', '[]', '" + timestamp + "') ");
                } catch (SQLException ex) {
                    //Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }

            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void parse_traceroute(String tracert, String ip) {
        if (tracert == null) {
            return;
        }

        System.out.println("Parsing traceroute ... : #" + tracert.hashCode());
        int i = 1;
        String previous_ip = null;
        //TODO detect ip of output
        Socket s;
        try {
            s = new Socket("free.fr", 80);
            previous_ip = s.getLocalAddress().getHostAddress();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
        }
        String host_ip = null;
        for (String line : tracert.split("\n")) {
            if ((line.startsWith("  " + i) || line.startsWith(" " + i) || line.startsWith("" + i)) && !line.endsWith("!X")) {
                //System.out.println(line.lastIndexOf("  "));
                //System.out.println(line.split("  "));
                String host;
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    host = (line.split("  ")[line.split("  ").length - 1]).trim();
                } else {
                    host = (line.split("  ")[1]).trim();
                }
                //System.out.println(host);
                if (Tool.is_ip(host)) {
                    System.out.println("ip :" + previous_ip + ">" + host);
                    host_ip = host;
                } else if (Tool.is_hostname(host.split(" ")[0])) {
                    host_ip = host.split(" ")[1].substring(1, host.split(" ")[1].length() - 2);
                    System.out.println("hostname :" + previous_ip + ">" + host);
                }

                if (host_ip != previous_ip) {
                    try {
                        //TODO think of the necessity
                        //DB.exec("INSERT INTO host ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES (" + id + ", '" + ip + "', '" + target + "', '[]', '[]', '" + timestamp + "') ");
                        DB.exec("INSERT INTO route ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES (" + id + ", '" + tracert.hashCode() + "', " + i + ", '" + previous_ip + "', '" + host_ip + "', '" + System.currentTimeMillis() + "') ");
                    } catch (SQLException ex) {
                        Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                previous_ip = host_ip;
                i++;
            }
        }
    }

    private String traceroute(String ip) {
        //TODO
        String route = "";
        try {
            Process traceRt;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                System.out.println("tracert -4 -w 400 " + ((checkdns == "false") ? "-d" : "") + " " + ip);
                //route += "win\n";
                traceRt = Runtime.getRuntime().exec("tracert -w 400 " + ((checkdns == "false") ? "-d" : "") + " " + ip);
            } else {
                System.out.println("traceroute -w 0.4 " + ((checkdns == "false") ? "-n" : "") + " " + ip);
                //route += "unix\n";
                traceRt = Runtime.getRuntime().exec("traceroute -w 0.4 " + ((checkdns == "false") ? "-n" : "") + " " + ip);
            }
            BufferedReader buff = new BufferedReader(new InputStreamReader(traceRt.getInputStream()));

            String line;
            while ((line = buff.readLine()) != null) {
                //System.out.println(line);
                route += line + "\n";
            }
            //route= ((ByteArrayInputStream) traceRt.getInputStream()).toString();
            //System.out.println("route : \n " + route);

            buff = new BufferedReader(new InputStreamReader(traceRt.getErrorStream()));
            String errors = "";
            while ((line = buff.readLine()) != null) {
                //System.err.println(line);
                errors += line + "\n";
            }
            if (errors != "") {
                System.err.println("errors : \n " + errors);
            } else {
                return route;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void recon() {
        try {
            //ResultSet current = DB.query("SELECT * FROM host WHERE id_analyse=" + id + " AND at='" + timestamp + "' ");
            ResultSet current = DB.query("SELECT * FROM host WHERE id_analyse=" + id);

            List<String> ips = new ArrayList<String>();
            while (current.next()) {
                ips.add(current.getString("ip"));
            }
            System.out.println(ips);
            //while (current.next()) {
            for (final String ip : ips) {

                //TODO ??? support recup last port range
                //System.out.println(current.getString("tcp").substring(1, -1));
                //List<Integer> tcp = new ArrayList<Integer>(Arrays.asList(current.getString("tcp").substring(1, -1).split(",")));
                //String ip = current.getString("ip");
                System.out.println(ip);

                Thread trcrt_thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        parse_traceroute(traceroute(ip), ip);
                    }
                });

                trcrt_thread.start();

                if (port != null && port.split("-").length == 2) {
                    List<Integer> tcp = new ArrayList<Integer>();
                    List<Integer> udp = new ArrayList<Integer>();
                    List<Scan> process = new ArrayList<Scan>();
                    for (int i = Integer.parseInt(port.split("-")[0]); i <= Integer.parseInt(port.split("-")[1]); i++) {
                        //System.out.println("Scanning port " + i + " @ " + ip);
                        try {
                            sleep(1000 / Integer.parseInt(limit));
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Scan s = new Scan(ip, i);
                        s.start();
                        process.add(s);

                        if (process.size() > 0) {
                            Scan scan = process.get(0);
                            if (!scan.isAlive()) {
                                if (scan.open) {
                                    switch (scan.type) {
                                        case "tcp":
                                            tcp.add(scan.port);
                                            break;
                                        case "udp":
                                            udp.add(scan.port);
                                            break;
                                    }
                                }
                                process.remove(0);
                                request_done++;
                            } else {
                            }
                        }
                        //request_done++;
                    }
                    while (process.size() > 0) {
                        Scan scan = process.get(0);
                        if (!scan.isAlive()) {
                            if (scan.open) {
                                switch (scan.type) {
                                    case "tcp":
                                        tcp.add(scan.port);
                                        break;
                                    case "udp":
                                        udp.add(scan.port);
                                        break;
                                }
                            }
                            process.remove(0);
                            request_done++;
                        } else {
                        }
                    }

                    process.clear();
                    System.out.println("tcp @ " + ip + " : " + tcp);
                    System.out.println("udp @ " + ip + " : " + udp);
                    //System.out.println( "UPDATE host SET udp='"+udp+"', tcp='"+tcp+"' WHERE id_analyse=" + id + " AND ip='" + ip + "'; ");

                    DB.exec("UPDATE host SET udp='" + udp + "', tcp='" + tcp + "' WHERE id_analyse=" + id + " AND ip='" + ip + "' ");
                }
                while (trcrt_thread.isAlive()) {
                    try {
                        // ON reprend la limite pour reveerfier la fin du traceroute car cela doit bien correspondre aux ressources sur la machine.
                        sleep(1000 / Integer.parseInt(limit));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                request_done += 10;

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            //Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class Scan extends Thread {

        String ip;
        int port;
        boolean open = false;
        String type;

        public Scan(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            if (is_tcp_open(ip, port)) {
                System.out.println("le port tcp " + port + " sur " + ip + " est ouvert !");
                type = "tcp";
                open = true;
            } else if (is_udp_open(ip, port)) {
                System.out.println("le port udp " + port + " sur " + ip + " est ouvert !");
                type = "udp";
                open = true;
            }

            //System.out.println("Port " + port + " @ " + ip + " scanned !");
        }

        private boolean is_udp_open(String host, int port) {
            //TODO optimize
            //TODO extract timeout to  class params
            //https://code.google.com/p/portscanner/source/browse/trunk/PortScanner/src/UDPScanner.java
            boolean flag = false;
            DatagramSocket socket = null;
            byte[] data = host.getBytes();
            try {
                socket = new DatagramSocket();
                socket.setSoTimeout(2500);
                socket.setTrafficClass(0x04 | 0x10);
                socket.connect(new InetSocketAddress(host, port));
                socket.send(new DatagramPacket(data, data.length));
                while (true) {
                    byte[] receive = new byte[4096];
                    DatagramPacket dp = new DatagramPacket(receive, 4096);
                    socket.receive(dp);
                    if (dp != null && dp.getData() != null) {
                        //System.out.println("---------------------------------------------------");
                        //System.out.println(new String(dp.getData()));
                        //byte[] bs = dp.getData();
                    /*
                         for (int i = 0; i < bs.length; i++) {
                         System.out.println(bs[i] + "");
                         }
                         */
                        flag = true;
                        //System.out.println("---------------------------------------------------");
                        break;
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //c'est normal que le test peux echouer
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                }
            }
            return flag;
        }

        private boolean is_tcp_open(String ip, int port) {
            //TODO optimize efficacity
            //TODO extract timeout to  class params
            //http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
            //https://code.google.com/p/portscanner/source/browse/trunk/PortScanner/src/SocketChecker.java
            try {
                //(Socket ignored = new Socket(ip, port))
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2500);
                return true;
            } catch (IOException ignored) {
                return false;
            }
        }
    };

}
