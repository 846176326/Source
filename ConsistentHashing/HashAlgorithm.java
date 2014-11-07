package ConsistentHashing;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hash算法，通过MD5算法实现
 * MD5算法根据key生成一个16字节的序列，我们将其切成4段，将其中一段作为得到的Hash值
 * 在生成虚拟服务器节点中，我们将这四段分别作为四个虚拟服务器节点的唯一标识，即四个hash值
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
		 * 实际我们只需要后32位即可，为什么返回一个long类型？
		 * 因为Long实现了Comparable接口
		 * Hash环上的节点之间是存在顺序关系的，必须实现Comparable接口
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

