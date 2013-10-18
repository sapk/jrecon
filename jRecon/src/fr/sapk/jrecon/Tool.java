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

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    public static String reverseDns(String hostIp) throws UnknownHostException{
        //TODO optimize for ip without reverseDNS.
        InetAddress addr = InetAddress.getByName(hostIp);
        return addr.getHostName();
    }
}
