/* export : 10 */


INSERT INTO analyse  ('id_analyse', 'state', 'name', 'target', 'port', 'limit', 'checkdns', 'timestamp', 'ended_at') VALUES ('$$ID_ANALYSE$$', 'Imported', ' # Mon Oct 21 17:56:00 CEST 2013', 'sapk.fr', 'null', '10', 'false', '1382370960744, '1382370962129'') 
INSERT INTO host  ('id_analyse', 'ip', 'hostname', 'tcp', 'udp', 'at') VALUES ('$$ID_ANALYSE$$', '37.187.4.165', 'sapk.fr', '[]', '[]', '1382370960744') 


INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ('$$ID_ANALYSE$$', '2147332281', '1', '148.60.83.161', '148.60.87.252', '1382370961782') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ('$$ID_ANALYSE$$', '2147332281', '2', '148.60.87.252', '148.60.248.254', '1382370961879') 
INSERT INTO route  ('id_analyse', 'uuid', 'hop', 'from', 'to', 'at') VALUES ('$$ID_ANALYSE$$', '2147332281', '12', '148.60.248.254', '37.187.4.165', '1382370961962') 
