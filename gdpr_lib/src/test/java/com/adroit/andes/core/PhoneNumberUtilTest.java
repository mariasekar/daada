package com.adroit.andes.core;

import org.junit.Test;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.ShortNumberInfo;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

public class PhoneNumberUtilTest {

	@SuppressWarnings("unused")
	@Test
	public void test() {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    	try {
    		ShortNumberInfo shortInfo = ShortNumberInfo.getInstance();
			//PhoneNumber phoneNumber = phoneUtil.parse("35885", "NG");
			//PhoneNumber phoneNumber = phoneUtil.parse("200", "NG");
			//PhoneNumber phoneNumber = phoneUtil.parse("8039958887", "NG");
			PhoneNumber phoneNumber = phoneUtil.parse("4223565", "NG");
			System.out.println(phoneUtil.formatNationalNumberWithPreferredCarrierCode(phoneNumber, ""));
			PhoneNumberOfflineGeocoder geoCoder = PhoneNumberOfflineGeocoder.getInstance();
			String desc = geoCoder.getDescriptionForNumber(phoneNumber, java.util.Locale.ENGLISH);
			System.out.println(desc);
			System.out.println(phoneNumber.toString());
			System.out.println(phoneNumber.getPreferredDomesticCarrierCode());
			
			String nationalSignificantNumber = phoneUtil.getNationalSignificantNumber(phoneNumber);
			int nationalDestinationCodeLength = phoneUtil.getLengthOfNationalDestinationCode(phoneNumber);

			if (nationalDestinationCodeLength > 0) {
				System.out.println(nationalSignificantNumber);
				System.out.println(nationalSignificantNumber.substring(0, nationalDestinationCodeLength));
			}
			PhoneNumberToCarrierMapper phoneNumberToCarrierMapper = PhoneNumberToCarrierMapper.getInstance();
			System.out.println(shortInfo.isPossibleShortNumber(phoneNumber));
			
		} catch (NumberParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
