package ConsistentHashing;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ��������У��ڵ�֮���Ǵ���˳���ϵ�ģ�
 * ����TreeMap��key����ʵ��Comparator�ӿ�
 * @author Lenovo
 */
public final class KetamaNodeLocator {

	private TreeMap<Long, Node> ketamaNodes;  // ��¼��������������ڵ㣬Ϊʲô��Long���ͣ���ΪLongʵ����Comparable�ӿ�
	private HashAlgorithm hashAlg;
	private int numReps = 160;    // ÿ���������ڵ����ɵ�����������ڵ�������Ĭ������Ϊ160

	public KetamaNodeLocator(List<Node> nodes, HashAlgorithm alg, int nodeCopies) {
		hashAlg = alg;
		ketamaNodes = new TreeMap<Long, Node>();

		numReps = nodeCopies;

		// �����нڵ㣬����numReps��������
		for (Node node : nodes) {
			// ÿ�ĸ�������Ϊһ�飬Ϊʲô�����������˵��
			for (int i = 0; i < numReps / 4; i++) {
				// Ϊ����������õ�Ωһ����
				byte[] digest = hashAlg.computeMd5(node.getName() + i);
				/**
				 * Md5��һ��16�ֽڳ��ȵ����飬��16�ֽڵ�����ÿ�ĸ��ֽ�һ�飬
				 * �ֱ��Ӧһ�������㣬�����Ϊʲô������������ĸ�����һ���ԭ��
				 */
				for (int h = 0; h < 4; h++) {
					// ����ÿ�ĸ��ֽڣ����һ��longֵ��ֵ����Ϊ�������ڵ���ڻ��е�Ωһkey
					long m = hashAlg.hash(digest, h);

					ketamaNodes.put(m, node);
				}
			}
		}
	}

	/**
	 * ����һ��keyֵ��Hash����˳ʱ��Ѱ��һ�����������������ڵ�
	 * @param k
	 * @return
	 */
	public Node getPrimary(final String k) {
		byte[] digest = hashAlg.computeMd5(k);
		Node rv = getNodeForKey(hashAlg.hash(digest, 0));  // Ϊʲô��0���²⣺0��1��2��3�����ԣ�����Ҫ�̶�
		return rv;
	}

	Node getNodeForKey(long hash) {
		final Node rv;
		Long key = hash;
		//����ҵ�����ڵ㣬ֱ��ȡ�ڵ㣬����
		if (!ketamaNodes.containsKey(key)) {
			//�õ����ڵ�ǰkey���Ǹ���Map��Ȼ�����ȡ����һ��key�����Ǵ���������������Ǹ�key  
			SortedMap<Long, Node> tailMap = ketamaNodes.tailMap(key);
			if (tailMap.isEmpty()) {
				key = ketamaNodes.firstKey();
			} else {
				key = tailMap.firstKey();
			}
			// For JDK1.6 version
			// key = ketamaNodes.ceilingKey(key);
			// if (key == null) {
			// key = ketamaNodes.firstKey();
			// }
		}

		rv = ketamaNodes.get(key);
		return rv;
	}
}
