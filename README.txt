HOW TO RUN THIS

0. Install scala, akka, and jruby. The easy way to do this is with homebrew and rvm.

1. You need to get the akka libraries in your classpath. I'm working on a mac with homebrew, so my classpath looks like this:

/usr/local/Cellar/akka/2.0.2/libexec/lib/scala-library.jar:/usr/local/Cellar/akka/2.0.2/libexec/lib/akka/akka-actor-2.0.2.jar:/usr/local/Cellar/akka/2.0.2/libexec/lib/akka/akka-transactor-2.0.2.jar:/usr/local/Cellar/akka/2.0.2/libexec/lib/akka/scala-stm_2.9.1-0.5.jar:$HOME/devel/Eierlegende_Wollmilchsau/target/scala-2.9.2/netfauxgo_2.9.2-0.01.jar

I'd like to have bundle up a jar that includes these dependencies. 

2. The last thing on that classpath is the jar file I have sbt make by running sbt package.

3. Give it lots of heap. If you are running a world with 1M patches and 500k agents, you want something on the order of 4 gigabytes. The more the merrier to avoid GC pauses.

4. Run it! I've been doing it like this:

sbt package && jruby -J-Xmx2048m Driver.rb

5. Non-profit!

LICENSING
This software is Copyright (c) R. Benjamin Shapiro and the University of Wisconsin, Madison. 
It is released under the terms of the GNU General Public License v3.