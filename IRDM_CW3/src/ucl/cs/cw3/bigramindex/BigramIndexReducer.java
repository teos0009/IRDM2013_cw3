package ucl.cs.cw3.bigramindex;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.mapreduce.Reducer;

import ucl.cs.cw3.io.ArrayListWritable;
import ucl.cs.cw3.io.InvertedIndex;
import ucl.cs.cw3.io.PairOfStringInt;
import ucl.cs.cw3.io.PairOfStrings;

/**
 * @author Tianyi Xiong
 * This is the reducer used for building bigram inverted index.
 * For each bigram , <code>emit(bigram, invertedindex)</code> and the List of (docid,tf) is sorted.
 * The count of bigram which start with word w is stored in an temporary folder in order to calculate
 * probability of each bigram in anther MapReduce job.
 */
public class BigramIndexReducer extends
		Reducer<PairOfStrings, PairOfStringInt, PairOfStrings, InvertedIndex> {

	private static InvertedIndex invertedindex = new InvertedIndex();

	@Override
	protected void reduce(PairOfStrings key, Iterable<PairOfStringInt> value,
			Context context) throws IOException, InterruptedException {
		int termfreq = 0;

		
		Map<String, Integer> indexitems = new HashMap<String, Integer>();
		for (PairOfStringInt v : value) {
			if (!indexitems.containsKey(v.getLeftElement())) {
				indexitems.put(v.toString(), v.getRightElement());
			} else {
				int tf = indexitems.get(v.getLeftElement());
				indexitems.put(v.getLeftElement(), tf + v.getRightElement());
			}
			termfreq += v.getRightElement();

		}

		invertedindex.setTermfreq(termfreq);
		invertedindex.setProb(0f);

		ArrayListWritable<PairOfStringInt> indexlist = new ArrayListWritable<PairOfStringInt>();
		for (Entry<String, Integer> e : indexitems.entrySet()) {
			indexlist.add(new PairOfStringInt(e.getKey(), e.getValue()));
		}
		// sort
		Collections.sort(indexlist);
		invertedindex.setIndex(indexlist);
		context.write(key, invertedindex);
	}

}
