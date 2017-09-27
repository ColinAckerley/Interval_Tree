package structures;
import java.util.ArrayList;
public class IntervalTree
{
	IntervalTreeNode root;
	public IntervalTree(ArrayList<Interval> intervals)
	{
		// make a copy of intervals to use for right sorting
		ArrayList<Interval> intervalsRight = new ArrayList<Interval>(intervals.size());
		for(Interval iv:intervals)
		{
			intervalsRight.add(iv);
		}
		// rename input intervals for left sorting
		ArrayList<Interval> intervalsLeft = intervals;
		// sort intervals on left and right end points
		sortIntervals(intervalsLeft, 'l');
		sortIntervals(intervalsRight, 'r');
		// get sorted list of end points without duplicates
		ArrayList<Integer> sortedEndPoints = getSortedEndPoints(intervalsLeft, intervalsRight);
		// build the tree nodes
		root = buildTreeNodes(sortedEndPoints);
		// map intervals to the tree nodes
		mapIntervalsToTree(intervalsLeft, intervalsRight);
	}
	public IntervalTreeNode getRoot()
	{
		return root;
	}
	public static void sortIntervals(ArrayList<Interval> intervals, char lr)
	{
		ArrayList<Integer> rightPoints = new ArrayList<Integer>();
		ArrayList<Integer> leftPoints = new ArrayList<Integer>();
		for(int i = 0; i < intervals.size(); i++)
		{
			leftPoints.add(intervals.get(i).leftEndPoint);
			rightPoints.add(intervals.get(i).rightEndPoint);
		}
		if(lr == 'l')
			doSort(leftPoints);
		else
			doSort(rightPoints);
	}
	public static void doSort(ArrayList<Integer> toSort)
	{
		for(int i = 1; i < toSort.size(); i++)
		{
			int newVal = toSort.get(i);
			int j = i;
			while (j > 0 && toSort.get(j - 1) > newVal)
			{
				toSort.set(j, toSort.get(j - 1));
				j--;
			}
			toSort.set(j, newVal);
		}
	}
	public static ArrayList<Integer> getSortedEndPoints(ArrayList<Interval> leftSortedIntervals,
			ArrayList<Interval> rightSortedIntervals)
	{
		ArrayList<Integer> sortedPoints = new ArrayList<Integer>();
		for(int i = 0; i < leftSortedIntervals.size(); i++)
		{
			if(!sortedPoints.contains(leftSortedIntervals.get(i).leftEndPoint))
				sortedPoints.add(leftSortedIntervals.get(i).leftEndPoint);
			else
				continue;
		}
		for(int i = 0; i < rightSortedIntervals.size(); i++)
		{
			if(!sortedPoints.contains(rightSortedIntervals.get(i).rightEndPoint))
				sortedPoints.add(rightSortedIntervals.get(i).rightEndPoint);
			else
				continue;
		}
		return sortedPoints;
	}
	public static IntervalTreeNode buildTreeNodes(ArrayList<Integer> endPoints)
	{
		Queue<IntervalTreeNode> tree = new Queue<IntervalTreeNode>();
		float curVal;
		for(int i = 0; i < endPoints.size(); i++)
		{
			curVal = endPoints.get(i);
			IntervalTreeNode curNode = new IntervalTreeNode(curVal, curVal, curVal);
			curNode.leftIntervals = new ArrayList<Interval>();
			curNode.rightIntervals = new ArrayList<Interval>();
			tree.enqueue(curNode);
		}
		
		while (!tree.isEmpty())
		{
			if(tree.size() == 1)
				return tree.dequeue();
			int tmp = tree.size;
			while (tmp > 1)
			{
				IntervalTreeNode t1 = tree.dequeue();
				IntervalTreeNode t2 = tree.dequeue();
				float v1 = t1.maxSplitValue;
				float v2 = t2.minSplitValue;
				IntervalTreeNode N = new IntervalTreeNode((v1 + v2) / 2, t1.minSplitValue, t2.maxSplitValue);
				N.leftIntervals = new ArrayList<Interval>();
				N.rightIntervals = new ArrayList<Interval>();
				N.leftChild = t1;
				N.rightChild = t2;
				tree.enqueue(N);
				tmp -= 2;
			}
			if(tmp == 1)
			{
				tree.enqueue(tree.dequeue());
			}
		}
		return tree.dequeue();
	}
	private void recursiveMap(Interval cur, IntervalTreeNode curRoot, boolean isLeft)
	{
		if(cur.contains(curRoot.splitValue))
		{
			if(isLeft)
				curRoot.leftIntervals.add(cur);
			else
				curRoot.rightIntervals.add(cur);
			return;
		}
		if(curRoot.splitValue < cur.leftEndPoint)
			recursiveMap(cur, curRoot.rightChild, isLeft);
		else
			recursiveMap(cur, curRoot.leftChild, isLeft);
	}
	public void mapIntervalsToTree(ArrayList<Interval> leftSortedIntervals, ArrayList<Interval> rightSortedIntervals)
	{
		root.leftIntervals = new ArrayList<Interval>();
		root.rightIntervals = new ArrayList<Interval>();
		for(Interval cur:leftSortedIntervals)
		{
			recursiveMap(cur, root, true);
		}
		for(Interval cur:rightSortedIntervals)
		{
			recursiveMap(cur, root, false);
		}
		return;
	}
	public ArrayList<Interval> findIntersectingIntervals(Interval q)
	{
		return getIntersections(root, q);
	}
	public ArrayList<Interval> getIntersections(IntervalTreeNode cur, Interval q)
	{
		ArrayList<Interval> resultList = new ArrayList<Interval>();
		float splitVal = cur.splitValue;
		ArrayList<Interval> lList = cur.leftIntervals;
		ArrayList<Interval> rList = cur.rightIntervals;
		IntervalTreeNode lSub = cur.leftChild;
		IntervalTreeNode rSub = cur.rightChild;
		if(lSub == null && rSub == null)
			return resultList;
		if(q.contains(splitVal))
		{
			resultList.addAll(lList);
			resultList.addAll(getIntersections(rSub, q));
			resultList.addAll(getIntersections(lSub, q));
		}
		else if(splitVal < q.leftEndPoint)
		{
			int i = rList.size() - 1;
			while (i >= 0 && rList.get(i).intersects(q))
			{
				resultList.add(rList.get(i));
				i--;
			}
			resultList.addAll(getIntersections(rSub, q));
		}
		else if(splitVal > q.rightEndPoint)
		{
			int i = 0;
			while (i < lList.size() && lList.get(i).intersects(q))
			{
				resultList.add(lList.get(i));
				i++;
			}
			resultList.addAll(getIntersections(lSub, q));
		}
		return resultList;
	}
}