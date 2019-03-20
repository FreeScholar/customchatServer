package com.chatalot.server.licensekey;

import java.security.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class License implements Serializable {
//	public static final long serialVersionUID= 3003915261832683421L;
//added by ritchie
static final long serialVersionUID = 8932408958778618078L;
	private static final long p1  =  1586772116408018644L;
	private static final long p2  =  8332344923931526881L;
	private static final long p3  = -7395717268348254312L;
	private static final long p4  =  4859041558364694914L;
	private static final long p5  = -5732629764791566610L;
	private static final long p6  = -4113916765976433114L;
	private static final long p7  = -7847091137905363228L;
	private static final long p8  =  7461730258881510757L;
	private static final long p9  =  749244209925268945L;
	private static final long p10 = -8042235109570871732L;
	private static final long p11 =  8822752954864835784L;

	private String nameFirst = "Joe";
	private byte[] nameFirstCheck;
	private String nameLast = "Smith";
	private byte[] nameLastCheck;
	private String nameMiddle = "Harley";
	private byte[] nameMiddleCheck;

	private String addressStreet = "315 MainStreet";
	private byte[] addressStreetCheck;
	private String addressCity = "Ashton";
	private byte[] addressCityCheck;
	private String addressState = "Massachusetts";
	private byte[] addressStateCheck;
	private String addressCountry = "USA";
	private byte[] addressCountryCheck;
	private String addressZip = "02458";
	private byte[] addressZipCheck;

	private String email = "info@customchat.com";
	private byte[] emailCheck;
	private String ip = "http://127.0.0.1:6743";
	private byte[] ipCheck;

	private long   noUsers = 10;
	private byte[] noUsersCheck;
	private long   version = 1;
	private byte[] versionCheck;
	private Date starts = new Date(1018, 1, 1);
	private byte[] startsCheck;
	private Date ends = new Date(1020, 1, 1);
	private byte[] endsCheck;

	private byte[] key;

	private URL addr;

	class loopBackServer extends Thread {
		ServerSocket ss;
		Socket  s;
		ObjectInputStream in;
		ObjectOutputStream out;
		Double d1, d2;
		boolean passed = false;

		loopBackServer(int port, double d1, double d2) throws IOException{
			ss = new ServerSocket(port);
			this.d1 = new Double(d1);
			this.d2 = new Double(d2);
			start();
		}

		public void run() {
			try {
				s = ss.accept();
				out = new ObjectOutputStream(s.getOutputStream());
				in  = new ObjectInputStream(s.getInputStream());
				out.writeObject(d1);
				Double d2a = (Double)in.readObject();
				passed = d2a.equals(d2);
			} catch(Exception e) {
			  System.err.println(e.getMessage());
			  System.exit(-1);
			} finally {
			  try {
				s.close();
			  } catch (Exception ignored) {}
			  try {
				ss.close();
			  } catch (Exception ignored) {}
			}
		}

		public boolean passed() {
			return passed;
		}
	}

	public License(String nameFirst,
					String nameLast,
					String nameMiddle,

					String addressStreet,
					String addressCity,
					String addressState,
					String addressZip,
					String addressCountry,
					String email,
					String ip,

					long   noUsers,
					long   version,
					Date starts,
					Date ends)
	throws NoSuchAlgorithmException, MalformedURLException {
		MessageDigest md = MessageDigest.getInstance("MD5");

		this.nameFirst = nameFirst;
		nameFirstCheck = md.digest(nameFirst.getBytes());

		this.nameLast  = nameLast;
		nameLastCheck  = md.digest(nameLast.getBytes());

		this.nameMiddle= nameMiddle;
		nameMiddleCheck= md.digest(nameMiddle.getBytes());


		this.addressStreet = addressStreet;
		addressStreetCheck = md.digest(addressStreet.getBytes());

		this.addressState  = addressState;
		addressStateCheck  = md.digest(addressState.getBytes());

		this.addressCity   = addressCity;
		addressCityCheck   = md.digest(addressCity.getBytes());

		this.addressZip     = addressZip;
		addressZipCheck     = md.digest(addressZip.getBytes());

		this.addressCountry = addressCountry;
		addressCountryCheck = md.digest(addressCountry.getBytes());


		this.email = email;
		emailCheck = md.digest(email.getBytes());

		this.ip    = ip;
		ipCheck    = md.digest(ip.getBytes());

		this.noUsers = noUsers;
		noUsersCheck   = md.digest(String.valueOf(noUsers).getBytes());

		this.version = version;
		versionCheck = md.digest(String.valueOf(version).getBytes());

		this.starts = starts;
		
		startsCheck = md.digest(starts.toGMTString().getBytes());

		this.ends   = ends;
		endsCheck   = md.digest(ends.toGMTString().getBytes());

		key = getKey();

		addr = new URL(ip);
	}
	public final String date() {
	  return ends.toString();
	}
/**
 * Insert the method's description here.
 * Creation date: (4/9/00 4:15:46 PM)
 * @return java.lang.String
 */
public URL getAddr() {
	return addr;
}
	public static License getInstance(File f, int iPort)
	throws InvalidLicenseException, IOException, UnsupportedEncodingException,
		   ClassNotFoundException, NoSuchAlgorithmException {
		if(f != null && !f.canRead())
			throw new InvalidLicenseException("The license file is missing or does not have read permission turned on.");

		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));


		License l = (License)in.readObject();
		if(l.isValid(iPort))
			return l;
		else
			throw new InvalidLicenseException("The license key is not valid.");
	}
	private byte[] getKey() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could not generate key.");
			System.exit(-1);
		}
		md.update(String.valueOf(p1).getBytes());
		md.update(nameFirst.getBytes());
		md.update(String.valueOf(p2).getBytes());
		md.update(nameLast.getBytes());
		md.update(String.valueOf(p3).getBytes());
		md.update(nameMiddle.getBytes());
		md.update(String.valueOf(p4).getBytes());
		md.update(addressStreet.getBytes());
		md.update(String.valueOf(p5).getBytes());
		md.update(addressState.getBytes());
		md.update(String.valueOf(p6).getBytes());
		md.update(addressCountry.getBytes());
		md.update(String.valueOf(p7).getBytes());
		md.update(addressZip.getBytes());
		md.update(String.valueOf(p8).getBytes());
		md.update(email.getBytes());
		md.update(String.valueOf(p9).getBytes());
		md.update(ip.getBytes());
		md.update(String.valueOf(p10).getBytes());
		md.update(String.valueOf(noUsers).getBytes());
		md.update(String.valueOf(p11).getBytes());
		md.update(String.valueOf(version).getBytes());
		md.update(starts.toString().getBytes());
		md.update(ends.toString().getBytes());
		return md.digest();
	}
	private boolean isValid(int iport)
	throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		return
		   MessageDigest.isEqual(nameFirstCheck      , md.digest(nameFirst.getBytes()))
		&& MessageDigest.isEqual(nameLastCheck       , md.digest(nameLast.getBytes()))
		&& MessageDigest.isEqual(nameMiddleCheck     , md.digest(nameMiddle.getBytes()))
		&& MessageDigest.isEqual(addressStreetCheck  , md.digest(addressStreet.getBytes()))
		&& MessageDigest.isEqual(addressStateCheck   , md.digest(addressState.getBytes()))
		&& MessageDigest.isEqual(addressCityCheck    , md.digest(addressCity.getBytes()))
		&& MessageDigest.isEqual(addressZipCheck     , md.digest(addressZip.getBytes()))
		&& MessageDigest.isEqual(addressCountryCheck , md.digest(addressCountry.getBytes()))
		&& MessageDigest.isEqual(emailCheck          , md.digest(email.getBytes()))
		&& MessageDigest.isEqual(ipCheck             , md.digest(ip.getBytes()))
		&& MessageDigest.isEqual(noUsersCheck        , md.digest(String.valueOf(noUsers).getBytes()))
		&& MessageDigest.isEqual(versionCheck        , md.digest(String.valueOf(version).getBytes()))
		&& MessageDigest.isEqual(startsCheck         , md.digest(starts.toGMTString().getBytes()))
		&& MessageDigest.isEqual(endsCheck           , md.digest(ends.toGMTString().getBytes()))
	//	&& MessageDigest.isEqual(key                 , getKey())
		&& starts.before(new Date())
		&& ends.after(new Date())
		&& loopBackTest(iport);

	}
	private boolean loopBackTest(int port) {
	  Socket s = null;
		try {
			//int port = addr.getPort();
			InetAddress loop = InetAddress.getByName(addr.getHost());

			double d1 = Math.random();
			double d2 = Math.random();

			loopBackServer server = new loopBackServer(port, d1, d2);

			s = new Socket(loop, port);

			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in   = new ObjectInputStream(s.getInputStream());

			out.writeObject(new Double(d2));
			Double d1a = (Double)in.readObject();

			server.join(100000L);

			return((d1a.doubleValue() == d1) && server.passed());
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		  try {
			if(s!=null)
			  s.close();
		  } catch (Exception ignored) {}
		}

	}
	public static void main(String[] argv)
	throws Exception {

	//System.out.println("ARGV0 is "+argv[0]) ;

	//java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT,Locale.US);
	
 
//		License l = new License(argv[0],
//								argv[1],
//								argv[2],
//								argv[3],
//								argv[4],
//								argv[5],
//								argv[6],
//								argv[7],
//								argv[8],
//								argv[9],
//								Integer.parseInt(argv[10]),
//								Integer.parseInt(argv[11]),
//								df.parse(argv[12]),
//								df.parse(argv[13]));
License l = new License("Joe",
								"Smith",
								"Harley",
								"315 MainStreet",
								"Ashton",
								"Massachusetts",
								"02458",
								"USA",
								"info@customchat.com",
								"http://127.0.0.1:6743",
								10,
								1,
								new Date(118, 1, 1),
								new Date(120,1,1));
		l.output(new File("ccLicense"));

		//License l = new License("Joe",
								//"Smith",
								//"Harley",
								//"315 MainStreet",
								//"Ashton",
								//"Massachusetts",
								//"02458",
								//"USA",
								//"info@customchat.com",
								//"http://127.0.0.1:6743",
								//10,
								//1,
								//new Date(99,1,1),
								//new Date(101,1,1));
		//l.output(new File("ccLicense"));
	}
	public final long maxChatters() {
		return this.noUsers;
	}
	private final void output(File f)
	throws IOException, UnsupportedEncodingException {
		if(f == null && !f.canWrite())
			throw new IOException();

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(this);
	}
	public final int port() {
		return addr.getPort();
	}
}
