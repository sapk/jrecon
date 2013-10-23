/* export : 1 */


INSERT INTO analyse  ('id_analyse', 'state', 'name', 'target', 'port', 'limit', 'checkdns', 'timestamp', 'ended_at') VALUES ($$ID_ANALYSE$$, 'Imported', ' # Wed Oct 23 17:26:21 CEST 2013', 'sapk.fr', 'null', '10', 'false', '1382541981317', '1382541982463') 
INSERT INTO host  ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES ($$ID_ANALYSE$$, '37.187.4.165', 'sapk.fr', '[]', '[]', '1382541981317') 


INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ($$ID_ANALYSE$$, '-483332113', '1', '148.60.83.161', 'null', '1382541982332') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ($$ID_ANALYSE$$, '-483332113', '2', 'null', '148.60.248.254', '1382541982333') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ($$ID_ANALYSE$$, '-483332113', '3', '148.60.248.254', '129.20.252.251', '1382541982334') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ($$ID_ANALYSE$$, '-483332113', '12', '129.20.252.251', '178.33.103.228', '1382541982343') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ($$ID_ANALYSE$$, '-483332113', '13', '178.33.103.228', '37.187.4.165', '1382541982344') 
