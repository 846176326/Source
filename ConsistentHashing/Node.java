package ConsistentHashing;

public class Node {

	String Name;

	public Node(String nodeName) {
		this.Name = nodeName;
	}

	public String getName() {
		return Name;
	}

	public void setName(String nodeName) {
		this.Name = nodeName;
	}

	@Override
	public String toString() {
		return "Node [nodeName=" + Name + "]";
	}

}
