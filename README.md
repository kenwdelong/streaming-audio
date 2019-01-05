# Streaming Audio Demo
This project uses the jitsi project's ICE4J and libjitsi libraries to stream audio between two endpoints, possibly separately by NAT firewalls.

## To run the project
You will need 3 computers: the transmitter endpoint, the receiver endpoint, and the SDP document exchange host. The document exchange host must be visible from both endpoints. All three computers need Java 8, the endpoint computers also need Git.

1. The libjitsi JAR is not in Maven Central, so you'll have to clone that project onto the endpoint computers.
1. On each endpoint, in the libjitsi root folder, run `mvn install`. This will put the libjitsi JAR into the local maven repository.
1. Clone this project on both endpoint machines.
1. On one of those machines, run `mvn package`. This will generate the JAR for the document exchange server.
1. Take the JAR (the big one) in the `/target` folder and deploy it on a well-known server somewhere where both endpoint machines can reach it. Run the executable JAR with `java -jar streaming-1.0.0-SNAPSHOT.jar`.  This host is the "SDP Exchange Host".
1. On the transmitting endpoint, run `Driver` with the arguments `-p 5200 -t Tx -h 54.144.235.144:8080`.
1. On the receiving endpoint, run `Driver` with the arguments `-p 5200 -t Rx -h 54.144.235.144:8080`. 
    * If the transmitting and receiving endpoints are the same machine, choose a different port (`-p` argument) for the second process. The demo will consume 3 ports, one for ICE, one for RTP, and one for RTPC, so the above configuration will use up ports 5200, 5201, and 5202, so be sure to give it enough room between the port numbers.
1. Now the transmitter should be able to talk to the receiver!    