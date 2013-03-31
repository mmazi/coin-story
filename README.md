coin-story
==========

Bitcoin market order book history.

A very simple application that logs order book snapshots from popular exchanges every 15 minutes into a relational database.

Uses https://github.com/timmolter/XChange/ to connect to the exchnages in an uniform manner.

Must run in a Java EE 6 application server; tested on JBoss (might need minor changes to run on JPA providers other than
Hibernate, eg. changing Ord.time to java.sql.Timestamp).