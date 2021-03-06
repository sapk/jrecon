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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Antoine
 */
public class DB {

    // synchronized ????
    void reset() {
        close();
        File f = new File(DBPath);
        f.delete();
        connect();
        check();
    }

    //private static String DBPath = JRecon.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"/../data/db.sqlite";
    private static String DBPath = "data/db.sqlite";
    //private static String DBPath = ":memory:";

    private static Connection connection = null;
    private static Statement statement = null;

    public DB() {
        DBPath = Tool.get_root_folder()+DBPath;
        File p = new File(DBPath).getParentFile();
        if (!p.isDirectory()) {
            p.mkdir();
        }
        if (connection == null || statement == null) {
            connect();
            check();
        }
        //close();
    }

    int count(String sql) throws SQLException {
        //TODO 
        return 0;
    }

    static synchronized ResultSet query(String sql) throws SQLException {
        if (connection == null || statement == null || connection.isClosed()) {
            connect();
        }
        return statement.executeQuery(sql);
    }

    static synchronized void exec(String sql) throws SQLException {
        if (connection == null || statement == null) {
            connect();
        }
        statement.executeUpdate(sql);
        connection.commit();
    }

    static synchronized void addQueue(String sql) throws SQLException {
        //System.out.println("SQL :: "+sql);
        statement.addBatch(sql);
    }

    static synchronized void execQueue() throws SQLException {
        System.out.println(System.currentTimeMillis() + " SQL Batch executing ...");
        statement.executeBatch();
        System.out.println(System.currentTimeMillis() + " SQL Batch executed ...");
        connection.commit();
        System.out.println(System.currentTimeMillis() + " SQL Batch commited ...");
    }

    private static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            System.out.println("Connexion a " + DBPath + " avec succès");
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            System.out.println("Erreur de connexion");
        }
    }

    private void close() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void check() {
        System.out.println("Checking DB ...");

        try {
            statement.executeUpdate("CREATE TABLE analyse ('id_analyse' INTEGER PRIMARY KEY AUTOINCREMENT, 'state' TEXT, 'name' TEXT, 'target' TEXT, 'port' TEXT, 'limit' TEXT, 'checkdns' INTEGER, 'timestamp' INTEGER, 'ended_at' INTEGER)");
            statement.executeUpdate("CREATE TABLE host ('id_analyse' INTEGER, 'ip' TEXT, 'hostname' TEXT, 'tcp' TEXT, 'udp' TEXT, 'at' INTEGER, UNIQUE(id_analyse, ip))");
            statement.executeUpdate("CREATE TABLE route ('id_analyse' INTEGER, 'uuid' TEXT, 'hop' INTEGER, 'from' TEXT, 'to' TEXT, 'at' INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
;
}
