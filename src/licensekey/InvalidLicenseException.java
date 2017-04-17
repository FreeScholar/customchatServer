package customchat.licensekey;

import java.security.*;
import java.io.*;
import java.util.*;
import java.net.*;

class InvalidLicenseException extends Exception {
	public InvalidLicenseException(String mesg) {
		super(mesg);
	}
}
