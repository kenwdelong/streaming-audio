# Streaming Audio Demo
This project uses the jitsi project's [ICE4J](https://github.com/jitsi/ice4j) and [libjitsi](https://github.com/jitsi/libjitsi) libraries to stream audio between two endpoints, possibly separately by NAT firewalls. These projects don't have a lot of documentation or support, so I thought I'd put this out for folks who are trying to get started.

The ICE code is based on the tutorial [here](http://www.stellarbuild.com/blog/article/ice4j-networking-tutorial-part-1). The libjitsi code is based on the `AvTransmit2` and `AvReceive2` classes in the libjitsi source code base. 

## To run the project
You will need 3 computers: the transmitter endpoint, the receiver endpoint, and the SDP document exchange host. The document exchange host must be visible from both endpoints. All three computers need Java 8, the endpoint computers also need Git.

1. The libjitsi JAR is not in Maven Central, so you'll have to clone that project onto the endpoint computers.
1. On each endpoint, in the libjitsi root folder, run `mvn install`. This will put the libjitsi JAR into the local maven repository. (If you are using an IDE like Eclipse, and you have it set up to put open projects on the classpath of dependent projects, you might be able to skip this step).
1. Clone this project (audio-streaming) on both endpoint machines.
1. On one of those machines, run `mvn package`. This will generate the Spring Boot executable JAR for the document exchange server.
1. Take the JAR (the big one) in the `/target` folder and deploy it on a well-known server somewhere where both endpoint machines can reach it. Run the executable JAR with `java -jar streaming-1.0.0-SNAPSHOT.jar`.  This host is the "SDP Exchange Host".
1. On the transmitting endpoint, run `Driver` with the arguments `-p 5200 -t Tx -h <IP or Hostname of SDP Exchange Host>:8080`.
1. On the receiving endpoint, run `Driver` with the arguments `-p 5200 -t Rx -h <IP or Hostname of SDP Exchange Host>:8080`. 
    * If the transmitting and receiving endpoints are the same machine, choose a different port (`-p` argument) for the second process. The demo will consume 3 ports, one for ICE, one for RTP, and one for RTPC, so the above configuration will use up ports 5200, 5201, and 5202, so be sure to give it enough room between the port numbers.
1. Now the transmitter should be able to talk to the receiver!
1. If it doesn't work, it _might_ help to put the native libraries on the path. Look in the lib/native directory in the libjitsi, find the appropriate folder, and put that path on the `java.library.path` (add it as a -D system property).

## How it works
The endpoints start by running the ICE protocol - basically generating a list of IP address/port combinations by which the other endpoint might be able to reach them. It looks at the local networking environment as well as uses the STUN protocol to find public addresses on the other side of a potential NAT router. They then each create an SDP document with their list of candidates and send it to the other endpoint through the document exchange server. This is a crazy simple Spring Boot web app I've included in this project (two whole classes!). They each run through the other's list of candidates until they agree on a set of addresses.  ICE then terminates and libjitsi begins, opening a media stream using those IP addresses and ports. The project runs for about 60 seconds then stops.

I have not made a big effort to figure out the threading model or proper exception handling. I used RxJava to handle the async callback from the completion of the ICE dance. If you were to adapt this for real use, you'd better think about those things a bit.

The Jitsi projects use a homebrew logger that wraps JUL. The log format is truly horrific. Sorry about that.    