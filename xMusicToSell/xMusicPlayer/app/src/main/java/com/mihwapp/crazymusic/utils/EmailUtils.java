package com.mihwapp.crazymusic.utils;

import java.util.regex.Pattern;

public class EmailUtils {
	
	public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
	          "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
	          "\\@" +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
	          "(" +
	          "\\." +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
	          ")+"
	      );
	
	public static boolean isEmailAddressValid(String address) {
		return EMAIL_ADDRESS_PATTERN.matcher(address).matches();
	}

}
