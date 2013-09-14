import javax.sql.DataSource

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.SimpleJobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder

@Configuration
@EnableBatchProcessing
class BatchDriver {

	@Autowired
	JobBuilderFactory jobBuilderFactory

	@Autowired
	StepBuilderFactory stepBuilder

	@Autowired
	JobLauncher launcher

	@Bean
	DataSource dataSource() {
		return new EmbeddedDatabaseBuilder()
		.addScript("classpath:org/springframework/batch/core/schema-drop-hsqldb.sql")
		.addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")
		.build();
	}

	def start(scriptPath) {
		ApplicationContext context = new AnnotationConfigApplicationContext(BatchDriver.class)

		context.getAutowireCapableBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false)

		Map<Object> steps = new LinkedHashMap();

		println '>>> Running driver'
		def binding = new Binding([step: { namedParams, stepId ->
				if(namedParams['tasklet'] != null) {
					steps.put(stepId, stepBuilder.get(stepId)
							.tasklet(namedParams['tasklet'] as Tasklet)
							.build())
				} else {
					steps.put(stepId, stepBuilder.get(stepId)
							.chunk(namedParams['chunk'])
							.reader(namedParams['reader'] as ItemReader)
							.processor(namedParams['processor'] as ItemProcessor)
							.writer(namedParams['writer'] as ItemWriter)
							.build())
				}
			}])

		println "Script to be run ${scriptPath}"

		def shell = new GroovyShell()
		Script script = shell.parse(new File(scriptPath))
		println 'parsed successfully'
		script.binding = binding
		println 'bound successfully'

		script.run()

		println "We have ${steps.size()} steps"

		Iterator stepCollection = steps.iterator()

		SimpleJobBuilder jobBuilder = jobBuilderFactory.get('job').start(stepCollection.next().value)

		while(stepCollection.hasNext()) {
			jobBuilder = jobBuilder.next(stepCollection.next().value)
		}

		Job job = jobBuilder.build()

		launcher.run(job, new JobParameters())

		println '>>> Driver complete'
	}

	public static void main(String [] args) {
		BatchDriver command = new BatchDriver()
		command.start(args[0])
	}
}
