
public class Driver {
	public static void main(String[] args) throws Exception {
		
		DataDividerByUser dataDividerByUser = new DataDividerByUser();
		CoOccurrenceMatrixGenerator coOccurrenceMatrixGenerator = new CoOccurrenceMatrixGenerator();
		Normalize normalize = new Normalize();
		Normalize2 normalize_2 = new Normalize2();
		Multiplication multiplication = new Multiplication();
		Sum sum = new Sum();
		RMSE rmse = new RMSE();
		Result result = new Result();

		String rawInput = args[0];
		String userMovieListOutputDir = args[1];
		String coOccurrenceMatrixDir = args[2];
		String normalizeDir = args[3];
		String normalizeDir2 = args[4];
		String multiplicationDir = args[5];
		String sumDir = args[6];
		String RMSEDir = args[7];
		String ResultDir = args[8];
		String testDir = args[9];
		String[] path1 = {rawInput, userMovieListOutputDir};
		String[] path2 = {userMovieListOutputDir, coOccurrenceMatrixDir};
		String[] path3 = {coOccurrenceMatrixDir, normalizeDir};
		String[] path4 = {normalizeDir, normalizeDir2};
		String[] path5 = {normalizeDir2, rawInput, multiplicationDir};
		String[] path6 = {multiplicationDir, sumDir};
		String[] path7 = {sumDir, testDir,RMSEDir};
		String[] path8 = {RMSEDir, ResultDir};

		dataDividerByUser.main(path1);
		coOccurrenceMatrixGenerator.main(path2);
		normalize.main(path3);
		normalize_2.main(path4);
		multiplication.main(path5);
		sum.main(path6);
		rmse.main(path7);
		result.main(path8);


	}

}
