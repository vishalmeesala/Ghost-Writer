package com.ghostwriter.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

@Service
public class OpenAiServicesAccess {
	final private static String OpenAiApiKey = "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";//System.getenv("OpenAiApiKey");

	public ArrayList<CompletionChoice> getGhostText(String ghostPrompt){
		System.out.println("Welcome to GhostWriter\n\n\n");

		OpenAiService service = new OpenAiService(OpenAiApiKey,50000);

        Engine davinci = service.getEngine("davinci");

    	ArrayList<CompletionChoice> storyArray = new ArrayList<>();

    	CompletionRequest completionRequest = CompletionRequest.builder()
								                .prompt(ghostPrompt)
								                .temperature(0.9)
								                .maxTokens(1500)
								                .topP(1.0)
								                .frequencyPenalty(0.0)
								                .presencePenalty(0.3)
								                .echo(true)
								                .build();
    	service.createCompletion("davinci", completionRequest).getChoices().forEach(line -> {storyArray.add(line);});
		return storyArray;
	}
	
	public String imageCreationApi(String str) throws IOException {
		
		URL url = new URL("https://api.openai.com/v1/images/generations");
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestMethod("POST");
	
		httpConn.setRequestProperty("Content-Type", "application/json");
		httpConn.setRequestProperty("Authorization","Bearer "+OpenAiApiKey);
	
		httpConn.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
		writer.write("{\n  \"prompt\": \""+str+"\",\n  \"n\": 2,\n  \"size\": \"1024x1024\"\n}");
		writer.flush();
		writer.close();
		httpConn.getOutputStream().close();
	
		InputStream responseStream = httpConn.getResponseCode() / 100 == 2
				? httpConn.getInputStream()
				: httpConn.getErrorStream();
		Scanner s = new Scanner(responseStream).useDelimiter("\\A");
		String response = s.hasNext() ? s.next() : "";
		return response;
	}
}
