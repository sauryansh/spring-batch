package com.xb.geo.springbatch.config;

import com.github.javafaker.Faker;
import com.xb.geo.springbatch.entity.AddressStageRecord;
import com.xb.geo.springbatch.model.CsvData;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;

@Configuration
@EnableBatchProcessing
public class CSVReaderConfig {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final JdbcTemplate jdbcTemplate;
	
	public CSVReaderConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, JdbcTemplate jdbcTemplate) {
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.jdbcTemplate = jdbcTemplate;
	}
	
	//job
	@Bean
	Job sampleJob() {
		return jobBuilderFactory
			  .get("sampleJob")
			  .start(firstStep())
			  .next(csvReaderWriterStep())
			  .next(csvReaderWriterStepToWriteInDB())
			  .build();
	}
	
	private Step csvReaderWriterStepToWriteInDB() {
		return stepBuilderFactory.get("readDataFromAddressStageRecordStep5-" + System.currentTimeMillis())
			  .<CsvData, AddressStageRecord>chunk(500)
			  .reader(reader1())
			  .processor(processor5()) //transform
			  .writer(customerCompositeItemWriter())
			  .build();
	}
	
	private ItemWriter<? super AddressStageRecord> customerCompositeItemWriter() {
		return items -> {
			for (AddressStageRecord item : items) {
				String insertQuery = "INSERT INTO ADDRESS_REPOSITORY_S (" +
					  "shipment_id, address_line, city, state, pincode, " +
					  "region, country, layer_id, latitude, longitude, " +
					  " geo_accuracy_level, is_inside_geofence, " +
					  "is_manual_scan, is_offline_submit, " +
					  "is_geo_enabled, created_date, purge_date) " +
					  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?)";
				
				jdbcTemplate.update(
					  insertQuery,
					  item.getShipmentId(),
					  item.getAddressLine(),
					  item.getCity(),
					  item.getState(),
					  item.getPincode(),
					  item.getRegion(),
					  item.getCountry(),
					  item.getLayerId(),
					  item.getLatitude(),
					  item.getLongitude(),
					  item.getGeoAccuracyLevel(),
					  item.getIsInsideGeofence(),
					  item.getIsManualScan(),
					  item.getIsOfflineSubmit(),
					  item.getIsGeoEnabled(),
					  item.getCreatedDate(),
					  item.getPurgeDate()
				);
				
				System.out.println("Inserted item: " + item);
			}
		};
	}

	
	private ItemProcessor<? super CsvData,? extends AddressStageRecord> processor5() {
		Faker faker = new Faker();
		return item -> {
			AddressStageRecord addressStageRecord = AddressStageRecord.builder()
				  .shipmentId(item.getMessageId())
				  .addressLine(item.getAddressLine())
				  .pincode(item.getPincode())
				  .city(item.getCity())
				  .state(item.getState())
				  .country("India")
				  .region(item.getState().equalsIgnoreCase("Telangana") ? "SOUTH" : "WEST")
				  .layerId(16)
				  .latitude(String.valueOf(Double.parseDouble(item.getLatitude())))
				  .longitude(String.valueOf(Double.parseDouble(item.getLongitude())))
				  .geocode(new GeometryFactory().createPoint(new Coordinate(Double.parseDouble(item.getLatitude()), Double.parseDouble(item.getLongitude()))))
				  .geoAccuracyLevel(faker.number().randomDouble(4, 0, 100))
				  .isInsideGeofence(faker.bool().bool())
				  .isManualScan(faker.bool().bool())
				  .isOfflineSubmit(faker.bool().bool())
				  .isGeoEnabled(faker.bool().bool())
				  .createdDate(new Timestamp(System.currentTimeMillis()))
				  .purgeDate(new Timestamp(System.currentTimeMillis()))
				  .build();
			
			return addressStageRecord;
		};
	}
	
	
	//chunk oriented
	// reader
	// processor (optional)
	// writer
	@Bean
	Step csvReaderWriterStep() {
		return stepBuilderFactory
			  .get("readDataFromCSV-" + System.currentTimeMillis()) //naming the step
			  .<CsvData, CsvData>chunk(100)
			  .reader(reader1())
			  .writer(writer())
			  .build();
	}
	
	private ItemWriter<? super CsvData> writer() {
		return items -> {
			for (CsvData csvData : items) {
				System.out.println("Writing customer: " + csvData);
			}
		};
	}
	
	private FlatFileItemReader<CsvData> reader1() {
		FlatFileItemReader<CsvData> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource("src/main/resources/p_h_raw_data.csv"));
		itemReader.setName("csvReader");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}
	
	private LineMapper<CsvData> lineMapper() {
		DefaultLineMapper<CsvData> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("message_id", "address_line", "pincode", "city", "state", "latitude", "longitude");
		BeanWrapperFieldSetMapper<CsvData> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(CsvData.class);
		
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
		
	}
	
	
	//step
	//tasklet step
	@Bean
	Step firstStep() {
		return stepBuilderFactory
			  .get("sampleStep1")
			  .tasklet(getFirstTask())
			  .allowStartIfComplete(false)
			  .build();
	}
	
	//Tasklet
	@Bean
	Tasklet getFirstTask() {
		return (contribution, chunkContext) -> {
			System.out.println("Sample Step 1 from Spring Batch");
			return RepeatStatus.FINISHED;
		};
	}
	
}
