/**
 * Copyright (c) 2013 Sveriges Kommuner och Landsting (SKL). <http://www.skl.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.ei.intsvc.integrationtests.notifyservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.processnotification._1.rivtabp21.ProcessNotificationResponderInterface;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationResponseType;
import riv.itintegration.engagementindex.processnotificationresponder._1.ProcessNotificationType;

import javax.jws.WebService;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


@WebService(
        serviceName = "ProcessNotificationService", 
        portName = "ProcessNotificationPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:ProcessNotification:1:rivtabp21")
public class ProcessNotificationTestProducer implements ProcessNotificationResponderInterface {

    public static final long TEST_ID_FAULT_TIMEOUT = 0;
    public static final String FULL_TEST_ID_FAULT_TIMEOUT = "19" + TEST_ID_FAULT_TIMEOUT;
    
	private static final Logger log = LoggerFactory.getLogger(ProcessNotificationTestProducer.class);
	private static final Logger datalog = LoggerFactory.getLogger("NOTIFICATIONDATA");

	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
	
	@Value("timeout")
	private long SERVICE_TIMOUT_MS;

	private static MessageDigest md;
	static {
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
	}
	@Override
	public ProcessNotificationResponseType processNotification(String logicalAddress, ProcessNotificationType request) {

		log.info("ProcessNotificationTestProducer received a notification request with {} transactions for logical-address {}", request.getEngagementTransaction().size(), logicalAddress);

		// Get current date and time
		String now = dateFormat.format(Calendar.getInstance().getTime());
		
		// Get the list of transactions
		List<EngagementTransactionType> eiTran = request.getEngagementTransaction();

		// Extract and log requested data
		for(EngagementTransactionType t : eiTran) {
			boolean del = t.isDeleteFlag();
			EngagementType e = t.getEngagement();

			String domain = e.getServiceDomain();
			String cat = e.getCategorization();
			String source = e.getSourceSystem();
			String logAddr = e.getLogicalAddress();
			String person = e.getRegisteredResidentIdentification();
			String creationTime = e.getCreationTime();
			String updateTime = e.getUpdateTime();

			datalog.info("{}, {}, {}, {}, {}, {}, {}, {}, {}", now, createHash(person), logAddr, source, domain, cat, del, creationTime, updateTime);
		}

        // Force a timeout if timeout Id
        String residentId = request.getEngagementTransaction().get(0).getEngagement().getRegisteredResidentIdentification();
		if (FULL_TEST_ID_FAULT_TIMEOUT.equals(residentId)) forceTimeout();

        ProcessNotificationResponseType response = new ProcessNotificationResponseType();
        response.setComment("");
        response.setResultCode(ResultCodeEnum.OK);
		return response;
	}

	private static String createHash(String person) {
		
		if(person == null || person.length() == 0)
			return "00000000000000";
		
		byte[] digest;
		String encodedPerson;
		try {
			md.update(person.getBytes("UTF-8"));
			digest = md.digest();
			encodedPerson = String.format("%064x", new java.math.BigInteger(1, digest));
			
			// Truncate string to last 14 chars
			int len = encodedPerson.length();
			if(len > 14) 
				len = len - 14;
			encodedPerson = encodedPerson.substring(len);
		} catch (UnsupportedEncodingException e1) {
			log.error(e1.getMessage());
			encodedPerson = "00000000000000";
		} // Change this to "UTF-16" if needed
		
		return encodedPerson;
	}
	
    private void forceTimeout() {
        try {
            log.info("TestProducer force a timeout to happen...");
            Thread.sleep(SERVICE_TIMOUT_MS + 1000);
        } catch (InterruptedException e) {}
    }
    
/*
    
    public static void main(String[] args) throws InterruptedException {
		System.out.println(createHash("12121212-1212"));
		String now = dateFormat.format(Calendar.getInstance().getTime());
		System.out.println(now);
		Thread.sleep(2000);
		now = dateFormat.format(Calendar.getInstance().getTime());
		System.out.println(now);
	}
*/
}
