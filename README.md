# kvstore

This projects is an In-Memory key-value Store, so that I can learn Distributed Systems.
 
The objective is to learn things like:

1. Should I implement a Write-ahead log so that the DB can recover from a crash?
2. Should I implement a Log-structured to store the values so that the database is not limited by the RAM (well, keys have to fit in memory)?   
3. How can the database scale read? and write? Do I need sharding? Replicas? Should it be a Leader based replication?.....
4. How can the data be replicated in different machines?
5. How does it know when the Leader has a failure?
6. ….

## Implemented
1. Implementation à la Bitcask. Data is persisted to disk. Every key and value are stored in an append-only file, so that operations are much much faster and simple.
The in-memory hashmap works as an Index for byte offsets. Every key in the hashmap points to an offset in the file.
2. Because data is persisted the DB is recreated at the start time.
3. Protocol. So far it supports GET, SET, FSET and CLOSE.
4. Isolation. 
5. Asynchronous replication.

## Replication
The database supports asynchronous replication. Replication works in a similar way to ACKs in TCP.  Each replica sends its current offset to master, that is, the offset of the last key it has stored. Then, the master sends all the keys from that point to the replica.

This the communication flow:

1. Replica: OFFSET 1234
2. Master: RSET key value\r\n
3. Master: RSET key2 value2\r\n
4. Master: ENDR\r\n
5. Replica: wait a few milliseconds.
6. Go to 1.

To enable replication, you only need to set these settings in config.edn in the replicas:

* :master false
* :replication-port 10010
* :master-host "localhost"
* :replication-delay 300

## Installation

Just clone this repo.

## Usage


    $ lein with-profile prod run

    Tests:

    $ lein with-profile dev test

The database has really simple text protocol. These are the  supported commands:

* SET key value
* FSET key value ;;No ack is sent.
* GET key
* CLOSE

List of internal commands used by the replication:

* OFFSET N
* RSET key value
* ENDR

## Examples

To try the database you just need  to open socket:

```
 $ telnet localhost 10009
Trying ::1...
Connected to localhost.
Escape character is '^]'.
SET mykey 123
OK
GET mykey
123
```


You can see an example in client.clj.


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

