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

/**
 *
 * @author sapk
 */
public class Host {
    
    private static DB db;
    private String ip;
    private String hostname;
    private String tcp;
    private String udp;
    private int at;
    
    public Host(int id_analyse, String ip) {
    }
    public Host(int id_analyse, String ip, String hostname) {
    }
}
