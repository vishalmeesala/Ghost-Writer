package com.ghostwriter.Services;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Service;

@Service
public class ArticleGrammarCheck {
public String duplicateLinesEliminater(String s) throws IOException {
		
		String[] tokens = s.split("\n");
		StringBuilder resultBuilder = new StringBuilder();
		Set<String> alreadyPresent = new HashSet<String>();

		boolean first = true;
		for(String token : tokens) {

		    if(!alreadyPresent.contains(token)) {
		        if(first) first = false;
		        else resultBuilder.append("\n");

		        if(!alreadyPresent.contains(token))
		            resultBuilder.append(token);
		    }

		    alreadyPresent.add(token);
		}
		s = resultBuilder.toString();
		
		System.out.println("After Duplicate lines removed " +s);
		
		return s;
		
	}
	

	public String spellChecker(String str) throws IOException {
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
}
