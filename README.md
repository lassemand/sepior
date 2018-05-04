Sepior demo
==================

This module contains streams for encrypting and decrypting data.
To encrypt data (sepior style) you simply write to an instance of EncryptionOutputStream.

Sepior encryption will basically encrypt three times with three different AES keys. 
I.e. three CipherStreams are created. The first CipherStream will encrypt and write to the 
second CipherStream which will encrypt and write to the third CipherStream which will encrypt
and write to the target stream (e.g. a file)

For large amounts of data we can get a significant performance gain by having these three 
CipherStreams encrypt in parallel. When the first CipherStreams has encrypted and written a 
block to the second, it should start encrypting the next block rather than waiting for the second to finish and so on.

Your job is to write a wrapper for a CipherStream called ConcurrentOutputStream that does this. It is instantiated in 
EncryptingOutputStream lines 98 and 101.

It should only be necessary to make changes to the class ConcurrentOutputStream.

Building the module
-------------------

Use maven to build the module.

    mvn clean package

The tests will fail since you have not yet written a ConcurrentOutputStream! To build without testing write:

    mvn clean package -DskipTests


You can now run a small test program that outputs performance measurements
   
    java -jar target/sepior-demo.jar

On my machine encrypting 1GB takes 13seconds and with my implementation of ConcurrentOutputStream it takes 6 seconds.


