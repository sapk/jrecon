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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author sapk
 */
public class Route {

    private DB db;
    private String uuid;
    private String from;
    private String to;
    private int hop;
    private int at;
    private int id_analyse;

    public Route(int id_analyse, String uuid, String hop, String from, String to) throws SQLException {
        ResultSet ret = db.query("SELECT * FROM route WHERE id_analyse='" + id_analyse + "' AND from='" + from + "' AND to='" + to + "';");
        if (ret.next()) {
            this.uuid = ret.getString("uuid");
            this.from = ret.getString("from");
            this.to = ret.getString("to");
            this.hop = ret.getInt("hop");
            this.at = ret.getInt("at");
        } else {
            DB.addQueue("INSERT INTO route ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES (" + id_analyse + ", '" + uuid + "', " + hop + ", '" + from + "', '" + to + "', '" + System.currentTimeMillis() + "') ");
        }
    }

    public Route(String uuid, String hop) throws SQLException {
        ResultSet ret = db.query("SELECT * FROM route WHERE uuid='" + uuid + "' AND hop='" + hop + "';");
        if (ret.next()) {
            this.uuid = ret.getString("uuid");
            this.from = ret.getString("from");
            this.to = ret.getString("to");
            this.hop = ret.getInt("hop");
            this.at = ret.getInt("at");
        }
    }

}
