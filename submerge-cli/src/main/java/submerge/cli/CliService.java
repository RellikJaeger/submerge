package submerge.cli;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import submerge.cli.configuration.ConfigurationLoader;
import submerge.cli.configuration.user.AssConfiguration;
import submerge.cli.configuration.user.DualSubConfiguration;

import com.submerge.sub.config.SubInput;
import com.submerge.sub.convert.SubtitleConverter;
import com.submerge.sub.exception.InvalidFileException;
import com.submerge.sub.exception.InvalidSubException;
import com.submerge.sub.object.ass.ASSSub;
import com.submerge.sub.object.itf.TimedTextFile;
import com.submerge.sub.object.srt.SRTSub;
import com.submerge.sub.parser.ParserFactory;
import com.submerge.sub.parser.itf.SubtitleParser;

public class CliService {

	/**
	 * Convert a file (srt or ass) to srt and write the new file on disk
	 * 
	 * @param file the file to convert
	 * @param outputFilename the destination file name
	 * @throws InvalidSubException
	 * @throws InvalidFileException
	 * @throws IOException
	 */
	public void toSRT(File file, String outputFilename) throws InvalidSubException, InvalidFileException, IOException {

		validFiles(file);
		String ext = FilenameUtils.getExtension(file.getName());

		SubtitleParser parser = ParserFactory.getParser(ext);

		TimedTextFile ttf = parser.parse(file);
		SubtitleConverter convert = new SubtitleConverter();
		SRTSub srt = convert.toSRT(ttf);

		String finalName = getFinalFilename(outputFilename, file, ext, "srt");
		FileUtils.writeStringToFile(new File(finalName), srt.toString());
	}

	/**
	 * Convert a file (srt or ass) to ass and write the new file on disk
	 * 
	 * @param file the file to convert
	 * @param outputFilename the destination file name
	 * @throws InvalidSubException
	 * @throws InvalidFileException
	 * @throws IOException
	 */
	public void toASS(File file, String outputFilename) throws InvalidSubException, InvalidFileException, IOException {

		validFiles(file);
		String ext = FilenameUtils.getExtension(file.getName());

		SubtitleParser parser = ParserFactory.getParser(ext);
		TimedTextFile ttf = parser.parse(file);

		AssConfiguration config = ConfigurationLoader.loadUserConfiguration().getSimpleAssConfig();

		SubInput subInput = new SubInput(ttf, config.getFontConfig().getFont());
		subInput.setStyleName(config.getStyleName());

		SubtitleConverter convert = new SubtitleConverter();
		ASSSub ass = convert.toASS(subInput);

		String finalName = getFinalFilename(outputFilename, file, ext, "ass");
		FileUtils.writeStringToFile(new File(finalName), ass.toString());
	}

	/**
	 * Change the file encoding
	 * 
	 * @param file the file to change encoding from
	 * @param outputFilename the destination file name, null to overwrite the input file
	 * @throws IOException
	 * @throws InvalidFileException
	 */
	public void toUTF8(File file, String outputFilename) throws IOException, InvalidFileException {

		validFiles(file);
		String encoding = com.submerge.sub.utils.FileUtils.guessEncoding(file);
		String content = FileUtils.readFileToString(file, encoding);

		if (StringUtils.isEmpty(outputFilename)) {
			FileUtils.writeStringToFile(file, content, "UTF-8");
		} else {
			FileUtils.writeStringToFile(new File(outputFilename), content, "UTF-8");
		}
	}

	/**
	 * Merge 2 files (srt or ass) in one ass and write the new file on disk
	 * 
	 * @param topFile the filename of top subtitle
	 * @param botFile the filename of bottom subtitle
	 * @param outputFilename the targer filename
	 * @throws InvalidSubException
	 * @throws InvalidFileException
	 * @throws IOException
	 */
	public void mergeToASS(File topFile, File botFile, String outputFilename) throws InvalidSubException,
			InvalidFileException, IOException {

		validFiles(topFile, botFile);

		String topExt = FilenameUtils.getExtension(topFile.getName());
		String botExt = FilenameUtils.getExtension(botFile.getName());

		TimedTextFile topTtf = ParserFactory.getParser(topExt).parse(topFile);
		TimedTextFile botTtf = ParserFactory.getParser(botExt).parse(botFile);

		DualSubConfiguration config = ConfigurationLoader.loadUserConfiguration().getDualAssConfig();

		AssConfiguration topConfig = config.getTop();
		AssConfiguration botConfig = config.getBottom();

		SubInput topSubInput = new SubInput(topTtf, topConfig.getFontConfig().getFont());
		SubInput botSubInput = new SubInput(botTtf, botConfig.getFontConfig().getFont());

		topSubInput.setStyleName(topConfig.getStyleName());
		botSubInput.setStyleName(botConfig.getStyleName());

		topSubInput.setAlignment(8);
		botSubInput.setAlignment(2);

		SubtitleConverter convert = new SubtitleConverter();
		ASSSub ass = convert.mergeToAss(topSubInput, botSubInput);

		String finalName = outputFilename;
		if (StringUtils.isEmpty(finalName)) {
			finalName = config.getFilename();
		}

		String topName = FilenameUtils.getBaseName(topFile.getName());
		String botName = FilenameUtils.getBaseName(botFile.getName());

		Map<String, String> substitutes = new HashMap<>();

		substitutes.put("top", topName);
		substitutes.put("bottom", botName);
		substitutes.put("baseTop", StringUtils.substringBeforeLast(topName, "."));
		substitutes.put("baseBottom", StringUtils.substringBeforeLast(botName, "."));

		StrSubstitutor substitutor = new StrSubstitutor(substitutes);
		finalName = substitutor.replace(finalName);

		FileUtils.writeStringToFile(new File(finalName + ".ass"), ass.toString());
	}

	/**
	 * Determine the filename of the generated file
	 * 
	 * @param outputFilename
	 * @param input
	 * @param ext
	 * @param targetExt
	 * @return
	 */
	private static String getFinalFilename(String outputFilename, File input, String ext, String targetExt) {

		String finalExt = "." + targetExt;
		String finalName = outputFilename;
		if (StringUtils.isEmpty(outputFilename)) {
			finalName = input.getName();
			finalName = StringUtils.removeEnd(input.getName(), "." + ext);
		}
		if (ext.equals(targetExt)) {
			finalName = StringUtils.removeEnd(finalName, finalExt) + "_out" + finalExt;
		}
		if (!finalName.endsWith(finalExt)) {
			finalName = finalName + finalExt;
		}
		return finalName;
	}

	/**
	 * Check is the file is valid
	 * 
	 * @param files
	 * @throws InvalidFileException
	 */
	private static void validFiles(File... files) throws InvalidFileException {
		for (File file : files) {
			if (!file.exists()) {
				throw new InvalidFileException("File " + file.getName() + " does not exists.");
			}
			if (!file.isFile()) {
				throw new InvalidFileException(file.getName() + " is not a file.");
			}
		}
	}
}
