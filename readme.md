# Computer Networks Assignment 2
## Chat Application
#### by- Rachit Kumar(2017CS10364)
####        Rahul Choudhary(2017CS10365)

### Execution:
#### Server:
javac Server.java
java Server n
(where n=0 for part1, 1 for part2 and 2 for part3)
#### Client:
javac Client.java   return
java Client localhost username1
Now, type "@username2 message" to send message from username1 to username2

#### Ctrl-C Scenario:
We have implemented this. The client is deregistered from the server list and this information is printed.

#### Offline Users:
We can add a buffer with each registered client in the server where the incoming messages can be stored and flushed when the client comes online if using for small scale. If on a large scale, then the server will store it on cloud and send it when the user comes online.

#### Why use base64 and not send in binary format:
This is not desirable because in binary format, ii will be very difficult to see if the data we're getting at the server is correct or not. In base64, one digit represents exactly 6bits of data(i.e. 3 bytes(ASCII) are represented by 4 Base64 digits) using the letters 0-9, a-z, A-Z, +, / and =(padding). Hence base64 is much more human readable and hence we can test at the server if the encrypted data is correct-ish or not.
