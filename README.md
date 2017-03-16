grails integration tests appear to be leaking "servlet contexts" (which appear to contain a copy of the entire spring application context) when the `@DirtiesContext` annotation is used.

To reproduce, 

```
git clone https://github.com/jonroler/grails-dirties-context-mem-leak.git
cd grails-dirties-context-mem-leak
./grailsw test-app -integration
```

Then, open a browser to build/reports/tests/index.html and look at the output for `memleak.Leak1Spec` and `memleak.Leak2Spec`. You should see something like:

```
Grails application running at http://localhost:44749 in environment: test
2017-03-16 12:42:18.901 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 2
2017-03-16 12:42:18.916 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 2
Grails application running at http://localhost:42167 in environment: test
2017-03-16 12:42:21.599 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 3
```

and

```
Grails application running at http://localhost:39589 in environment: test
2017-03-16 12:42:24.247 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 4
Grails application running at http://localhost:40697 in environment: test
2017-03-16 12:42:26.937 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 5
Grails application running at http://localhost:45679 in environment: test
2017-03-16 12:42:29.575 ERROR --- [    Test worker] memleak.Leak1Spec                        : Number of contexts: 6
```

What this is output is showing is that the map `grails.util.Holders.servletContexts.instances` is growing when the `@DirtiesContext` annotation is used on a test method, and it is not growing when this annotation is not used. If you look at the two test classes, you will see that all that it is doing is logging the size of this servlet context map (with some of the methods annotated with `@DirtiesContext` and others not annotated). 

This is a very serious memory leak for projects that have a large Spring application context since it appears that a copy of the spring application context is reachable from this servlet context map. For example, in our application, one entry in this map appears to retain about 15 MB of memory, and since we are using the `@DirtiesContext` in some of our tests, after running through a bunch of our tests, we have over 2 GB of memory held in this map due to this leak. As a result, our tests are failing with an OutOfMemory error.  Note that it also appears that a copy of the Hibernate session factory object is leaked whenever the `@DirtiesContext` annotation is used as well.

It appears that grails is attempting to clear the map. However, it is failing to do so since the thread context classloader is different between when the entry is added to the map and when it is removed. In other words, the result of `grails.util.Holder.getClassLoaderId()` is different between when the entry is added to the map and when the remove from the map is attempted (the result of `grails.util.Holder.getClassLoaderId()` is used as the key to store the servlet context object in the map).
