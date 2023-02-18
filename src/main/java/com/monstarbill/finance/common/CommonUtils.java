package com.monstarbill.finance.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.joda.time.DateTimeComparator;

import com.monstarbill.finance.payload.response.PaginationResponse;

/**
 * 
 * @author Prashant
 *
 */
public class CommonUtils {
	
	/**
	 * get username of logged in user from the context
	 * 
	 * @return
	 */
	public static String getLoggedInUsername() {
		return "";
	}

	/**
	 * Prashant : 30-Jun-2022
	 * convert camel case text to human readable Name
	 * eg. input - "fieldName" , output - "Field Name" 
	 * @param 
	 * @return
	 */
	public static String splitCamelCaseWithCapitalize(String inputString) {
		return WordUtils.capitalize(inputString.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " "));
	}
	
	/**
	 * Prashant : 30-Jun-2022
	 * mentioned fields has no need to mention in any of history
	 * @return
	 */
	public static List<String> getUnusedFieldsOfHistory() {
		return Arrays.asList("createddate", "createdby", "lastmodifieddate", "lastmodifiedby", "approverPreferenceId", "approverSequenceId", "approverMaxLevel");
	}
	
	/**
	 * Compares the 2 dates and returns 
	 * -1 :: if first date is small
	 *  0 :: if dates are same
	 *  1 :: if first date is larger
	 * @param first
	 * @param second
	 * @return
	 */
	public static int compareDates(Date first, Date second) {
		return DateTimeComparator.getDateOnlyInstance().compare(first, second);
	}
	
	/**
	 * set the data for all pagination list
	 * @param pageNumber
	 * @param pageSize
	 * @param list
	 * @param totalRecords
	 * @return
	 */
	public static PaginationResponse setPaginationResponse(int pageNumber, int pageSize, List<?> list, Long totalRecords) {
		PaginationResponse paginationResponse = new PaginationResponse();
		paginationResponse.setList(list);
		paginationResponse.setPageNumber(pageNumber);
		paginationResponse.setPageSize(pageSize);
		paginationResponse.setTotalRecords(totalRecords);
		Long totalPages = (totalRecords / pageSize);
		if (totalRecords % pageSize != 0) {
			totalPages++;
		}
		paginationResponse.setTotalPages(totalPages );
		return paginationResponse;
	}
	
	public static String prepareOrderByClause(String sortColumn, String sortOrder) {
		return " ORDER BY " + sortColumn + " " + sortOrder;
	}
	
	/**
	 * this method will return month number by month name
	 * @param monthName
	 * @return
	 * @throws ParseException
	 */
	public static int getMonthNumberByName(String monthName) throws ParseException {
	    Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(monthName);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int monthNumber = cal.get(Calendar.MONTH)+1;
		return monthNumber;
	}
	
	/**
	 * This function converts the list to comma separated string containing single quotes
	 * @param list
	 * @return
	 */
	public static String convertListToCommaSepratedStringWithQuotes(List<?> list) {
		String commaSepratedString = "'" + StringUtils.join(list, "','") + "'";
		return commaSepratedString;
	}
//	private String getCommaSeparatedStringWithQuotes(List<String> namesList) {
//	    return String.join(",", namesList
//	            .stream()
//	            .map(name -> ("'" + name + "'"))
//	            .collect(Collectors.toList()));
//	}
	
	/**
	 * This function converts the list to comma separated string without single quotes
	 * @param list
	 * @return
	 */
	public static String convertListToCommaSepratedWithoutQuotes(List<?> list) {
		String commaSepratedString = StringUtils.join(list,",");
		return commaSepratedString;
	}
	
	/**
	 * Convert date to format - yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String convertDateToFormattedString(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date);
	}
	
	public static List<?> convertObjectToList(Object obj) {
	    List<?> list = new ArrayList<>();
	    if (obj.getClass().isArray()) {
	        list = Arrays.asList((Object[])obj);
	    } else if (obj instanceof Collection) {
	        list = new ArrayList<>((Collection<?>)obj);
	    }
	    return list;
	}
	
	/*
	 * send email core functionality
	 */
	public static boolean sendMail(String toEmail, String ccMail, String subject, String body) throws Exception {
		try {
			final String FROM_EMAIL_ID = "no-reply@monstarbill.com";
			final String PASSWORD = "jpkcbfxkxolyqcnq";

			Properties properties = new Properties();
			properties.setProperty("mail.transport.protocol", "smtp");
			properties.setProperty("mail.host", "smtp.gmail.com");
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.port", "465");
			properties.put("mail.debug", "true");
			properties.put("mail.smtp.socketFactory.port", "465");
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.socketFactory.fallback", "false");
			properties.put("mail.smtp.starttls.enable", "true");
			
			Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(FROM_EMAIL_ID, PASSWORD);
				}
			});

			// session.setDebug(true);
			Transport transport = session.getTransport();
			InternetAddress addressFrom = new InternetAddress(FROM_EMAIL_ID);
	
			MimeMessage message = new MimeMessage(session);
			message.setSender(addressFrom);
			message.setSubject(subject);
			message.setContent(body, "text/html");
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			if (StringUtils.isNotEmpty(ccMail)) {
				message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));
			}
	
			transport.connect();
			Transport.send(message);
			transport.close();
			
			return true;
		} catch(Exception e) {
			System.err.println("Error while sending the mail.");
			e.printStackTrace();
			throw new CustomMessageException("Error while sending the mail.");
		}
	}

}
