package hu.u_szeged.cons;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import edu.berkeley.nlp.PCFGLA.ArrayParser;
import edu.berkeley.nlp.PCFGLA.CoarseToFineNBestParser;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.util.Numberer;
import ims.productParser.Parse;
import ims.productParser.ProductParser;
import ims.productParser.ProductParserData;

public class PPReplaceParser extends ProductParser {

	private static Map<String, Integer> wordFreqs;
	private static int threshold;

	public PPReplaceParser(CoarseToFineNBestParser p) {
		super(p);
	}

	public static void initReplaceParser(String modelFileName, int modelNumber) {
		System.setOut(new NullPrintStream());
		System.setErr(new NullPrintStream());
		// TODO: "szk.const.model", 4
		ArrayParser.SILENT = true;
		ProductParserData ppd = PPReplaceParser.loadModel(modelFileName);
		init(ppd, modelNumber);
		ProductParser.kbest = 1;
		ProductParser.skipParsingErrors = false;

		Map<String, Numberer> nums = new HashMap<>();
		nums.put("tags", ppd.getParsers().get(0).getGrammar().getTagNumberer());
		Numberer.setNumberers(nums);

		PPReplaceModel ppdr = (PPReplaceModel) ppd;

		wordFreqs = ppdr.wordFreqs;
		threshold = ppdr.threshold;
		// System.out.println("Initializing the product parser...");

	}

	private static ProductParserData loadModel(String fn) {
		// System.out.println("Loading the product parser from "+fn);
		ProductParserData ppd = null;
		try {
			FileInputStream fis = new FileInputStream(fn);
		    BufferedInputStream bis = new BufferedInputStream(fis);
            GZIPInputStream gs = new GZIPInputStream(bis);
            ObjectInputStream ois = new ObjectInputStream(gs);

			ppd = (ProductParserData) ois.readObject();
            ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ppd;
	}

	public static void init(ProductParserData ppd, int modelNumber) {
		threads = new LinkedList<>();

		if (ppd.getParsers().size() < modelNumber) {
			modelNumber = ppd.getParsers().size();
		}

		for (int id = 0; id < modelNumber; ++id) {
			ProductParser p = new ProductParser(ppd.getParsers().get(id));
			threads.add(p);
		}
	}

    public static String[][] parseSentenceEx(ArrayList<ArrayList<String>> tokens) {

		// get form, lemma, pos and feat for each token into a String[][]
		String[][] tokfeats = new String[tokens.size()][4];
		int i = 0;
		for (ArrayList<String> tok: tokens) {
			tokfeats[i][0] = tok.get(0);
			tokfeats[i][1] = tok.get(1);

          /* XXX kiszedjük a 'feature'-ból, ami POS+feat
             a POS-t és a feat-ok külön-külön... */
			String annot = tok.get(2);
			String[] parts = annot.split( "\\]", 2);
			String __pos = parts[0];
			if ( ! __pos.equals("OTHER") ) { __pos += "]"; }
			String __feat = "";
			if ( parts.length > 1 ) { __feat = parts[1]; }

			/* XXX "[]" és "/" eltüntetése */
			__pos = __pos.replace("[", "");
			__pos = __pos.replace("]", "");
			__pos = __pos.replace("/", "");
			__feat = __feat.replace("[", "");
			__feat = __feat.replace("]", "");
			/* XXX feat-ban a "." helyett "|" kell? -- ez nem befolyásolta */
			//__feat = __feat.replace(".", "|");

			System.out.println( "annot = " + annot );
			System.out.println( "POS   = " + __pos );
			System.out.println( "feat  = " + __feat );
			System.out.println();

			tokfeats[i][2] = __pos;
			tokfeats[i][3] = __feat;

			i++;
		}

		return parseSentence(tokfeats);

	}

	public static String[][] parseSentence(String[][] morph) {
		// TODO: Ez az ami kívülről érdekes
		// 	/**
		//	 * Constituent parsing of a sentence, using the forms and morphological
		//	 * analysis.
		//	 *
		//	 * @param morph
		//	 *            two dimensional array of the morphological analysis of the
		//	 *            forms each row contains two elements, the first is the lemma,
		//	 *            the second is the full POS (MSD) code e.g.:[alma][Nn-sn]
		//	 * @return two dimensional array, which contains the constituent parse of a
		//	 *         sentence on the last coulumn.
		//	 */

		if (morph == null) {
			return null;
		}

		String[][] result = new String[morph.length][];
		String constInput = cerateConstInput(morph);

		// Idáig szöveg bűvészkedés...
		try {
			Parse parse = parse(constInput);

			String[][] parseArr = tree2ColoumnFormat(parse.tree);  // szöveg bűvészkedés

			for (int i = 0; i < morph.length; i++) {
				result[i] = Arrays.copyOf(morph[i], morph[i].length + 1);
				result[i][morph[i].length] = parseArr[i][2];
			}

		} catch (Exception e) {
				for (int i = 0; i < morph.length; i++) {
					result[i] = Arrays.copyOf(morph[i], morph[i].length + 1);
					result[i][morph[i].length] = "_";
				}
				System.err.println("Can not parse a sentence.");
		}

		return result;
	}

	/*
	 * Return with [word, full morphological code, constituent] format.
	 */
	private static String[][] tree2ColoumnFormat(Tree<String> tree) {
		String[] chunks = tree.toString().split("\\([^()]*\\)");

		List<String> words = tree.getTerminalYield();
		List<String> preTerms = tree.getPreTerminalYield();

		String[][] sent = new String[words.size()][3];

		for (int i = 0; i < words.size(); i++) {

			sent[i][0] = words.get(i);
			sent[i][1] = preTerms.get(i);

			String chunk = chunks[i] + "*";
			int pos = 0;
			while (pos < chunks[i + 1].length() && chunks[i + 1].charAt(pos) == ')') {
				++pos;
			}
			chunk += chunks[i + 1].substring(0, pos);
			chunks[i + 1] = chunks[i + 1].substring(pos);
			sent[i][2] = chunk.replaceAll("-[^ ]*", "").replaceAll(" ", "");
		}

		return sent;
	}

	private static String cerateConstInput(String[][] morph){
		StringBuilder sentenceBuilder = new StringBuilder();

		if (morph.length > 0) {
			String word = morph[0][0].replaceAll("\\(", "*LRB*").replaceAll("\\)", "*RRB*");

			String morphCodeBuilder = morph[0][2] + "##" + morph[0][3] + "##";
			String morphCode = morphCodeBuilder.
					replaceAll("/", "~sla~").replaceAll("-", "~hyp~").replaceAll("_", "~hb~").
					replaceAll("=", "_");

			if (wordFreqs.containsKey(word) && wordFreqs.get(word) > threshold) {
				sentenceBuilder.append(word);
			} else {
				sentenceBuilder.append(morphCode);
			}
		}

		for (int i = 1; i < morph.length; i++) {

			String word = morph[i][0].replaceAll("\\(", "*LRB*").replaceAll("\\)", "*RRB*");

			String morphCodeBuilder = morph[i][2] + "##" + morph[i][3] + "##";
			String morphCode = morphCodeBuilder.replaceAll("/", "~sla~").
					replaceAll("-", "~hyp~").replaceAll("_", "~hb~").replaceAll("=", "_");

			sentenceBuilder.append(' ');

			if (wordFreqs.containsKey(word) && wordFreqs.get(word) > threshold) {
				sentenceBuilder.append(word);
			} else {
				sentenceBuilder.append(morphCode);
			}
		}

		// String sentence =
		// sentenceBuilder.toString()..replaceAll("/", "~sla~").
		// replaceAll("-", "~hyp~").replaceAll("_", "~hb~").replaceAll("=", "_");
		return sentenceBuilder.toString();
	}

	public static void main(String[] args) {
		// initReplaceParser("", 4);

		// System.out.println(parseSentence);
	}
}
