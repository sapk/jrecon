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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        try {
            DB.exec("INSERT INTO analyse ('state', 'name', 'target', 'port' , 'limit', 'checkdns', 'timestamp') VALUES ('Running', '" + name + "', '" + target + "', '" + port + "', '" + limit + "', '" + checkdns + "', '" + timestamp + "') ");
            ResultSet ret = DB.query("SELECT * FROM analyse WHERE name='" + name + "' ");
            id = ret.getInt("id_analyse");
            System.out.println("id analyse : " + id);
            //System.out.println(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Starting analyse ...");
        populate_db();
    }

    private void populate_db() {
        if (Tool.is_network(target)) {
            //TODO populate db with all ip possible.
        } else if (Tool.is_hostname(target)) {
            try {
                //TODO support multiple ip DNS.
                String ip = InetAddress.getByName(target).getHostAddress();
                System.out.println("hostname : " + target + " @ " + ip);
                try {
                    DB.exec("INSERT INTO host ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES (" + id + ", '" + ip + "', '" + target + "', '[]', '[]', '" + timestamp + "') ");
                } catch (SQLException ex) {
                    Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        }

        recon();
    }

    private boolean is_udp_open(String host, int port) {
        //TODO
        //https://code.google.com/p/portscanner/source/browse/trunk/PortScanner/src/UDPScanner.java
        boolean flag = false;
        DatagramSocket socket = null;
        byte[] data = host.getBytes();
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
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
        //http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
        //https://code.google.com/p/portscanner/source/browse/trunk/PortScanner/src/SocketChecker.java
        try {
            //(Socket ignored = new Socket(ip, port))
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 3000);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void traceroute(String ip) {
        //TODO
    }

    private void recon() {
        //TODO multi-thread discovery
        //TODO save to DB
        try {
            //TODO
            ResultSet current = DB.query("SELECT * FROM host WHERE id_analyse=" + id + " AND at='" + timestamp + "' ");
            while (current.next()) {
                List<Integer> tcp = new ArrayList<Integer>();
                List<Integer> udp = new ArrayList<Integer>();
                String ip = current.getString("ip");
                if (port.split("-").length == 2) {
                    for (int i = Integer.parseInt(port.split("-")[0]); i <= Integer.parseInt(port.split("-")[1]); i++) {
                        System.out.println("Scanning port " + i + " @ " + ip);
                        if (is_tcp_open(ip, i)) {
                            System.out.println("le port tcp " + i + " sur " + ip + " est ouvert !");
                            tcp.add(i);
                        }
                        //TODO vérifier si on peut écouter en udp et tcp ???
                        if (is_udp_open(ip, i)) {
                            System.out.println("le port udp " + i + " sur " + ip + " est ouvert !");
                            udp.add(i);
                        }
                        System.out.println("Port " + i + " @ " + ip + " scanned !");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Analyse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
