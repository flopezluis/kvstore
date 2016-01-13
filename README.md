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
3. Protocol. So far it supports GET, SET and CLOSE.

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
