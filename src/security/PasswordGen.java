package security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public class PasswordGen {
	
	private static String removeRepeatChars(String password) {
		char[] replacementCharSet = "abcdefghijklmnopqrstuvwxyxABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		char[] newPassword = password.toCharArray();
		SecureRandom sr = new SecureRandom();
		Pattern repeatCharPattern = Pattern.compile("([a-zA-Z0-9])\\1+");
		Matcher matcher = repeatCharPattern.matcher(password);
		
		while (matcher.find()) {
			int dupePosition = matcher.start();
			char duplicateChar = password.charAt(dupePosition);
			char replacementChar = replacementCharSet[sr.nextInt()];
			while (duplicateChar == replacementChar) {
				replacementChar = replacementCharSet[sr.nextInt()];
			}
			newPassword[dupePosition] = replacementChar;
		}
		
		String finalPassword = new String(newPassword);
		return finalPassword;
	}
	
	public String generatePassword(Boolean containsUpper, Boolean containsLower, Boolean containsDigit, Boolean containsSpecial, Boolean avoidRepetition, int passwordLength) {
		PasswordGenerator generator = new PasswordGenerator();
		List<CharacterRule> passRules = new ArrayList<CharacterRule>();
		
		if (containsUpper) {
			CharacterRule upperRule = new CharacterRule(EnglishCharacterData.UpperCase, 1);
			passRules.add(upperRule);
		}
		if (containsLower) {
			CharacterRule lowerRule = new CharacterRule(EnglishCharacterData.LowerCase, 1);
			passRules.add(lowerRule);
		}
		if (containsDigit) {
			CharacterRule digitRule = new CharacterRule(EnglishCharacterData.Digit, 1);
			passRules.add(digitRule);
		}
		if (containsSpecial) {
			char[] specialCharacters = "!@#$%^&*()-=_+[]{}|\'\"\\/?:.~".toCharArray();
			int numSpecChars = specialCharacters.length;
			SecureRandom sr = new SecureRandom();
			
			if (!containsUpper && !containsLower && !containsDigit) {
				// Password will contain ONLY special characters, randomly selected
				char[] specOnlyPassword = new char[passwordLength];
				for (int i = 0; i < passwordLength; i++) {
					specOnlyPassword[i] = specialCharacters[sr.nextInt(numSpecChars)];
				}
				
				String finalPassword = new String(specOnlyPassword);
				
				if (avoidRepetition) {
					return removeRepeatChars(finalPassword);
				}
				else {
					return finalPassword;
				}
			}
			else {
				// Password will contain AT LEAST ONE special characters
				String tempPassword = generator.generatePassword(passwordLength, passRules);
				char[] tempPassArray = tempPassword.toCharArray();
				// Replace a random amount of letters with special characters
				int numReplacements = sr.nextInt(passwordLength / 2);
				int i = 0;
				do {
					tempPassArray[sr.nextInt(passwordLength)] = specialCharacters[sr.nextInt(numSpecChars)];
				} while (++i < numReplacements);
				
				String finalPassword = new String(tempPassArray);
				
				if (avoidRepetition) {
					return removeRepeatChars(finalPassword);
				}
				else {
					return finalPassword;
				}
			}
		}
		
		String finalPassword = generator.generatePassword(passwordLength, passRules);
		
		if (avoidRepetition) {
			return removeRepeatChars(finalPassword);
		}
		else {
			return finalPassword;
		}
	}
	
	public static void main(String args[]) {
		PasswordGen pg = new PasswordGen();
		String poopword = pg.generatePassword(false, false, false, true, true, 16);
		System.out.println(poopword);
	}
}