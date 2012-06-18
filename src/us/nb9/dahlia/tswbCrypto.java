package us.nb9.dahlia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class tswbCrypto {
	static private final String TAG = "tswbCrypto_lxy";
	Context mContext;
	PublicKey pubkey;
	// �õ�˽��
	PrivateKey prikey;
	byte[] signed;
	String mInfo;
	
	tswbCrypto(Context context) {
		mContext = context;
	}
	

	/**
	 * ����MD5����
	 * 
	 * @param info
	 *            Ҫ���ܵ���Ϣ
	 * @return String ���ܺ���ַ���
	 */
	public String encryptToMD5(String info) {
		byte[] digesta = null;
		try {
			// �õ�һ��md5����ϢժҪ
			MessageDigest alga = MessageDigest.getInstance("MD5");
			// ���Ҫ���м���ժҪ����Ϣ
			alga.update(info.getBytes());
			// �õ���ժҪ
			digesta = alga.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// ��ժҪתΪ�ַ���
		String rs = byte2hex(digesta);
		return rs;
	}
	
	/**
	 * ����SHA����
	 * 
	 * @param info
	 *            Ҫ���ܵ���Ϣ
	 * @return String ���ܺ���ַ���
	 */
	public String encryptToSHA(String info) {
		byte[] digesta = null;
		try {
			// �õ�һ��SHA-1����ϢժҪ
			MessageDigest alga = MessageDigest.getInstance("SHA-1");
			// ���Ҫ���м���ժҪ����Ϣ
			alga.update(info.getBytes());
			// �õ���ժҪ
			digesta = alga.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// ��ժҪתΪ�ַ���
		String rs = byte2hex(digesta);
		return rs;
	}
	
	// //////////////////////////////////////////////////////////////////////////
	/**
	 * �����ܳ�
	 * 
	 * @param algorithm
	 *            �����㷨,���� DES,DESede,Blowfish
	 * @return SecretKey ���ܣ��Գƣ���Կ
	 */
	public SecretKey createSecretKey(String algorithm) {
		// ����KeyGenerator����
		KeyGenerator keygen;
		// ���� ��Կ����
		SecretKey deskey = null;
		try {
			// ��������ָ���㷨��������Կ�� KeyGenerator ����
			keygen = KeyGenerator.getInstance(algorithm);
			// ����һ����Կ
			deskey = keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// �����ܳ�
		return deskey;
	}
	
	/**
	 * �����ܳ׽���DES����
	 * 
	 * @param key
	 *            �ܳ�
	 * @param info
	 *            Ҫ���ܵ���Ϣ
	 * @return String ���ܺ����Ϣ
	 */
	public String encryptToDES(SecretKey key, String info) {
		// ���� �����㷨,���� DES,DESede,Blowfish
		String Algorithm = "DES";
		// ��������������� (RNG),(���Բ�д)
		SecureRandom sr = new SecureRandom();
		// ����Ҫ���ɵ�����
		byte[] cipherByte = null;
		try {
			// �õ�����/������
			Cipher c1 = Cipher.getInstance(Algorithm);
			// ��ָ������Կ��ģʽ��ʼ��Cipher����
			// ����:(ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE,UNWRAP_MODE)
			c1.init(Cipher.ENCRYPT_MODE, key, sr);
			// ��Ҫ���ܵ����ݽ��б��봦��,
			cipherByte = c1.doFinal(info.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// �������ĵ�ʮ��������ʽ
		return byte2hex(cipherByte);
	}
	
	/**
	 * �����ܳ׽���DES����
	 * 
	 * @param key
	 *            �ܳ�
	 * @param sInfo
	 *            Ҫ���ܵ�����
	 * @return String ���ؽ��ܺ���Ϣ
	 */
	public String decryptByDES(SecretKey key, String sInfo) {
		// ���� �����㷨,
		String Algorithm = "DES";
		// ��������������� (RNG)
		SecureRandom sr = new SecureRandom();
		byte[] cipherByte = null;
		try {
			// �õ�����/������
			Cipher c1 = Cipher.getInstance(Algorithm);
			// ��ָ������Կ��ģʽ��ʼ��Cipher����
			c1.init(Cipher.DECRYPT_MODE, key, sr);
			// ��Ҫ���ܵ����ݽ��б��봦��
			cipherByte = c1.doFinal(hex2byte(sInfo));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return byte2hex(cipherByte);
		return new String(cipherByte);
	}
	
	// /////////////////////////////////////////////////////////////////////////////
	/**
	 * �����ܳ��飬�������ף�˽�׷��뵽ָ���ļ���
	 * 
	 * Ĭ�Ϸ���mykeys.bat�ļ���
	 */
	public void createPairKey() {
		try {
			// �����ض����㷨һ����Կ��������
			KeyPairGenerator keygen = KeyPairGenerator.getInstance("DSA");
			// ��������������� (RNG)
			SecureRandom random = new SecureRandom();
			// �������ô�������������
			random.setSeed(1000);
			// ʹ�ø��������Դ����Ĭ�ϵĲ������ϣ���ʼ��ȷ����Կ��С����Կ��������
			keygen.initialize(512, random);// keygen.initialize(512);
			// ������Կ��
			KeyPair keys = keygen.generateKeyPair();
			// �õ�����
			pubkey = keys.getPublic();
			// �õ�˽��
			prikey = keys.getPrivate();
			// ������˽��д�뵽�ļ�����
			//doObjToFile("mykeys.bat", new Object[] { prikey, pubkey });
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����˽�׶���Ϣ����ǩ�� ��ǩ�������Ϣ���뵽ָ�����ļ���
	 * 
	 * @param info
	 *            Ҫǩ������Ϣ
	 * @param signfile
	 *            ������ļ�
	 */
	public void signToInfo(String info, String signfile) {
		// ���ļ����ж�ȡ˽��
		//PrivateKey myprikey = (PrivateKey) getObjFromFile("mykeys.bat", 1);
		PrivateKey myprikey = prikey;
		// ���ļ��ж�ȡ����
		//PublicKey mypubkey = (PublicKey) getObjFromFile("mykeys.bat", 2);
		PublicKey mypubkey = pubkey;
		mInfo = info;
		try {
			// Signature ������������ɺ���֤����ǩ��
			Signature signet = Signature.getInstance("DSA");
			// ��ʼ��ǩ��ǩ����˽Կ
			signet.initSign(myprikey);
			// ����Ҫ���ֽ�ǩ������֤������
			signet.update(info.getBytes());
			// ǩ�����֤���и����ֽڵ�ǩ��������ǩ��
			signed = signet.sign();
			// ������ǩ��,����,��Ϣ�����ļ���
			//doObjToFile(signfile, new Object[] { signed, mypubkey, info });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ����ǩ���ļ� ���ݹ��ף�ǩ������Ϣ��֤��Ϣ�ĺϷ���
	 * 
	 * @return true ��֤�ɹ� false ��֤ʧ��
	 */
	public boolean validateSign(String signfile) {
		// ��ȡ����
		//PublicKey mypubkey = (PublicKey) getObjFromFile(signfile, 2);
		PublicKey mypubkey = pubkey;
		// ��ȡǩ��
		//byte[] signed = (byte[]) getObjFromFile(signfile, 1);
		// ��ȡ��Ϣ
		//String info = (String) getObjFromFile(signfile, 3);
		String info = mInfo;
		try {
			// ��ʼһ��Signature����,���ù�Կ��ǩ��������֤
			Signature signetcheck = Signature.getInstance("DSA");
			// ��ʼ����֤ǩ���Ĺ�Կ
			signetcheck.initVerify(mypubkey);
			// ʹ��ָ���� byte �������Ҫǩ������֤������
			signetcheck.update(info.getBytes());
			System.out.println(info);
			// ��֤�����ǩ��
			return signetcheck.verify(signed);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * ��������ת��Ϊ16�����ַ���
	 * 
	 * @param b
	 *            �������ֽ�����
	 * @return String
	 */
	public String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	
	/**
	 * ʮ�������ַ���ת��Ϊ2����
	 * 
	 * @param hex
	 * @return
	 */
	public byte[] hex2byte(String hex) {
		byte[] ret = new byte[8];
		byte[] tmp = hex.getBytes();
		for (int i = 0; i < 8; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}
	
	/**
	 * ������ASCII�ַ��ϳ�һ���ֽڣ� �磺"EF"--> 0xEF
	 * 
	 * @param src0
	 *            byte
	 * @param src1
	 *            byte
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}
	

	/**
	 * �������ļ���ָ��λ�õĶ���
	 * 
	 * @param file
	 *            ָ�����ļ�
	 * @param i
	 *            ��1��ʼ
	 * @return
	 */
	/*
	public Object getObjFromFile(String file, int i) {
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			for (int j = 0; j < i; j++) {
				obj = ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
	*/
	
	public SecretKey getObjFromFile(String file, int i) {
		ObjectInputStream ois = null;
		SecretKey objKey = null;
		try {
			//FileInputStream fis = new FileInputStream(file);
			FileInputStream fis = mContext.openFileInput(file);
			ois = new ObjectInputStream(fis);
			for (int j = 0; j < i; j++) {
				objKey = (SecretKey) ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return objKey;
	}
	
	/**
	 * ��ָ���Ķ���д��ָ�����ļ�
	 * 
	 * @param file
	 *            ָ��д����ļ�
	 * @param objs
	 *            Ҫд��Ķ���
	 */
	
	public void doObjToFile(String file, Object[] objs) {
		String filePath;
		//filePath = Environment.getDataDirectory().toString()+"/"+file;
		//filePath = Environment.getExternalStorageDirectory().toString()+"/"+file;
		filePath = file;
		Log.i(TAG, "filePath = " + filePath);
		ObjectOutputStream oos = null;
		try {
			//FileOutputStream fos = new FileOutputStream(filePath);
			FileOutputStream fos = mContext.openFileOutput(filePath, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			for (int i = 0; i < objs.length; i++) {
				oos.writeObject(objs[i]);
			}
		} catch (Exception e) {
			Log.i(TAG, "haha1");
			e.printStackTrace();
		} finally {
			try {
				Log.i(TAG, "haha2");
				oos.close();
			} catch (IOException e) {
				Log.i(TAG, "haha3");
				e.printStackTrace();
			}
		}
	}
	
	public void doObjToFile(String file, SecretKey key_objs) {
		String filePath;
		//filePath = Environment.getDataDirectory().toString()+"/"+file;
		//filePath = Environment.getExternalStorageDirectory().toString()+"/"+file;
		filePath = file;
		Log.i(TAG, "filePath = " + filePath);
		ObjectOutputStream oos = null;
		try {
			//FileOutputStream fos = new FileOutputStream(filePath);
			FileOutputStream fos = mContext.openFileOutput(filePath, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(key_objs);
		} catch (Exception e) {
			Log.i(TAG, "haha1");
			e.printStackTrace();
		} finally {
			try {
				Log.i(TAG, "haha2");
				oos.close();
			} catch (IOException e) {
				Log.i(TAG, "haha3");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����
	 * 
	 * @param args
	 */
	public void myCryptoTest() {
		tswbCrypto jiami = this;
		// ִ��MD5����"Hello world!"
		System.out.println("Hello����MD5:" + jiami.encryptToMD5("Hello"));
		
		// ����һ��DES�㷨���ܳ�
		SecretKey key = jiami.createSecretKey("DES");
		Log.i(TAG, "key = " + key);
		// ���ܳ׼�����Ϣ"Hello world!"
		String str1 = jiami.encryptToDES(key, "Hello");
		System.out.println("ʹ��des������ϢHelloΪ:" + str1);
		// ʹ������ܳ׽���
		String str2 = jiami.decryptByDES(key, str1);
		System.out.println("���ܺ�Ϊ��" + str2);
		doObjToFile("DesKey.dat", key);
		SecretKey keyFile = getObjFromFile("DesKey.dat", 1);
		String str3 = decryptByDES(keyFile, str1);
		System.out.println("str3���ܺ�Ϊ��" + str3);

		
		// �������׺�˽��
		jiami.createPairKey();
		// ��Hello world!ʹ��˽�׽���ǩ��
		jiami.signToInfo("Hello", "mysign.bat");
		// ���ù��׶�ǩ��������֤��
		if (jiami.validateSign("mysign.bat")) {
			System.out.println("Success!");
		} else {
			System.out.println("Fail!");
		}
	}
}
