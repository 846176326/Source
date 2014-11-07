package ConsistentHashing;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hash�㷨��ͨ��MD5�㷨ʵ��
 * MD5�㷨����key����һ��16�ֽڵ����У����ǽ����г�4�Σ�������һ����Ϊ�õ���Hashֵ
 * ����������������ڵ��У����ǽ����Ķηֱ���Ϊ�ĸ�����������ڵ��Ψһ��ʶ�����ĸ�hashֵ
 * @author XXX
 */
public enum HashAlgorithm {

	/**
	 * MD5-based hash algorithm used by ketama.
	 */
	KETAMA_HASH;

	public long hash(byte[] digest, int nTime) {
		long rv = ((long) (digest[3+nTime*4] & 0xFF) << 24)
				| ((long) (digest[2+nTime*4] & 0xFF) << 16)
				| ((long) (digest[1+nTime*4] & 0xFF) << 8)
				| (digest[0+nTime*4] & 0xFF);
		
		/**
		 * ʵ������ֻ��Ҫ��32λ���ɣ�Ϊʲô����һ��long���ͣ�
		 * ��ΪLongʵ����Comparable�ӿ�
		 * Hash���ϵĽڵ�֮���Ǵ���˳���ϵ�ģ�����ʵ��Comparable�ӿ�
		 */
		return rv & 0xffffffffL; /* Truncate to 32-bits */
	}

	/**
	 * Get the md5 of the given key.
	 */
	public byte[] computeMd5(String k) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported", e);
		}
		md5.reset();
		byte[] keyBytes = null;
		try {
			keyBytes = k.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unknown string :" + k, e);
		}
		
		md5.update(keyBytes);
		return md5.digest();
	}
}

