import static org.junit.Assert.assertEquals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComboBox.KeySelectionManager;

import org.hamcrest.SelfDescribing;

/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 */
public class BPlusTree {

	public Node root;
	public static final int D = 2;
	/*
	public static void main(String args[]) {
		try {
			int primeNumbers[] = new int[] { 2, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14,
					15, 16 };
			BPlusTree tree=new BPlusTree();
			Utils.bulkInsert(tree, primeNumbers);

			String test=outputTree(tree);
//			System.out.println(test);
			String correct="@10/@%%@5/8/@@12/14/@%%[(2,2);(4,4);]#[(5,5);(7,7);]#[(8,8);(9,9);]$[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
//			test.compareTo(correct);
//			assertEquals(test, correct);
//			assertEquals(correct, correct);
			
		    tree.delete(2);
		    
		    test=outputTree(tree);
		    correct="@8/10/12/14/@%%[(4,4);(5,5);(7,7);]#[(8,8);(9,9);]#[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
		    System.out.print(""+test.compareTo(correct));
		    System.out.print("");
//		    assertEquals(test, correct);
		} catch (Exception e) {
			System.out.println(""+e);
		}
		
	}
	

	public static String outputTree(BPlusTree tree) {

		LinkedBlockingQueue<Node> queue;

		queue = new LinkedBlockingQueue<Node>();
		String result = "";

		int nodesInCurrentLevel = 1;
		int nodesInNextLevel = 0;
		ArrayList<Integer> childrenPerIndex = new ArrayList<Integer>();
		queue.add(tree.root);
		while (!queue.isEmpty()) {
			Node target = queue.poll();
			nodesInCurrentLevel--;
			if (target.isLeafNode) {
				LeafNode leaf = (LeafNode) target;
				result += "[";
				for (int i = 0; i < leaf.keys.size(); i++) {
					result += "(" + leaf.keys.get(i) + ","
							+ leaf.values.get(i) + ");";
				}
				childrenPerIndex.set(0, childrenPerIndex.get(0) - 1);
				if (childrenPerIndex.get(0) == 0) {
					result += "]$";
					childrenPerIndex.remove(0);
				} else {
					result += "]#";
				}
			} else {
				IndexNode index = ((IndexNode) target);
				result += "@";
				for (int i = 0; i < index.keys.size(); i++) {
					result += "" + index.keys.get(i) + "/";
				}
				result += "@";
				queue.addAll(index.children);
				if (index.children.get(0).isLeafNode) {
					childrenPerIndex.add(index.children.size());
				}
				nodesInNextLevel += index.children.size();
			}

			if (nodesInCurrentLevel == 0) {
				result += "%%";
				nodesInCurrentLevel = nodesInNextLevel;
				nodesInNextLevel = 0;
			}
		}
		System.out.println(result);
		return result;

	}
	*/

	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public String search(int key) {
		Node curIndexNode = root;
		if (!curIndexNode.isLeafNode) {
			while (!curIndexNode.isLeafNode) {
				int childrenIndex = -1;
				for (int itr = 0; itr < curIndexNode.keys.size(); itr++) {
					if (key < curIndexNode.keys.get(itr)) {
						childrenIndex = itr;
						break;
					}
				}
				if (childrenIndex == -1)
				{
					childrenIndex = curIndexNode.keys.size()+1;
				}
				curIndexNode = ((IndexNode)curIndexNode).children.get(childrenIndex);
			}
		}
		
		
		for (int keyItr = 0; keyItr < curIndexNode.keys.size(); keyItr++)
		{
			if (key == curIndexNode.keys.get(keyItr)) {
				return ((LeafNode)curIndexNode).values.get(keyItr);
			}
		}
		
		return null;
	}

	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(int key, String value) {
		Node curNode = root;
		
		if (curNode == null) {
			LeafNode newLeafNode = new LeafNode(key, value);
			root = newLeafNode;
			curNode = newLeafNode;
		} else {
			if ( !curNode.isLeafNode) {
				while (!curNode.isLeafNode) {
					int childrenIndex = -1;
					for (int itr = 0; itr < curNode.keys.size(); itr++) {
						if (key < curNode.keys.get(itr)) {
							childrenIndex = itr;
							break;
						}
					}
					if (childrenIndex == -1)
					{
						childrenIndex = curNode.keys.size();
					}
					curNode = ((IndexNode)curNode).children.get(childrenIndex);
				}
			}
			((LeafNode)curNode).insertSorted(key, value);
		}
		splitAfterInsert(curNode);
	}

	private void splitAfterInsert(Node curNode)
	{
		if (curNode.keys.size()>BPlusTree.D*2) {
			Entry<Integer, Node> resultEntry;
			if (curNode.isLeafNode) {
				resultEntry = splitLeafNode((LeafNode)curNode);
			} else
			{
				resultEntry = splitIndexNode((IndexNode)curNode);
			}
			
			if (curNode.fatherNode == null) {
				Node rightNode = (Node)resultEntry.getValue();
				IndexNode newRoot = new IndexNode((Integer)resultEntry.getKey(), curNode,rightNode);
				curNode.fatherNode = newRoot;
				rightNode.fatherNode = newRoot;
				root = newRoot;
			} else {
				Node rightNode = resultEntry.getValue();
				IndexNode father = (IndexNode)curNode.fatherNode;
				int cursorInteger = -1;
				for (int itr = 0; itr < father.keys.size(); itr++) {
					Node curChild = father.children.get(itr);
					if (curChild == curNode) {
						cursorInteger = itr;
						break;
					}
				}
				if (cursorInteger == -1) {
					cursorInteger = father.keys.size();
				}
				// insert items in position of cursorInteger of keys
				father.keys.add(cursorInteger, resultEntry.getKey());
				
				// insert items in position of cursorInteger of children
				father.children.add(cursorInteger+1, rightNode);
				rightNode.fatherNode = father;
				
				this.splitAfterInsert(curNode.fatherNode);
			}

		}
	}
	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf
	 * @return the key/node pair as an Entry
	 */
	public Entry<Integer, Node> splitLeafNode(LeafNode leaf) {
		// init right leaf node
		System.out.print("Origin Len"+leaf.keys.size()+";");
		int halfIndex = leaf.keys.size()/2;
		int maxIndex = leaf.keys.size()-1;
		if (maxIndex < 0) {
			maxIndex = 0;
		}
		List<String> rightNodeValues = leaf.values.subList(halfIndex, maxIndex+1);
		List<Integer> rightNodeKeys = leaf.keys.subList(halfIndex, maxIndex+1);
		LeafNode rightLeafNode = new LeafNode(rightNodeKeys,rightNodeValues);
		
		// remove items from leaf
		int count = leaf.keys.size()-leaf.keys.size()/2;
		for (int itr = 0; itr < count; itr++) {
			leaf.keys.remove(leaf.keys.size()-1);
			leaf.values.remove(leaf.values.size()-1);
		}
		
		// rebind nextLeaf
		rightLeafNode.nextLeaf = leaf.nextLeaf;
		leaf.nextLeaf = rightLeafNode;
		System.out.println("Current Len:"+leaf.keys.size()+";Right Len:"+rightLeafNode.keys.size()+";Right Start Node:"+rightLeafNode.keys.get(0));
//		Entry<Integer, Node> newEntry = (rightLeafNode.keys.get(0), rightLeafNode);
		return new AbstractMap.SimpleEntry<Integer, Node>(rightLeafNode.keys.get(0), rightLeafNode);
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index
	 * @return new key/node pair as an Entry
	 */
	public Entry<Integer, Node> splitIndexNode(IndexNode index) {
		// init right index node
		int halfIndex = index.keys.size()/2;
		int maxIndex = index.keys.size()-1;
		if (maxIndex < 0) {
			maxIndex = 0;
		}
		List<Node> rightNodeChildren = index.children.subList(halfIndex+1, maxIndex+2);
		List<Integer> rightNodeKeys = index.keys.subList(halfIndex+1, maxIndex+1);
		IndexNode rightIndexNode = new IndexNode(rightNodeKeys, rightNodeChildren);
		
		int halfKey = index.keys.get(halfIndex);
		
		// remove items from inex
		int count = index.keys.size()-index.keys.size()/2;
		for (int itr = 0; itr < count; itr++) {
			index.keys.remove(index.keys.size()-1);
			index.children.remove(index.children.size()-1);
		}
				
		return new AbstractMap.SimpleEntry<Integer, Node>(halfKey, rightIndexNode);

	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(int key) {
		Node curNode = root;
		
		if ( !curNode.isLeafNode) {
			while (!curNode.isLeafNode) {
				int childrenIndex = -1;
				for (int itr = 0; itr < curNode.keys.size(); itr++) {
					if (key < curNode.keys.get(itr)) {
						childrenIndex = itr;
						break;
					}
				}
				if (childrenIndex == -1)
				{
					childrenIndex = curNode.keys.size();
				}
				curNode = ((IndexNode)curNode).children.get(childrenIndex);
			}
		}
		
		LeafNode curLeaf = (LeafNode)curNode;
		for (int itr = 0; itr < curLeaf.keys.size(); itr++) {
			int curKey = curLeaf.keys.get(itr);
			if (curKey == key) {
				curLeaf.keys.remove(itr);
				curLeaf.values.remove(itr);
			}
		}
		mergeAfterDelete(curLeaf);
		
	}
	
	private void mergeAfterDelete(Node curNode)
	{
		if (curNode.keys.size() < BPlusTree.D) {
			Node leftNode, rightNode;
			IndexNode father = (IndexNode)curNode.fatherNode;
			int curNodeIndex = -1;
			for (int itr = 0; itr < father.children.size(); itr++) {
				Node itrNode = father.children.get(itr);
				if (itrNode == curNode) {
					curNodeIndex = itr;
					break;
				}
			}
			
			if (curNodeIndex == 0) {
				leftNode = father.children.get(curNodeIndex);
				rightNode = father.children.get(curNodeIndex+1);
			} else
			{
				rightNode = father.children.get(curNodeIndex);
				leftNode = father.children.get(curNodeIndex-1);
			}
			int splitKeyPosition = -1;
			if (curNode.isLeafNode) {
				splitKeyPosition = handleLeafNodeUnderflow((LeafNode)leftNode, (LeafNode)rightNode, (IndexNode)curNode.fatherNode);
			} else
			{
				splitKeyPosition = handleIndexNodeUnderflow((IndexNode)leftNode, (IndexNode)rightNode, (IndexNode)curNode.fatherNode);
			}
			if (splitKeyPosition != -1)
			{
				System.out.println("Before remove"+curNode.fatherNode.keys.size());
				curNode.fatherNode.keys.remove(splitKeyPosition);
				System.out.println("After remove"+curNode.fatherNode.keys.size());
				((IndexNode)curNode.fatherNode).children.remove(splitKeyPosition+1);
				if (curNode.fatherNode.keys.size() == 0 && curNode.fatherNode == root)
				{
					root = curNode;
					return;
				}
			}
			mergeAfterDelete(curNode.fatherNode);
		}
			
			
			
	}
	
	
	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode left, LeafNode right,
			IndexNode parent) {

		int splitKeyPosition = -1;
		
		for (int itr = 0; itr < parent.keys.size(); itr++) {
			Node leftLeaf = parent.children.get(itr);
			Node rightLeaf = parent.children.get(itr+1);
			if ((leftLeaf == left) && (rightLeaf == right)) {
				splitKeyPosition = itr;
				break;
			}
		}
		
		if (left.keys.size()< BPlusTree.D && right.keys.size() > BPlusTree.D+1) {
//			if (right.keys.size() > BPlusTree.D+1) {
				left.keys.add(right.keys.get(0));
				left.values.add(right.values.get(0));
				int key = right.keys.get(1);
				if (splitKeyPosition != -1) {
					left.fatherNode.keys.set(splitKeyPosition, key);
				}
				right.keys.remove(0);
				right.values.remove(0);
				splitKeyPosition = -1;
//			}
		} else if (right.keys.size() < BPlusTree.D && left.keys.size() > BPlusTree.D+1) {
//			if (left.keys.size() > BPlusTree.D+1) {
				int leftLast = left.keys.size();
				right.keys.add(0,left.keys.get(leftLast));
				right.values.add(0, left.values.get(leftLast));
				int key = left.keys.get(leftLast);
				if (splitKeyPosition != -1) {
					left.fatherNode.keys.set(splitKeyPosition, key);
				}
				left.keys.remove(leftLast);
				left.values.remove(leftLast);
				splitKeyPosition = -1;
//			}
		} else
		{
			System.out.println("Before keys:"+left.keys.size());
			left.keys.addAll(right.keys);
			System.out.println("After keys:"+left.keys.size());
			left.values.addAll(right.values);
		}
		
		return splitKeyPosition;
	}

	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode leftIndex,
			IndexNode rightIndex, IndexNode parent) {
		
		int splitKeyPosition = -1;
		
		for (int itr = 0; itr < parent.keys.size(); itr++) {
			Node leftLeaf = parent.children.get(itr);
			Node rightLeaf = parent.children.get(itr+1);
			if ((leftLeaf == leftIndex) && (rightLeaf == rightIndex)) {
				splitKeyPosition = itr;
				break;
			}
		}
		
		if (leftIndex.keys.size()< BPlusTree.D && rightIndex.keys.size() > BPlusTree.D+1) {
//			if (rightIndex.keys.size() > BPlusTree.D+1) {
				leftIndex.keys.add(rightIndex.keys.get(0));
				leftIndex.children.add(rightIndex.children.get(0));
				int key = rightIndex.keys.get(1);
				if (splitKeyPosition != -1) {
					leftIndex.fatherNode.keys.set(splitKeyPosition, key);
				}
				rightIndex.keys.remove(0);
				rightIndex.children.remove(0);
				splitKeyPosition = -1;
//			}
		} else if (rightIndex.keys.size() < BPlusTree.D && leftIndex.keys.size() > BPlusTree.D+1) {
//			if (leftIndex.keys.size() > BPlusTree.D+1) {
				int leftLast = leftIndex.keys.size();
				rightIndex.keys.add(0,leftIndex.keys.get(leftLast));
				rightIndex.children.add(0, leftIndex.children.get(leftLast));
				int key = leftIndex.keys.get(leftLast);
				if (splitKeyPosition != -1) {
					leftIndex.fatherNode.keys.set(splitKeyPosition, key);
				}
				leftIndex.keys.remove(leftLast);
				leftIndex.children.remove(leftLast);
				splitKeyPosition = -1;
//			}
		} else
		{
			int splitKey = parent.keys.get(splitKeyPosition);
			leftIndex.keys.add(splitKey);
			System.out.println("Before keys:"+leftIndex.keys.size());
			leftIndex.keys.addAll(rightIndex.keys);
			System.out.println("Before keys:"+leftIndex.keys.size());
			leftIndex.children.addAll(rightIndex.children);
		}
		
		
		
		
		return splitKeyPosition;
	}

}
