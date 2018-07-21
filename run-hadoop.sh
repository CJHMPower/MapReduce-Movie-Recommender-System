#!/bin/bash
hdfs dfs -mkdir /input /test
hdfs dfs -put input/* /input
hdfs dfs -put test/* /test
hdfs dfs -rm -r /dataDividedByUser /coOccurrenceMatrix /Normalize /Normalize2 /Multiplication /Sum /RMSE /Result
cd src/main/java
hadoop com.sun.tools.javac.Main *.java
jar cf recommender.jar *.class
hadoop jar recommender.jar Driver /input /dataDividedByUser /coOccurrenceMatrix /Normalize /Normalize2 /Multiplication /Sum /RMSE /Result /test
hdfs dfs -cat /Result/*
