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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 *
 * @author Antoine
 */
public class Tool {

    public static boolean is_ip(String s) {
        //TODO support ipv6
        return s.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    }

    public static boolean is_network(String s) {
        //TODO support ipv6
        if (s.contains("/") && s.split("/").length == 2) {
            return (is_ip(s.split("/")[0]) && s.split("/")[1].matches("^[0-3]?[0-9]$"));
        } else {
            return false;
        }
    }

    public static boolean is_hostname(String s) {
        return s.matches("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    }

    //TODO implement convertStreamToString
    public static byte[] toByteArray(double d) {
        long l = Double.doubleToRawLongBits(d);
        return new byte[]{
            (byte) ((l >> 56) & 0xff),
            (byte) ((l >> 48) & 0xff),
            (byte) ((l >> 40) & 0xff),
            (byte) ((l >> 32) & 0xff),
            (byte) ((l >> 24) & 0xff),
            (byte) ((l >> 16) & 0xff),
            (byte) ((l >> 8) & 0xff),
            (byte) ((l >> 0) & 0xff),};
    }

    public static long IPtoLong(String ip) {
        //TODO suport IPv6
        ip = ip.trim();
        System.out.println(ip);
        long n = (long) Integer.parseInt(ip.split("\\.")[0]) * 256 * 256 * 256;
        n += (long) Integer.parseInt(ip.split("\\.")[1]) * 256 * 256;
        n += (long) Integer.parseInt(ip.split("\\.")[2]) * 256;
        n += (long) Integer.parseInt(ip.split("\\.")[3]);
        return n;
    }

    public static String LongtoIPv4(long n) {
        long d = (n % 256);
        long c = (n / 256) % 256;
        long b = (n / (256 * 256)) % 256;
        long a = (n / (256 * 256 * 256)) % 256;

        return a + "." + b + "." + c + "." + d;
    }

    public static String reverseDns(String hostIp) throws UnknownHostException {
        //TODO optimize for ip without reverseDNS.
        InetAddress addr = InetAddress.getByName(hostIp);
        return addr.getHostName();
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon loadImageIcon(String path) {
        URL imgURL = UIFrameHome.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void writetoFile(String filename, String text) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        FileWriter writer = new FileWriter(file);
        writer.write(text);
        writer.flush();
    }

    public static String buildXML(ArrayList<String[]> nodes, ArrayList<String[]> routes) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!--  Data for visualitation of a network  -->\n"
                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n"
                + "<graph edgedefault=\"undirected\">\n"
                + " \n"
                + "<!-- data schema -->\n"
                + "<key id=\"ip\" for=\"node\" attr.name=\"ip\" attr.type=\"string\"/>\n"
                + "<key id=\"hostname\" for=\"node\" attr.name=\"hostname\" attr.type=\"string\"/>\n"
                //                + "<key id=\"tcp\" for=\"node\" attr.name=\"at\" attr.type=\"string\"/>\n"
                //                + "<key id=\"udp\" for=\"node\" attr.name=\"at\" attr.type=\"string\"/>\n"
                //                + "<key id=\"at\" for=\"node\" attr.name=\"at\" attr.type=\"string\"/>\n"
                //                    + "<key id=\"at\" for=\"node\" attr.name=\"at\" attr.type=\"DateTime\"/>\n"
                + ""
                + "<key id=\"size\" for=\"edge\" attr.name=\"size\" attr.type=\"Double\"/>\n"
                + "\n\n";

        for (int i = 0; i < nodes.size(); i++) {
            String[] tmp = nodes.get(i);
            xml += "<node id=\"" + i + "\">\n"
                    + " <data key=\"ip\">" + tmp[1] + "</data>\n"
                    + " <data key=\"hostname\">" + tmp[2] + "</data>\n"
                    //                    + " <data key=\"tcp\">" + tmp[3] + "</data>\n"
                    //                    + " <data key=\"udp\">" + tmp[4] + "</data>\n"
                    //                    + " <data key=\"at\">" + tmp[5] + "</data>\n"
                    + "</node>\n";
        }
        for (int i = 0; i < routes.size(); i++) {
            String[] tmp = routes.get(i);
            xml += "\n<edge source=\"" + tmp[1] + "\" target=\"" + tmp[2] + "\">\n"
                    + " <data key=\"size\">" + tmp[3] + "</data>\n"
                    + "</edge>\n";
        }

        xml += "\n\n"
                + "</graph>\n"
                + "</graphml>";
        return xml;
    }

    public static ArrayList[] getData(String id_analyse) throws SQLException {
        ArrayList[] data = new ArrayList[2];
        data[0] = new ArrayList<String[]>();
        data[1] = new ArrayList<String[]>();

        ResultSet host = DB.query("SELECT * FROM host WHERE id_analyse='" + id_analyse + "'");
        ArrayList<String> nodes = new ArrayList<>();

        while (host.next()) {
            if (!nodes.contains(host.getString("ip"))) {
                nodes.add(host.getString("ip"));
                String[] tmp = new String[]{"" + nodes.size(), host.getString("ip"), host.getString("hostname"), host.getString("tcp"), host.getString("udp"), host.getString("at")};
                data[0].add(nodes.size() - 1, tmp);
            }
        }

        ResultSet route = DB.query("SELECT * FROM route WHERE id_analyse='" + id_analyse + "'");
        ArrayList<String> trajets = new ArrayList<>();

        while (route.next()) {
            int source = 0, target = 0;

            if (!nodes.contains(route.getString("from"))) {
                nodes.add(host.getString("from"));
                String[] tmp = new String[]{"" + nodes.size(), host.getString("from"), host.getString("from"), "", "", ""};
                data[0].add(nodes.size() - 1, tmp);
                source = nodes.size() - 1;
            } else {
                source = nodes.indexOf(route.getString("from"));
            }

            if (!nodes.contains(route.getString("to"))) {
                nodes.add(host.getString("to"));
                String[] tmp = new String[]{"" + nodes.size(), host.getString("to"), host.getString("to"), "", "", ""};
                data[0].add(nodes.size() - 1, tmp);
                target = nodes.size() - 1;
            } else {
                target = nodes.indexOf(route.getString("to"));
            }

            if (!trajets.contains(source + "->" + target)) {
                trajets.add(source + "->" + target);
                String[] tmp = new String[]{"" + trajets.size(), "" + source, "" + target, "" + 1};
                data[1].add(trajets.size() - 1, tmp);
            } else {
                int id = trajets.indexOf(source + "->" + target);
                String[] tmp = (String[]) data[1].get(id);
                tmp[3] = "" + (Integer.parseInt(tmp[3]) + 1);
                data[1].set(id, tmp);
            }

        }

        //data[0].add(host)
        return data;
    }
}
