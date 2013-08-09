Search Engine - Washington State University, Vancouver 
=============
Created by Bo Wang, Hoang Le, and Huy Tran 

Usage
=============

+ download, and put list1, list2, list3, pagerank.dat, doclength.txt, doctile.txt, pid_map.dat, stoplist.txt files from S3 into data folder

+ open 3 terminal , and start 3 slaves

cd SlaveNode/build
./make (just need to do in one terminal, if not work : chmod 755 make)
java -client -Xmx1000m SlaveServerThread 60000 ../../data/list1 (in terminal 1)
java -client -Xmx1000m SlaveServerThread 60002 ../../data/list2 (in terminal 2)
java -client -Xmx1000m SlaveServerThread 60004 ../../data/list3 (in terminal 3)

+ open another terminal, and start master node

cd MasterNode/build

./make (if not work : chmod 755 make)
java -cp .:gson.jar -client -Xmx1000m master/ServerThread localhost,60000 localhost,60002 localhost,60004 ../../data/pagerank.dat ../../data/doclength.txt ../../data/doctitle.txt ../../data/pid_map.dat ../../data/stoplist.txt

+ open browser (install apache if not)

open : localhost/search.html 
