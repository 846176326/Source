package ConsistentHashing;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 在这个环中，节点之间是存在顺序关系的，
 * 所以TreeMap的key必须实现Comparator接口
 * @author Lenovo
 */
public final class KetamaNodeLocator {

	private TreeMap<Long, Node> ketamaNodes;  // 记录所有虚拟服务器节点，为什么是Long类型，因为Long实现了Comparable接口
	private HashAlgorithm hashAlg;
	private int numReps = 160;    // 每个服务器节点生成的虚拟服务器节点数量，默认设置为160

	public KetamaNodeLocator(List<Node> nodes, HashAlgorithm alg, int nodeCopies) {
		hashAlg = alg;
		ketamaNodes = new TreeMap<Long, Node>();

		numReps = nodeCopies;

		// 对所有节点，生成numReps个虚拟结点
		for (Node node : nodes) {
			// 每四个虚拟结点为一组，为什么这样？下面会说到
			for (int i = 0; i < numReps / 4; i++) {
				// 为这组虚拟结点得到惟一名称
				byte[] digest = hashAlg.computeMd5(node.getName() + i);
				/**
				 * Md5是一个16字节长度的数组，将16字节的数组每四个字节一组，
				 * 分别对应一个虚拟结点，这就是为什么上面把虚拟结点四个划分一组的原因
				 */
				for (int h = 0; h < 4; h++) {
					// 对于每四个字节，组成一个long值数值，做为这个虚拟节点的在环中的惟一key
					long m = hashAlg.hash(digest, h);

					ketamaNodes.put(m, node);
				}
			}
		}
	}

	/**
	 * 根据一个key值在Hash环上顺时针寻找一个最近的虚拟服务器节点
	 * @param k
	 * @return
	 */
	public Node getPrimary(final String k) {
		byte[] digest = hashAlg.computeMd5(k);
		Node rv = getNodeForKey(hashAlg.hash(digest, 0));  // 为什么是0？猜测：0、1、2、3都可以，但是要固定
		return rv;
	}

	Node getNodeForKey(long hash) {
		final Node rv;
		Long key = hash;
		//如果找到这个节点，直接取节点，返回
		if (!ketamaNodes.containsKey(key)) {
			//得到大于当前key的那个子Map，然后从中取出第一个key，就是大于且离它最近的那个key  
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
