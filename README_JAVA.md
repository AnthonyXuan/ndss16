# PrivateAggregateStatistics

[toc]

This is a Java implementation of the NDSS 2021 paper: "Practical Techniques for Privately Gathering Statistics from Large Data Streams". It provides an efficient, privacy-preserving method for aggregating statistics from large data streams, using cryptographic protocols and succinct data representation structures.

## How2Run

To run this program, simply execute the `main` method in `Main.java`. This prototype presents the time consumption of constructing encrypted Count-Min Sketches and performing the aggregation.

## Structure

The implementation is divided into two main files:

- `CyclicGroup.java`: Implements the cyclic group used in the computations.
- `Main.java`: Contains the implementation of the User and Tally entities, as well as the main method that runs the whole process.

## Details

### CyclicGroup.java

This defines the cyclic group used in the computations. Included are methods for generating random numbers within the group, checking primality, and performing operations such as addition, multiplication, and exponentiation within the group.

### Main.java

This file contains the implementation of the User class, the Tally class, and the main method.

The `User` class implements the methods for constructing the Count-Min sketch and encrypting it. It includes methods for generating random numbers in specific ranges, hashing, and other helper functions.

The `Tally` class implements the aggregation method.

The `main` method initializes the system parameters, creates the users, lets them construct and encrypt their sketches, aggregates the encrypted sketches, and finally measures and outputs the time taken for these operations.

> Note: For simplification, the code assumes perfect security parameters and does not handle possible exceptions or errors that may occur in a real-world implementation. The random number generation and hash functions are also simplified versions.