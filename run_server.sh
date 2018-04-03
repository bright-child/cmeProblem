if [ "$#" = "2" ]; then
   java -cp target/cmeProblem-1.0-SNAPSHOT.jar org.jynergy.cme.server.ChatServer $1 $2
else
   java -cp target/cmeProblem-1.0-SNAPSHOT.jar org.jynergy.cme.server.ChatServer $1
fi                      