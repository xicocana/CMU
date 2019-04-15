rm *.json
touch register_clients.json
echo '{"admin":"admin_pwd"}' >> register_clients.json
touch users_albums.json
echo '{"admin":[["default_album","drive_id","txt_id"]]}' >> users_albums.json
mvn clean compile exec:java
