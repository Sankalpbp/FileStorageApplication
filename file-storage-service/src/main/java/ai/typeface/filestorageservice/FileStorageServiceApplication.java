package ai.typeface.filestorageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FileStorageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileStorageServiceApplication.class, args);
	}

	@Bean
	public org.modelmapper.ModelMapper getModelMapper ( ) {
		return new org.modelmapper.ModelMapper();
	}
}
