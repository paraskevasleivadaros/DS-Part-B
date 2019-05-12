#!/bin/bash
cd /home/paraskevas/IdeaProjects/DS-Part-A/src;
javac broker.java
javac publisher.java
javac consumer.java
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java broker 3421"
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java broker 3822"
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java broker 3719"
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java publisher 1"
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java publisher 2"
gnome-terminal -- /home/paraskevas/IdeaProjects/DS-Part-A/src "java consumer 021"