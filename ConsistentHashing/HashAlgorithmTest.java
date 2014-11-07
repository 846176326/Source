package ConsistentHashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 分布平均性测试
 * @author Lenovo
 */
public class HashAlgorithmTest {

	static Random ran = new Random();
	
	// key的数量，key在实际客户端中是根据要存储的值产生的hash序列？
	private static final Integer EXE_TIMES = 100000;
	// 服务器节点的数量
	private static final Integer NODE_COUNT = 5;
	// 每个服务器节点生成的虚拟节点数量
	private static final Integer VIRTUAL_NODE_COUNT = 160;
	
	/**
	 * 模拟EXE_TIMES个客户端数据存储时选择缓存服务器的情况，
	 * 得到每个服务器节点所存储的值的数量，从而计算出值在服务器节点的分布情况
	 * 判断该算法的"性能"，正常情况下要求均匀分布
	 * @param args
	 */
	public static void main(String[] args) {
		HashAlgorithmTest test = new HashAlgorithmTest();
		
		// 记录每个服务器节点所分布到的key节点数量
		Map<Node, Integer> nodeRecord = new HashMap<Node, Integer>();
		
		// 模拟生成NODE_COUNT个服务器节点
		List<Node> allNodes = test.getNodes(NODE_COUNT);
		// 将服务器节点根据Hash算法扩展成VIRTUAL_NODE_COUNT个虚拟节点布局到Hash环上（实际上是一棵搜索树）
		// 由KetamaNodeLocator类实现和记录
		KetamaNodeLocator locator = 
				new KetamaNodeLocator(allNodes, HashAlgorithm.KETAMA_HASH, VIRTUAL_NODE_COUNT);
		
		// 模拟生成随机的key值（由长度50以内的字符组成）
		List<String> allKeys = test.getAllStrings();
		for (String key : allKeys) {
			// 根据key在Hash环上找到相应的服务器节点node
			Node node = locator.getPrimary(key);
			
			// 记录每个服务器节点分布到的数据个数
			Integer times = nodeRecord.get(node);
			if (times == null) {
				nodeRecord.put(node, 1);
			} else {
				nodeRecord.put(node, times + 1);
			}
		}
		
		// 打印分布情况
		System.out.println("Nodes count : " + NODE_COUNT + ", Keys count : " + EXE_TIMES + ", Normal percent : " + (float) 100 / NODE_COUNT + "%");
		System.out.println("-------------------- boundary  ----------------------");
		for (Map.Entry<Node, Integer> entry : nodeRecord.entrySet()) {
			System.out.println("Node name :" + entry.getKey() + " - Times : " + entry.getValue() + " - Percent : " + (float)entry.getValue() / EXE_TIMES * 100 + "%");
		}
		
	}
	
	
	/**
	 * Gets the mock node by the material parameter
	 * 
	 * @param nodeCount 
	 * 		the count of node wanted
	 * @return
	 * 		the node list
	 */
	private List<Node> getNodes(int nodeCount) {
		List<Node> nodes = new ArrayList<Node>();
		
		for (int k = 1; k <= nodeCount; k++) {
			Node node = new Node("node" + k);
			nodes.add(node);
		}
		
		return nodes;
	}
	
	/**
	 *	All the keys	
	 */
	private List<String> getAllStrings() {
		List<String> allStrings = new ArrayList<String>(EXE_TIMES);
		
		for (int i = 0; i < EXE_TIMES; i++) {
			allStrings.add(generateRandomString(ran.nextInt(50)));
		}
		
		return allStrings;
	}
	
	/**
	 * To generate the random string by the random algorithm
	 * <br>
	 * The char between 32 and 127 is normal char
	 * 
	 * @param length
	 * @return
	 */
	private String generateRandomString(int length) {
		StringBuffer sb = new StringBuffer(length);
		
		for (int i = 0; i < length; i++) {
			sb.append((char) (ran.nextInt(95) + 32));
		}
		
		return sb.toString();
	}
}
