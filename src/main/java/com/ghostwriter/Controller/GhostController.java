package com.ghostwriter.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ghostwriter.Services.ArticleGrammarCheck;
import com.ghostwriter.Services.OpenAiServicesAccess;
import com.theokanning.openai.completion.CompletionChoice;

@RestController(value="/openAi")
public class GhostController {
	@Autowired
	ArticleGrammarCheck grammarCheck;
	@Autowired
	OpenAiServicesAccess OpenAiServiceCall;

	final private static String OpenAiApiKey = "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";//System.getenv("OpenAiApiKey");
	//need to use this System.getenv("OpenAiApiKey");
	/**
	 * ghostWriterRequestHandler REST API handles input stream from the Client and
	 * Creates minimum 700 words Articles from the input String array
	 * 
	 * 
	 * @param ghostPromptinputStrings
	 * @return
	 * @throws IOException
	 */
	@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping(value = "/getGhostArticles",headers = {"content-type=application/json" }, consumes = "application/json", produces = "application/json")
	@ResponseBody public List<ArrayList<CompletionChoice>> ghostWriterRequestHandler(@RequestBody String[] ghostPromptinputStrings) throws IOException {
		
		System.out.println("Hello Y'all!!! Welcome to Ghost Writer-AI with GPT-3 support, powered by OpenAi \n\n\n");
		
		List<ArrayList<CompletionChoice>> outputStringArray= new ArrayList();
        
		for(int i = 0; i<ghostPromptinputStrings.length; i++){
        
			ArrayList<CompletionChoice> storyArray = new ArrayList<>();
        	
			storyArray = OpenAiServiceCall.getGhostText(ghostPromptinputStrings[i]);
        	
			String s = storyArray.get(0).getText();
        	
	
			int len = (s.split(" ")).length;
			startOverAgain:
			if(  len < 700  ) {
				while( len < 700 )
				{
					storyArray = OpenAiServiceCall.getGhostText(ghostPromptinputStrings[i]);
					s = storyArray.get(0).getText();
					
					s = grammarCheck.duplicateLinesEliminater(s);
					s = grammarCheck.spellChecker(s);
					
					len = (s.split(" ")).length;
					
				}
				
			}
			else {
				
				s = grammarCheck.duplicateLinesEliminater(s);
				s = grammarCheck.spellChecker(s);
				len = (s.split(" ")).length;
				if(len < 700) {
					break startOverAgain;
				}
			}

			storyArray.get(0).setText(s);
        	
        	System.out.println("Word Count for ghost Prompt \" " +ghostPromptinputStrings[i]+ "\" : - "+ len +"\n\n");
        	
        	outputStringArray.add(storyArray);
        }

		return outputStringArray;

	}
	@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping(value = "/SpellCheck",headers = {"content-type=application/json" }, consumes = "application/json", produces = "application/json")
	@ResponseBody public String spellChecker(@RequestBody String str) throws IOException {
		
		String[] input = str.split(" ");
		JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
	    for (Rule rule : langTool.getAllRules()) {
	      if (!rule.isDictionaryBasedSpellingRule()) {
	        langTool.disableRule(rule.getId());
	      }
	    }
		
		for (int i =0; i<input.length;i++) {
			
		    List<RuleMatch> matches = langTool.check(input[i]);
		    for (RuleMatch match : matches) {
		          List<String> suggestions = match.getSuggestedReplacements();
		          if(!suggestions.isEmpty()) {
		        	  input[i]= suggestions.get(0);
		          }
		    }
		}
		
		str = "";
		for(String s :input) {
			str = str+s+" ";
		}
		return "the sentence :\n"+str;
	}
	@CrossOrigin(origins = "http://localhost:8080")
	@PostMapping(value = "/imageAi",headers = {"content-type=application/json" }, consumes = "application/json", produces = "application/json")
	@ResponseBody public String imageCreation(@RequestBody String[] str) throws IOException {
		
		URL url = new URL("https://api.openai.com/v1/images/generations");
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setRequestMethod("POST");
	
		httpConn.setRequestProperty("Content-Type", "application/json");
		httpConn.setRequestProperty("Authorization","Bearer "+OpenAiApiKey);
	
		httpConn.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
		writer.write("{\n  \"prompt\": \""+str[0]+"\",\n  \"n\": 2,\n  \"size\": \"1024x1024\"\n}");
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
