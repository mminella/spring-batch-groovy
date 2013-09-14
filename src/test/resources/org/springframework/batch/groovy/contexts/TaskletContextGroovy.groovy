step('myStep',
	tasklet: { stepExecution, chunkContext ->
		println "Tasklet 1 was executed! $stepExecution, $chunkContext"
})

step('mySecondStep',
	chunk: 3,
	reader: {
		println 'Read was called';
		return 'Item';},
	processor: {item -> println "Processor was called with $item"; return item.toUpperCase(); },
	writer: {list ->
		println "Writer was called with ${list.size()} items"
		list.each {
			println it
		}
	}
)
