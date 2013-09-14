spring-batch-groovy
===================

A POC for developing a groovy based DSL for Spring Batch.  The syntax for this DSL is based on the syntax used in the [Jenkin's Build Flow plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Flow+Plugin).  Currently you can configure both tasklet steps and chunk based steps.  Only basic chunk elements are supported (commit-interval, reader, processor, writer).  Also, currently steps are executed only in the order they are configured.

### A tasklet step

```
step('myTaskletStep', 
 	 tasklet: {stepContribution, chunkContext -> println 'This tasklet was executed})
```

### A chunk step
*Note* This is a never ending job.

```
step('myChunkStep',
     chunk: 5,
     reader: {return 'item'},
     processor: {item -> return item.toUpperCase()},
     writer: {listOfItems -> listOfItems.each{ println it }})
````

### Multiple steps
These steps will be executed within the order they are configured 

```
step('myFirstTaskletStep', 
	 tasklet: {stepContribution, chunkContext -> println 'This tasklet was executed})
step('mySecondTaskletStep', 
	 tasklet: {stepContribution, chunkContext -> println 'This tasklet was executed})
```