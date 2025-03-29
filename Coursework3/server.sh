cd server
rmiregistry &
sleep 2
java Replica 1 &
java Replica 2 &
java Replica 3 &
java FrontEnd
