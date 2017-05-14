package com;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRegEx {

	private static final Logger logger = LoggerFactory.getLogger(TestRegEx.class);

	@Value("${service.annexes.regex}")
	private String regExpList;

	@Test
	public void testHebrewMatch() {

		
		List<String> res = new ArrayList<String>();
		// Create a Pattern object
		Pattern r = Pattern.compile(regExpList);
		
		String[] lines = {"*** העתק גדכדכדכדגכדכדגכדגכגדכדג","*** עותק דכדגכדכגכדגכדכדגכ" , "שלום"};
		for (String line : lines) {
			// Now create matcher object.
			Matcher m = r.matcher(line);
			if (m.find()) {
				res.add(line + "\n");
			}
		}
		for (String string : res) {
			System.out.println(string);
		}
	}
}