package davidkeller.mail.javafx.client;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.nio.file.Path;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public class MailDocumentWebClient {

	WebClient client = WebClient.create("http://localhost:8080");

	public void consume(
			String mailId, 
			String documentId,
			Path path) {

    	final Flux<DataBuffer> dataBufferFlux = client.get()
                .uri("/api/mail/document/{mailId}/{documentId}", mailId, documentId)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
    	        .retrieve()
    	        .bodyToFlux(DataBuffer.class);

    	DataBufferUtils
    	        .write(dataBufferFlux, path, CREATE_NEW)
    	        .block(); // only block here if the rest of your code is synchronous

    }
}